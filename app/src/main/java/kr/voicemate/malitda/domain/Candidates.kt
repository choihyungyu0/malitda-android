package kr.voicemate.malitda.domain

enum class CandidateSource { RAW, ALTERNATIVE, M1_EXACT, M1_REORDERED }

data class Candidate(val text: String, val source: CandidateSource) {
    val isM1: Boolean get() = source == CandidateSource.M1_EXACT || source == CandidateSource.M1_REORDERED
}

/**
 * S10 후보 목록 생성. 기획서 M1 규칙만 적용한다.
 *  - 원문(raw)은 항상 목록에 남는다(삭제·치환 금지).
 *  - 규칙1: 현재 원문의 정규화 문자열이 저장한 오인식과 '완전히' 같을 때만 승인문장을 맨 앞에 둔다.
 *  - 규칙2: 인식기가 실제로 낸 복수 후보 안에 사용자가 승인했던 문장이 있으면 순서만 앞으로 옮긴다.
 *  - 유사도만으로 새 문장을 만들지 않는다.
 */
object CandidateBuilder {
    const val MAX_ENGINE_CANDIDATES = 3

    fun build(
        raw: String,
        alternatives: List<String>,
        exactCorrection: String?,
        approvedTextsNormalized: Set<String>,
    ): List<Candidate> {
        val base = ArrayList<Candidate>()
        val seen = HashSet<String>()
        val rawN = TextNormalizer.normalize(raw)
        if (rawN.isNotEmpty()) {
            base += Candidate(raw.trim(), CandidateSource.RAW); seen += rawN
        }
        for (alt in alternatives) {
            val n = TextNormalizer.normalize(alt)
            if (n.isEmpty() || n in seen) continue
            if (base.size >= MAX_ENGINE_CANDIDATES) break
            base += Candidate(alt.trim(), CandidateSource.ALTERNATIVE); seen += n
        }

        val result = ArrayList<Candidate>(base.size + 1)
        // 규칙1: 완전 일치 교정
        if (exactCorrection != null) {
            val cn = TextNormalizer.normalize(exactCorrection)
            val idx = base.indexOfFirst { TextNormalizer.normalize(it.text) == cn && it.source != CandidateSource.RAW }
            if (idx >= 0) base.removeAt(idx)
            result += Candidate(exactCorrection.trim(), CandidateSource.M1_EXACT)
        }
        // 규칙2: 실제 후보 안의 승인문장 재정렬 (원문은 자리 이동 대상이 아님)
        val reordered = base.filter { it.source == CandidateSource.ALTERNATIVE && TextNormalizer.normalize(it.text) in approvedTextsNormalized }
            .map { it.copy(source = CandidateSource.M1_REORDERED) }
        val reorderedN = reordered.map { TextNormalizer.normalize(it.text) }.toSet()
        result += reordered
        result += base.filter { TextNormalizer.normalize(it.text) !in reorderedN || it.source == CandidateSource.RAW }
        return result
    }
}
