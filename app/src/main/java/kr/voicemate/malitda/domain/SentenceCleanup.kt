package kr.voicemate.malitda.domain

/**
 * 기획서의 "문장 정리 규칙": 인식기 출력의 표시용 정리. A/B/C 모든 조건에 동일하게 적용한다.
 * 결정론적 규칙만 쓰며 단어를 바꾸거나 새 문장을 만들지 않는다.
 *  1) 비발화 토큰 제거 — `[2]`, `<unk>` 같은 대괄호·꺾쇠 토큰
 *  2) 형태소 단위로 띄어진 조사·어미를 앞 어절에 붙임 — 목록 안의 토큰만, 한 어절에 한 번만
 *  3) 연속 공백 정리
 */
object SentenceCleanup {
    private val NON_SPEECH = Regex("\\[[^\\]]*\\]|<[^>]*>")
    private val WS = Regex("\\s+")

    /** 앞 어절에 붙일 조사·어미 목록(표시용). 여기 없는 토큰은 절대 손대지 않는다. */
    val ATTACHABLE: Set<String> = setOf(
        "은", "는", "이", "가", "을", "를", "에", "의", "에서", "에게", "께", "께서", "로", "으로", "와", "과", "도", "만", "까지", "부터",
        "처럼", "보다", "이나", "나", "든지", "라도", "이라도", "랑", "이랑", "한테", "마다", "밖에", "조차", "마저", "대로", "뿐", "들",
        "요", "고", "서", "며", "면", "지", "죠", "네", "군", "어", "아", "야", "여", "게", "래", "죠", "니", "냐", "자", "다", "요",
    )

    fun clean(text: String): String {
        val noTokens = NON_SPEECH.replace(text, " ")
        val toks = noTokens.split(WS).filter { it.isNotEmpty() }
        if (toks.isEmpty()) return ""
        val out = StringBuilder()
        var lastAbsorbed = false
        for ((i, tok) in toks.withIndex()) {
            val attach = i > 0 && !lastAbsorbed && tok in ATTACHABLE
            if (attach) { out.append(tok); lastAbsorbed = true } else { if (out.isNotEmpty()) out.append(' '); out.append(tok); lastAbsorbed = false }
        }
        return out.toString()
    }
}
