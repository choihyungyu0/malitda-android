package kr.voicemate.malitda.stt

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.io.InputStream

/**
 * 엔진 선택기. 기획서의 "수정 전 로컬 STT 사전 비교"를 위해 같은 앱 안에서 엔진을 바꿔가며
 * 같은 음원·같은 문장 정리 규칙·같은 확인·수정·승인 흐름으로 비교할 수 있게 한다.
 * 메모리 절약을 위해 선택된 엔진 하나만 메모리에 올린다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SttRouter(
    private val engines: Map<String, SttEngine>,
    initial: String,
    scope: CoroutineScope,
) : SttEngine {
    private val _selected = MutableStateFlow(if (initial in engines) initial else engines.keys.first())
    val selected: StateFlow<String> = _selected.asStateFlow()
    val ids: List<String> get() = engines.keys.toList()
    fun engineName(id: String): String = engines[id]?.name ?: id

    private val current: SttEngine get() = engines[_selected.value] ?: engines.values.first()

    override val name: String get() = current.name
    override val prepareState: StateFlow<PrepareState> =
        _selected.flatMapLatest { engines[it]!!.prepareState }.stateIn(scope, SharingStarted.Eagerly, PrepareState.NotStarted)

    override suspend fun prepare() = current.prepare()

    /** 엔진 전환: 이전 엔진을 내리고 새 엔진을 준비한다. */
    suspend fun switchTo(id: String) {
        if (id !in engines || id == _selected.value) return
        val old = current
        _selected.value = id
        old.release()
        current.prepare()
    }

    override fun startListening(callback: SttCallback): Boolean = current.startListening(callback)
    override fun stop() = current.stop()
    override fun cancel() = current.cancel()
    override suspend fun recognizeWav(input: InputStream): SttResult = current.recognizeWav(input)
    override fun release() = current.release()

    companion object {
        const val VOSK = "vosk"
        const val WHISPER = "whisper"
    }
}
