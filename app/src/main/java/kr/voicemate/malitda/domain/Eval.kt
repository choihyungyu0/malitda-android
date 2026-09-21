package kr.voicemate.malitda.domain

/**
 * 기획서 기술지표 계산: 글자 오류율(CER)·단어 오류율(WER)·완전일치·무응답·Top-3 포함.
 * 참조문·가설문 모두 같은 정규화(TextNormalizer)를 거친 뒤 비교한다.
 */
object EvalMetrics {
    fun <T> levenshtein(a: List<T>, b: List<T>): Int {
        if (a.isEmpty()) return b.size
        if (b.isEmpty()) return a.size
        var prev = IntArray(b.size + 1) { it }
        var cur = IntArray(b.size + 1)
        for (i in 1..a.size) {
            cur[0] = i
            for (j in 1..b.size) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                cur[j] = minOf(prev[j] + 1, cur[j - 1] + 1, prev[j - 1] + cost)
            }
            val t = prev; prev = cur; cur = t
        }
        return prev[b.size]
    }

    private fun chars(s: String) = TextNormalizer.normalize(s).replace(" ", "").toList()
    private fun words(s: String) = TextNormalizer.normalize(s).split(' ').filter { it.isNotEmpty() }

    /** 공백을 뺀 글자 단위 오류율. 참조가 비면 null. */
    fun cer(ref: String, hyp: String): Double? {
        val r = chars(ref); if (r.isEmpty()) return null
        return levenshtein(r, chars(hyp)).toDouble() / r.size
    }

    /** 어절 단위 오류율. 참조가 비면 null. */
    fun wer(ref: String, hyp: String): Double? {
        val r = words(ref); if (r.isEmpty()) return null
        return levenshtein(r, words(hyp)).toDouble() / r.size
    }

    fun exact(ref: String, hyp: String): Boolean = TextNormalizer.normalize(ref) == TextNormalizer.normalize(hyp) && ref.isNotBlank()
}

data class EvalRow(
    val file: String,
    val ref: String?,
    val hyp: String,
    val alternatives: List<String>,
    val cer: Double?,
    val wer: Double?,
    val exact: Boolean,
    val top3: Boolean,
    val noResult: Boolean,
    val processingMs: Long,
    val audioMs: Long,
) {
    val withinAudio: Boolean get() = processingMs <= audioMs
}

data class EvalSummary(
    val rows: List<EvalRow>,
    val engine: String,
    val csvPath: String?,
) {
    val n: Int get() = rows.size
    val withRef: List<EvalRow> get() = rows.filter { it.ref != null }
    val meanCer: Double? get() = withRef.mapNotNull { it.cer }.takeIf { it.isNotEmpty() }?.average()
    val meanWer: Double? get() = withRef.mapNotNull { it.wer }.takeIf { it.isNotEmpty() }?.average()
    val exactRate: Double? get() = withRef.takeIf { it.isNotEmpty() }?.let { r -> r.count { it.exact }.toDouble() / r.size }
    val top3Rate: Double? get() = withRef.takeIf { it.isNotEmpty() }?.let { r -> r.count { it.top3 }.toDouble() / r.size }
    val noResultRate: Double get() = if (n == 0) 0.0 else rows.count { it.noResult }.toDouble() / n
    val withinAudioRate: Double get() = if (n == 0) 0.0 else rows.count { it.withinAudio }.toDouble() / n

    fun toCsv(): String {
        val sb = StringBuilder("file,ref,hyp,alt2,alt3,cer,wer,exact,top3,no_result,processing_ms,audio_ms,within_audio,engine\n")
        fun q(s: String?) = "\"" + (s ?: "").replace("\"", "\"\"") + "\""
        for (r in rows) {
            sb.append(listOf(
                q(r.file), q(r.ref), q(r.hyp), q(r.alternatives.getOrNull(1)), q(r.alternatives.getOrNull(2)),
                r.cer?.let { "%.4f".format(it) } ?: "", r.wer?.let { "%.4f".format(it) } ?: "",
                r.exact, r.top3, r.noResult, r.processingMs, r.audioMs, r.withinAudio, q(engine),
            ).joinToString(",")).append('\n')
        }
        return sb.toString()
    }
}
