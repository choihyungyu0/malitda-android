package kr.voicemate.malitda.stt

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kr.voicemate.malitda.domain.SentenceCleanup
import java.io.DataInputStream
import java.io.EOFException
import java.io.InputStream
import kotlin.concurrent.thread
import kotlin.math.sqrt

/**
 * whisper.cpp(ggml-base-q5_1) 오프라인 STT. 마이크 PCM을 메모리에 모았다가 중지 시 한 번에 인식한다.
 * - 원음성은 파일로 쓰지 않는다. 부분 결과는 없다(Whisper는 비스트리밍).
 * - 무음 게이팅: 발화 에너지가 거의 없으면 인식기에 넣지 않고 무응답으로 처리한다(환각 방지).
 * - N-best 를 내지 않으므로 M1 규칙2(실제 후보 내 재정렬)는 이 엔진에서 동작하지 않는다.
 */
class WhisperSttEngine(
    private val context: Context,
    private val installer: ModelInstaller,
) : SttEngine {
    override val name: String = "whisper.cpp ${ModelInstaller.WHISPER_MODEL_VERSION}"

    private val _prepareState = MutableStateFlow<PrepareState>(PrepareState.NotStarted)
    override val prepareState: StateFlow<PrepareState> = _prepareState
    private val prepareMutex = Mutex()
    private val inferLock = java.util.concurrent.locks.ReentrantLock()
    @Volatile private var handle: Long = 0L
    private val main = Handler(Looper.getMainLooper())

    override suspend fun prepare() = prepareMutex.withLock {
        if (handle != 0L) return
        try {
            if (!WhisperNative.load()) throw IllegalStateException("whisper 네이티브 라이브러리를 불러오지 못했어요")
            if (!installer.isInstalled()) {
                _prepareState.value = PrepareState.Installing(0, 0)
                installer.install { d, t -> _prepareState.value = PrepareState.Installing(d, t) }
            }
            _prepareState.value = PrepareState.Loading
            val t0 = SystemClock.elapsedRealtime()
            val h = withContext(Dispatchers.IO) { WhisperNative.init(java.io.File(installer.targetDir, ModelInstaller.WHISPER_MODEL_FILE).absolutePath) }
            if (h == 0L) throw IllegalStateException("whisper 모델을 열지 못했어요")
            handle = h
            _prepareState.value = PrepareState.Ready(installer.installedBytes(), SystemClock.elapsedRealtime() - t0)
        } catch (t: Throwable) {
            Log.e(TAG, "whisper 준비 실패", t)
            _prepareState.value = PrepareState.Failed(t.message ?: t.javaClass.simpleName, t)
        }
    }

    // ---------------- 마이크 ----------------
    private var recorder: AudioRecord? = null
    private var recThread: Thread? = null
    @Volatile private var recording = false
    private val samples = ArrayList<ShortArray>()
    private var startedAt = 0L
    private var callback: SttCallback? = null

    @SuppressLint("MissingPermission")
    override fun startListening(callback: SttCallback): Boolean {
        if (handle == 0L) return false
        cancel()
        val minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val rec = try {
            AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxOf(minBuf * 2, SAMPLE_RATE))
        } catch (t: Throwable) { callback.onError(t); return false }
        if (rec.state != AudioRecord.STATE_INITIALIZED) { rec.release(); callback.onError(IllegalStateException("마이크를 열 수 없어요")); return false }
        this.callback = callback
        synchronized(samples) { samples.clear() }
        recorder = rec
        recording = true
        startedAt = SystemClock.elapsedRealtime()
        rec.startRecording()
        recThread = thread(name = "malitda-whisper-rec") {
            val buf = ShortArray(SAMPLE_RATE / 5) // 200ms
            while (recording) {
                val n = rec.read(buf, 0, buf.size)
                if (n > 0) synchronized(samples) { samples += buf.copyOf(n) }
                else if (n < 0) break
            }
        }
        return true
    }

    override fun stop() {
        val cb = callback ?: return
        val stopAt = SystemClock.elapsedRealtime()
        val audioMs = stopAt - startedAt
        stopRecorder()
        val pcm = synchronized(samples) { concat(samples) }
        thread(name = "malitda-whisper-infer") {
            val result = runCatching {
                val text = if (speechFrameRatio(pcm) < MIN_SPEECH_RATIO) "" else transcribeBlocking(pcm)
                SttResult(text, if (text.isBlank()) emptyList() else listOf(text), SystemClock.elapsedRealtime() - stopAt, audioMs)
            }
            main.post {
                if (callback !== cb) return@post
                callback = null
                result.onSuccess { cb.onFinal(it) }.onFailure { cb.onError(it) }
            }
        }
    }

    override fun cancel() {
        callback = null
        stopRecorder()
        synchronized(samples) { samples.clear() }
    }

    private fun stopRecorder() {
        recording = false
        recThread?.let { runCatching { it.join(500) } }
        recThread = null
        recorder?.let { runCatching { it.stop() }; runCatching { it.release() } }
        recorder = null
    }

    // ---------------- 파일 ----------------
    override suspend fun recognizeWav(input: InputStream): SttResult = withContext(Dispatchers.Default) {
        if (handle == 0L) throw IllegalStateException("STT not ready")
        val pcm = readWavPcm(input)
        val audioMs = pcm.size * 1000L / SAMPLE_RATE
        val t0 = SystemClock.elapsedRealtime()
        val text = if (speechFrameRatio(pcm) < MIN_SPEECH_RATIO) "" else transcribeBlocking(pcm)
        SttResult(text, if (text.isBlank()) emptyList() else listOf(text), SystemClock.elapsedRealtime() - t0, audioMs)
    }

    private fun transcribeBlocking(pcm: FloatArray): String {
        val h = handle; if (h == 0L) return ""
        val threads = Runtime.getRuntime().availableProcessors().coerceIn(2, 4)
        // 동시 추론 금지(메모리·스레드 경합)
        inferLock.lock()
        val raw = try { WhisperNative.transcribe(h, pcm, "ko", threads, NO_SPEECH_THOLD) } finally { inferLock.unlock() }
        return SentenceCleanup.clean(raw)
    }

    override fun release() {
        cancel()
        val h = handle; handle = 0L
        if (h != 0L) WhisperNative.release(h)
        _prepareState.value = PrepareState.NotStarted
    }

    // ---------------- 유틸 ----------------
    private fun concat(chunks: List<ShortArray>): FloatArray {
        val total = chunks.sumOf { it.size }
        val out = FloatArray(total)
        var i = 0
        for (c in chunks) { for (s in c) { out[i++] = s / 32768f } }
        return out
    }

    /** 20ms 프레임 중 RMS가 문턱을 넘는 비율(발화 유무 판단). */
    private fun speechFrameRatio(pcm: FloatArray): Float {
        if (pcm.isEmpty()) return 0f
        val frame = SAMPLE_RATE / 50
        var frames = 0; var loud = 0
        var i = 0
        while (i + frame <= pcm.size) {
            var acc = 0.0
            for (j in i until i + frame) acc += pcm[j] * pcm[j]
            val rms = sqrt(acc / frame)
            frames++; if (rms > FRAME_RMS_THOLD) loud++
            i += frame
        }
        return if (frames == 0) 0f else loud.toFloat() / frames
    }

    private fun readWavPcm(input: InputStream): FloatArray {
        val din = DataInputStream(input)
        val riff = ByteArray(12); din.readFully(riff)
        require(String(riff, 0, 4) == "RIFF" && String(riff, 8, 4) == "WAVE") { "WAV 파일이 아니에요" }
        var dataLen = -1L
        while (true) {
            val id = ByteArray(4)
            try { din.readFully(id) } catch (e: EOFException) { throw IllegalArgumentException("data 청크가 없어요") }
            val size = le32(din)
            when (String(id)) {
                "fmt " -> {
                    val fmt = ByteArray(size); din.readFully(fmt)
                    val channels = (fmt[2].toInt() and 0xff) or ((fmt[3].toInt() and 0xff) shl 8)
                    val rate = (fmt[4].toInt() and 0xff) or ((fmt[5].toInt() and 0xff) shl 8) or ((fmt[6].toInt() and 0xff) shl 16) or ((fmt[7].toInt() and 0xff) shl 24)
                    val bits = (fmt[14].toInt() and 0xff) or ((fmt[15].toInt() and 0xff) shl 8)
                    require(channels == 1 && rate == SAMPLE_RATE && bits == 16) { "16kHz mono 16bit WAV만 지원해요 (${rate}Hz/${channels}ch/${bits}bit)" }
                    if (size % 2 == 1) din.skipBytes(1)
                }
                "data" -> { dataLen = size.toLong().let { if (it <= 0 || it == 0xFFFFFFFFL) -1L else it }; break }
                else -> din.skipBytes(size + (size % 2))
            }
        }
        val bytes = if (dataLen > 0) ByteArray(dataLen.toInt()).also { din.readFully(it) } else din.readBytes()
        val out = FloatArray(bytes.size / 2)
        for (i in out.indices) {
            val s = ((bytes[2 * i].toInt() and 0xff) or (bytes[2 * i + 1].toInt() shl 8)).toShort()
            out[i] = s / 32768f
        }
        return out
    }

    private fun le32(din: DataInputStream): Int {
        val b = ByteArray(4); din.readFully(b)
        return (b[0].toInt() and 0xff) or ((b[1].toInt() and 0xff) shl 8) or ((b[2].toInt() and 0xff) shl 16) or ((b[3].toInt() and 0xff) shl 24)
    }

    companion object {
        private const val TAG = "WhisperStt"
        const val SAMPLE_RATE = 16000
        /** 20ms 프레임 RMS 문턱(정규화 PCM). 조용한 방 소음 ~0.002, 말소리 ~0.02 이상. */
        const val FRAME_RMS_THOLD = 0.008f
        /** 발화 프레임 비율이 이보다 낮으면 무응답 처리. */
        const val MIN_SPEECH_RATIO = 0.04f
        /** whisper 세그먼트 무발화 확률 문턱. */
        const val NO_SPEECH_THOLD = 0.6f
    }
}
