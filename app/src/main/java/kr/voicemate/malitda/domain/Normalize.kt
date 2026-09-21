package kr.voicemate.malitda.domain

import java.security.MessageDigest
import java.text.Normalizer

/**
 * M1 "완전 일치" 판정에 쓰는 정규화. 규칙은 이 세 가지뿐이며 유사도·편집거리는 쓰지 않는다.
 *  1) 유니코드 NFC 정규화 + 양끝 공백 제거
 *  2) 연속 공백을 하나로
 *  3) 문장 끝의 문장부호(. ! ? … ,)만 제거
 */
object TextNormalizer {
    private val WS = Regex("\\s+")
    private val TRAILING = charArrayOf('.', '!', '?', '…', ',', ' ', '。')

    fun normalize(text: String): String {
        val nfc = Normalizer.normalize(text, Normalizer.Form.NFC).trim()
        val collapsed = nfc.replace(WS, " ")
        return collapsed.trimEnd(*TRAILING)
    }

    fun sha256(text: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(text.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
