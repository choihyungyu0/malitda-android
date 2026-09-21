package kr.voicemate.malitda.stt

import android.content.Context
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kr.voicemate.malitda.domain.SentenceCleanup
import org.json.JSONObject
import java.io.DataInputStream
import java.io.EOFException
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.InputStream

/**
 * Vosk(Kaldi) 오프라인 STT. 모델·실행기 모두 단말 안에 있고 네트워크 권한 자체가 없다.
 * - 원음성은 파일로 쓰지 않는다(AudioRecord 버퍼 → 인식기).
 * - 인식 문장은 로그에 남기지 않는다.
 */
class VoskSttEngine(
    private val context: Context,
    private val installer: ModelInstaller,
) : SttEngine {
    override val name: String = "Vosk ${ModelInstaller.MODEL_VERSION}"

    private val _prepareState = MutableStateFlow<PrepareState>(PrepareState.NotStarted)
    override val prepareState: StateFlow<PrepareState> = _prepareState

    private val prepareMutex = Mutex()
    @Volatile private var model: Model? = null
    private var speechService: SpeechService? = null
    private var activeRecognizer: Recognizer? = null
    private var activeSession: Session? = null

    override suspend fun prepare() = prepareMutex.withLock {
        if (model != null) return
        try {
            LibVosk.setLogLevel(LogLevel.WARNINGS)
            if (!installer.isInstalled()) {
                _prepareState.value = PrepareState.Installing(0, 0)
                installer.install { d, t -> _prepareState.value = PrepareState.Installing(d, t) }
            }
            _prepareState.value = PrepareState.Loading
            val t0 = SystemClock.elapsedRealtime()
            val m = withContext(Dispatchers.IO) { Model(installer.targetDir.absolutePath) }
            model = m
            _prepareState.value = PrepareState.Ready(installer.installedBytes(), SystemClock.elapsedRealtime() - t0)
        } catch (t: Throwable) {
            Log.e(TAG, "STT 준비 실패", t)
            _prepareState.value = PrepareState.Failed(t.message ?: t.javaClass.simpleName, t)
        }
    }

    override fun startListening(callback: SttCallback): Boolean {
        val m = model ?: return false
        stopInternal()
        return try {
            val rec = Recognizer(m, SAMPLE_RATE).apply { setMaxAlternatives(MAX_ALTERNATIVES) }
            activeRecognizer = rec
            val service = SpeechService(rec, SAMPLE_RATE)
            speechService = service
            val session = Session(callback)
            activeSession = session
            service.startListening(session)
            true
        } catch (t: Throwable) {
            Log.e(TAG, "startListening 실패", t)
            callback.onError(t)
            false
        }
    }

    override fun stop() {
        // stop()은 최종 결과(onFinalResult)를 한 번 더 전달한다. 처리시간 측정을 위해 요청 시각을 기록한다.
        activeSession?.stopRequestedAt = SystemClock.elapsedRealtime()
        speechService?.stop()
    }

    override fun cancel() {
        speechService?.cancel()
        stopInternal()
    }

    private fun stopInternal() {
        speechService?.let { runCatching { it.stop(); it.shutdown() } }
        speechService = null
        activeRecognizer?.let { runCatching { it.close() } }
        activeRecognizer = null
        activeSession = null
    }

    override suspend fun recognizeWav(input: InputStream): SttResult = withContext(Dispatchers.Default) {
        val m = model ?: throw IllegalStateException("STT not ready")
        val t0 = SystemClock.elapsedRealtime()
        Recognizer(m, SAMPLE_RATE).use { rec ->
            rec.setMaxAlternatives(MAX_ALTERNATIVES)
            val dataLen = skipWavHeader(input)
            val buf = ByteArray(4096)
            var total = 0L
            val segments = ArrayList<Parsed>()
            while (dataLen < 0 || total < dataLen) {
                val n = input.read(buf); if (n < 0) break
                total += n
                if (rec.acceptWaveForm(buf, n)) segments += parse(rec.result)
            }
            segments += parse(rec.finalResult)
            val merged = merge(segments)
            val audioMs = total * 1000 / (2 * SAMPLE_RATE.toLong())
            SttResult(merged.first, merged.second, SystemClock.elapsedRealtime() - t0, audioMs)
        }
    }

    /** RIFF/WAVE 청크를 읽어 'data' 청크 시작까지 건너뛴다. 16kHz mono PCM16이 아니면 예외. @return data 길이(모르면 -1) */
    private fun skipWavHeader(input: InputStream): Long {
        val din = DataInputStream(input)
        val riff = ByteArray(12); din.readFully(riff)
        require(String(riff, 0, 4) == "RIFF" && String(riff, 8, 4) == "WAVE") { "WAV 파일이 아니에요" }
        while (true) {
            val id = ByteArray(4)
            try { din.readFully(id) } catch (e: EOFException) { throw IllegalArgumentException("data 청크가 없어요") }
            val size = readLeInt(din)
            when (String(id)) {
                "fmt " -> {
                    val fmt = ByteArray(size); din.readFully(fmt)
                    val channels = (fmt[2].toInt() and 0xff) or ((fmt[3].toInt() and 0xff) shl 8)
                    val rate = (fmt[4].toInt() and 0xff) or ((fmt[5].toInt() and 0xff) shl 8) or ((fmt[6].toInt() and 0xff) shl 16) or ((fmt[7].toInt() and 0xff) shl 24)
                    val bits = (fmt[14].toInt() and 0xff) or ((fmt[15].toInt() and 0xff) shl 8)
                    require(channels == 1 && rate == 16000 && bits == 16) { "16kHz mono 16bit WAV만 지원해요 (${rate}Hz/${channels}ch/${bits}bit)" }
                    if (size % 2 == 1) din.skipBytes(1)
                }
                "data" -> return size.toLong().let { if (it <= 0 || it == 0xFFFFFFFFL) -1L else it }
                else -> { din.skipBytes(size + (size % 2)) }
            }
        }
    }

    private fun readLeInt(din: DataInputStream): Int {
        val b = ByteArray(4); din.readFully(b)
        return (b[0].toInt() and 0xff) or ((b[1].toInt() and 0xff) shl 8) or ((b[2].toInt() and 0xff) shl 16) or ((b[3].toInt() and 0xff) shl 24)
    }

    override fun release() {
        stopInternal()
        model?.close(); model = null
        _prepareState.value = PrepareState.NotStarted
    }

    /** 한 번의 듣기 세션. Vosk는 침묵 구간마다 onResult(구간 결과)를 내고, stop() 시 onFinalResult를 낸다. */
    private inner class Session(private val callback: SttCallback) : RecognitionListener {
        private val segments = ArrayList<Parsed>()
        private val startedAt = SystemClock.elapsedRealtime()
        @Volatile var stopRequestedAt: Long = 0L
        private var finished = false

        override fun onPartialResult(hypothesis: String?) {
            val p = runCatching { SentenceCleanup.clean(JSONObject(hypothesis ?: "{}").optString("partial")) }.getOrDefault("")
            if (p.isNotBlank()) callback.onPartial(p)
        }

        override fun onResult(hypothesis: String?) {
            val parsed = parse(hypothesis)
            if (parsed.top.isNotBlank()) { segments += parsed; callback.onSegment(segments.joinToString(" ") { it.top }) }
        }

        override fun onFinalResult(hypothesis: String?) {
            if (finished) return
            finished = true
            val now = SystemClock.elapsedRealtime()
            val parsed = parse(hypothesis)
            if (parsed.top.isNotBlank()) segments += parsed
            val merged = merge(segments)
            val processing = if (stopRequestedAt > 0) now - stopRequestedAt else 0L
            stopInternal()
            callback.onFinal(SttResult(merged.first, merged.second, processing, now - startedAt))
        }

        override fun onError(exception: Exception?) {
            if (finished) return
            finished = true
            stopInternal()
            callback.onError(exception ?: RuntimeException("unknown STT error"))
        }

        override fun onTimeout() { /* 시간제한을 두지 않는다 */ }
    }

    private data class Parsed(val top: String, val alternatives: List<String>)

    /** 인식기 JSON → (1순위, 후보들). 모든 문자열에 동일한 문장 정리 규칙(SentenceCleanup)을 적용한다. */
    private fun parse(json: String?): Parsed {
        if (json.isNullOrBlank()) return Parsed("", emptyList())
        return runCatching {
            val o = JSONObject(json)
            val alts = o.optJSONArray("alternatives")
            if (alts != null && alts.length() > 0) {
                val list = (0 until alts.length()).map { SentenceCleanup.clean(alts.getJSONObject(it).optString("text")) }.filter { it.isNotEmpty() }.distinct()
                Parsed(list.firstOrNull() ?: "", list)
            } else {
                val t = SentenceCleanup.clean(o.optString("text"))
                Parsed(t, if (t.isEmpty()) emptyList() else listOf(t))
            }
        }.getOrDefault(Parsed("", emptyList()))
    }

    /** 구간이 여럿이면 앞 구간의 1순위를 이어 붙이고, 마지막 구간의 후보만 복수 후보로 남긴다. */
    private fun merge(segments: List<Parsed>): Pair<String, List<String>> {
        val nonEmpty = segments.filter { it.top.isNotBlank() }
        if (nonEmpty.isEmpty()) return "" to emptyList()
        val prefix = nonEmpty.dropLast(1).joinToString(" ") { it.top }
        val last = nonEmpty.last()
        val alts = last.alternatives.ifEmpty { listOf(last.top) }.map { if (prefix.isEmpty()) it else "$prefix $it" }
        return alts.first() to alts
    }

    companion object {
        private const val TAG = "VoskStt"
        const val SAMPLE_RATE = 16000.0f
        const val MAX_ALTERNATIVES = 3
    }
}
