package kr.voicemate.malitda.stt

import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream

/** 엔진 준비 상태. 모델은 APK 안에 있으며 첫 실행 때 앱 전용 저장소로 풀어 놓는다. */
sealed interface PrepareState {
    data object NotStarted : PrepareState
    data class Installing(val filesDone: Int, val filesTotal: Int) : PrepareState
    data object Loading : PrepareState
    data class Ready(val modelBytes: Long, val loadMs: Long) : PrepareState
    data class Failed(val message: String, val cause: Throwable? = null) : PrepareState
}

data class SttResult(
    /** 인식기가 가장 높게 본 문장(원문). 비어 있으면 무응답. */
    val raw: String,
    /** 인식기가 실제로 낸 복수 후보(원문 포함, 순서 유지). */
    val alternatives: List<String>,
    /** 중지 버튼 → 최종 결과까지 걸린 시간(ms). */
    val processingMs: Long,
    /** 녹음 길이(ms). */
    val audioMs: Long,
)

interface SttCallback {
    fun onPartial(text: String)
    fun onSegment(text: String)
    fun onFinal(result: SttResult)
    fun onError(error: Throwable)
}

interface SttEngine {
    val name: String
    val prepareState: StateFlow<PrepareState>
    suspend fun prepare()
    /** 마이크 인식 시작. 준비되지 않았으면 false. */
    fun startListening(callback: SttCallback): Boolean
    /** 중지 → 최종 결과 콜백. */
    fun stop()
    /** 취소 → 결과 없이 종료. */
    fun cancel()
    /** 16kHz mono PCM16 WAV 파일 인식(비교실험·개발용). */
    suspend fun recognizeWav(input: InputStream): SttResult
    fun release()
}
