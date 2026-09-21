package kr.voicemate.malitda.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NormalizeTest {
    @Test fun trimsCollapsesAndStripsTrailingPunctuation() {
        assertEquals("오늘 날씨 정말 좋다", TextNormalizer.normalize("  오늘   날씨 정말 좋다!! "))
        assertEquals("오늘 날씨 정말 좋다", TextNormalizer.normalize("오늘 날씨 정말 좋다."))
        assertEquals("오늘 날씨 정말 좋다", TextNormalizer.normalize("오늘 날씨 정말 좋다"))
    }

    @Test fun doesNotChangeInteriorPunctuationOrWords() {
        assertEquals("와, 오늘 날씨", TextNormalizer.normalize("와, 오늘 날씨"))
        assertEquals("오늘 날시", TextNormalizer.normalize("오늘 날시"))
    }

    @Test fun nfcUnifiesDecomposedHangul() {
        val decomposed = "한" // 한 (자모 분해형)
        assertEquals("한", TextNormalizer.normalize(decomposed))
    }
}

class CandidateBuilderTest {
    @Test fun rawAlwaysPresentAndFirstWhenNoCorrections() {
        val c = CandidateBuilder.build("오늘 날시 좋다", listOf("오늘 날시 좋다", "오늘 날씨 좋다", "오늘 날씨 좋아"), null, emptySet())
        assertEquals(3, c.size)
        assertEquals(CandidateSource.RAW, c[0].source)
        assertEquals("오늘 날시 좋다", c[0].text)
    }

    @Test fun rule1ExactCorrectionGoesFirstAndRawStays() {
        val c = CandidateBuilder.build("오늘 날시 좋다", listOf("오늘 날시 좋다"), "오늘 날씨 좋다!", emptySet())
        assertEquals(CandidateSource.M1_EXACT, c[0].source)
        assertEquals("오늘 날씨 좋다!", c[0].text)
        assertTrue(c.any { it.source == CandidateSource.RAW && it.text == "오늘 날시 좋다" })
    }

    @Test fun rule2ReordersOnlyRealAlternatives() {
        val approved = setOf(TextNormalizer.normalize("오늘 날씨 좋아"))
        val c = CandidateBuilder.build("오늘 날시 좋다", listOf("오늘 날시 좋다", "오늘 날씨 좋다", "오늘 날씨 좋아"), null, approved)
        assertEquals(CandidateSource.M1_REORDERED, c[0].source)
        assertEquals("오늘 날씨 좋아", c[0].text)
        assertEquals(CandidateSource.RAW, c[1].source)
        assertEquals(3, c.size)
    }

    @Test fun similarityNeverCreatesNewSentence() {
        // 승인문장이 실제 후보에 없으면(유사할 뿐) 아무것도 끼워 넣지 않는다.
        val approved = setOf(TextNormalizer.normalize("오늘 날씨 너무 좋다"))
        val c = CandidateBuilder.build("오늘 날시 좋다", listOf("오늘 날시 좋다", "오늘 날씨 좋다"), null, approved)
        assertFalse(c.any { it.isM1 })
        assertEquals(2, c.size)
    }

    @Test fun duplicatesByNormalizationAreCollapsed() {
        val c = CandidateBuilder.build("네", listOf("네", "네.", "네!"), null, emptySet())
        assertEquals(1, c.size)
    }

    @Test fun engineCandidatesCappedAtThree() {
        val c = CandidateBuilder.build("a", listOf("a", "b", "c", "d", "e"), null, emptySet())
        assertEquals(3, c.size)
    }
}

class ApprovalTest {
    @Test fun anyEditRevokesApproval() {
        val t = Approval.token("와, 오늘 날씨 너무 좋다!")
        assertTrue(Approval.isApproved(t, "와, 오늘 날씨 너무 좋다!"))
        assertTrue(Approval.isApproved(t, "  와, 오늘 날씨 너무 좋다!  "))
        assertFalse(Approval.isApproved(t, "와, 오늘 날씨 너무 좋다"))
        assertFalse(Approval.isApproved(t, "와, 오늘 날씨 너무 좋다!!"))
        assertFalse(Approval.isApproved(null, "와, 오늘 날씨 너무 좋다!"))
        assertFalse(Approval.isApproved(Approval.token(""), ""))
    }
}
