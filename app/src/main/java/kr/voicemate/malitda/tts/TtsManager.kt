package kr.voicemate.malitda.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

sealed interface TtsState {
    data object Initializing : TtsState
    data class Ready(val engine: String, val voice: String?, val offline: Boolean) : TtsState
    data class Unavailable(val reason: String) : TtsState
}

/** 읽어주는 도중 위치(단어 하이라이트용). */
data class SpeakProgress(val utteranceId: String, val start: Int, val end: Int, val done: Boolean)

/**
 * 시스템 한국어 TTS. 네트워크 필요 음성은 피하고(isNetworkConnectionRequired=false) 오프라인 음성을 우선 고른다.
 * 오프라인 한국어 음성이 없으면 Ready(offline=false)로 두고 화면에 '로컬 아님'을 표시한다.
 */
class TtsManager(context: Context) {
    private val _state = MutableStateFlow<TtsState>(TtsState.Initializing)
    val state: StateFlow<TtsState> = _state
    private val _progress = MutableStateFlow<SpeakProgress?>(null)
    val progress: StateFlow<SpeakProgress?> = _progress
    private val _speaking = MutableStateFlow(false)
    val speaking: StateFlow<Boolean> = _speaking

    private var tts: TextToSpeech? = null
    private var seq = 0

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status != TextToSpeech.SUCCESS) { _state.value = TtsState.Unavailable("TTS 엔진 초기화 실패"); return@TextToSpeech }
            val t = tts ?: return@TextToSpeech
            val avail = t.setLanguage(Locale.KOREAN)
            if (avail == TextToSpeech.LANG_MISSING_DATA || avail == TextToSpeech.LANG_NOT_SUPPORTED) {
                _state.value = TtsState.Unavailable("한국어 음성 데이터가 없어요"); return@TextToSpeech
            }
            val voice = pickOfflineKoreanVoice(t)
            if (voice != null) t.voice = voice
            val offline = (voice ?: t.voice)?.let { !it.isNetworkConnectionRequired } ?: false
            t.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) { _speaking.value = true }
                override fun onDone(utteranceId: String?) { _speaking.value = false; _progress.value = SpeakProgress(utteranceId ?: "", 0, 0, true) }
                @Deprecated("Deprecated in Java") override fun onError(utteranceId: String?) { _speaking.value = false; _progress.value = null }
                override fun onError(utteranceId: String?, errorCode: Int) { _speaking.value = false; _progress.value = null }
                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    _progress.value = SpeakProgress(utteranceId ?: "", start, end, false)
                }
            })
            _state.value = TtsState.Ready(t.defaultEngine ?: "", (voice ?: t.voice)?.name, offline)
        }
    }

    private fun pickOfflineKoreanVoice(t: TextToSpeech): Voice? = runCatching {
        t.voices?.filter { it.locale.language == "ko" && !it.isNetworkConnectionRequired && !it.features.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) }
            ?.minByOrNull { it.latency }
    }.getOrNull()

    /** @return false면 지금은 소리로 읽을 수 없음(S24). */
    fun speak(text: String, rate: Float): Boolean {
        val t = tts ?: return false
        if (_state.value !is TtsState.Ready || text.isBlank()) return false
        t.setSpeechRate(rate.coerceIn(0.5f, 1.5f))
        val id = "malitda-${++seq}"
        _progress.value = null
        val r = t.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), id)
        return r == TextToSpeech.SUCCESS
    }

    fun stop() { tts?.stop(); _speaking.value = false; _progress.value = null }

    fun release() { tts?.shutdown(); tts = null }
}
