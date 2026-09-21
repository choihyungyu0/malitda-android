package kr.voicemate.malitda.domain

/**
 * 승인 토큰: 승인 시점의 문장 해시. 문장이 한 글자라도 바뀌면 해시가 달라져 공유가 자동으로 잠긴다.
 */
object Approval {
    fun token(text: String): String = TextNormalizer.sha256(text.trim())
    fun isApproved(token: String?, currentText: String): Boolean =
        token != null && currentText.isNotBlank() && token == token(currentText)
}
