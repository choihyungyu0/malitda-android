package kr.voicemate.malitda.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EvalTest {
    @Test fun levenshteinBasics() {
        assertEquals(0, EvalMetrics.levenshtein(listOf('a', 'b'), listOf('a', 'b')))
        assertEquals(2, EvalMetrics.levenshtein(listOf('a', 'b'), emptyList()))
        assertEquals(1, EvalMetrics.levenshtein("kitten".toList(), "sitten".toList()))
        assertEquals(3, EvalMetrics.levenshtein("kitten".toList(), "sitting".toList()))
    }

    @Test fun cerIgnoresSpacesAndTrailingPunctuation() {
        assertEquals(0.0, EvalMetrics.cer("오늘 날씨 정말 좋다!", "오늘날씨 정말 좋다")!!, 1e-9)
        // 오늘날씨정말좋다(8자) vs 오리날씨천마의좋다: 늘→리, 정→천, 말→마, +의 = 4 편집 / 8
        assertEquals(0.5, EvalMetrics.cer("오늘 날씨 정말 좋다", "오리 날씨 천마의 좋다")!!, 1e-9)
        assertNull(EvalMetrics.cer("", "무엇"))
    }

    @Test fun werOnWords() {
        assertEquals(0.0, EvalMetrics.wer("지금 학교에 가고 있어요", "지금 학교에 가고 있어요.")!!, 1e-9)
        assertEquals(0.25, EvalMetrics.wer("지금 학교에 가고 있어요", "지금 학교의 가고 있어요")!!, 1e-9)
    }

    @Test fun exactAndSummary() {
        assertTrue(EvalMetrics.exact("네.", "네"))
        assertFalse(EvalMetrics.exact("", ""))
        val rows = listOf(
            EvalRow("a.wav", "네", "네", listOf("네"), 0.0, 0.0, true, true, false, 100, 500),
            EvalRow("b.wav", "아니요", "", emptyList(), 1.0, 1.0, false, false, true, 900, 600),
        )
        val s = EvalSummary(rows, "test", null)
        assertEquals(0.5, s.meanCer!!, 1e-9)
        assertEquals(0.5, s.exactRate!!, 1e-9)
        assertEquals(0.5, s.noResultRate, 1e-9)
        assertEquals(0.5, s.withinAudioRate, 1e-9)
        assertTrue(s.toCsv().startsWith("file,ref,hyp"))
        assertEquals(3, s.toCsv().trim().lines().size)
    }
}
