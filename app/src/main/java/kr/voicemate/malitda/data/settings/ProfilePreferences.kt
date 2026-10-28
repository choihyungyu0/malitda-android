package kr.voicemate.malitda.data.settings

enum class ConfirmationMode(val label: String) {
    PICTURE("그림 먼저"), TEXT("글 먼저"), TTS("소리 먼저");

    companion object {
        fun parse(value: String?) = entries.firstOrNull { it.name == value } ?: PICTURE
    }
}

enum class ListeningTimeLimit(val seconds: Int, val label: String) {
    TWENTY_SECONDS(20, "20초 · 기본"),
    SIXTY_SECONDS(60, "60초 · 더 천천히 말하기"),
    MANUAL(0, "직접 끝내기 · 시간 제한 없음");

    val autoStopDelayMillis: Long? get() = seconds.takeIf { it > 0 }?.times(1_000L)

    val guidance: String get() = "말을 마치면 말하기 끝내기를 눌러 주세요.\n" +
        if (this == MANUAL) "자동으로 끝나지 않아요." else "${seconds}초가 지나면 저절로 끝나요."

    companion object {
        fun fromSeconds(value: Int) = entries.firstOrNull { it.seconds == value } ?: TWENTY_SECONDS
    }
}

/** Defaults are migrated without replacing an existing student's chosen art style or meanings. */
data class ProfilePreferences(
    val confirmationMode: ConfirmationMode = ConfirmationMode.PICTURE,
    val candidateCount: Int = 3,
    val ttsRate: Float = 1.0f,
    val haptics: Boolean = true,
    val allowSimilarExpressions: Boolean = true,
    /** 0 means the user ends listening; only the three explicit choices are persisted. */
    val listenTimeLimitSeconds: Int = ListeningTimeLimit.TWENTY_SECONDS.seconds,
) {
    fun normalized() = copy(
        candidateCount = candidateCount.coerceIn(1, 3),
        ttsRate = if (ttsRate.isFinite()) ttsRate.coerceIn(0.5f, 1.2f) else 1f,
        listenTimeLimitSeconds = ListeningTimeLimit.fromSeconds(listenTimeLimitSeconds).seconds,
    )
}
