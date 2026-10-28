package kr.voicemate.malitda.domain

import kr.voicemate.malitda.data.db.MeaningEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeMeaningsOrderTest {
    private fun meaning(
        id: Long,
        isSample: Boolean = false,
        useCount: Int = 0,
        createdAt: Long = id,
        imageKey: String? = "water",
        situation: String? = "쉬는 시간",
    ) = MeaningEntity(
        id = id,
        profileId = 1,
        text = "뜻 $id",
        imageKey = imageKey,
        situation = situation,
        confirmed = !isSample,
        active = true,
        createdAt = createdAt,
        updatedAt = createdAt,
        useCount = useCount,
        isSample = isSample,
        confirmationMethod = if (isSample) null else "picture",
        confirmationResponse = if (isSample) null else "matched",
        confirmedAt = if (isSample) null else createdAt,
    )

    @Test fun newlyAddedPersonalMeaningPrecedesFourFrequentlyUsedOlderSamples() {
        val samples = (1L..4L).map { meaning(it, isSample = true, useCount = 10, createdAt = it) }
        val personal = meaning(5, useCount = 0, createdAt = 100)

        val ordered = HomeMeaningsOrder.personalFirst(samples + personal)

        assertEquals(listOf(5L, 1L, 2L, 3L, 4L), ordered.map { it.id })
        assertSame(personal, ordered.first())
    }

    @Test fun preservesIncomingOrderAndOriginalEntriesWithinBothGroups() {
        val firstSample = meaning(8, isSample = true, useCount = 9, imageKey = "walk")
        val firstPersonal = meaning(7, useCount = 8, imageKey = "water", situation = "집")
        val secondSample = meaning(3, isSample = true, useCount = 7, imageKey = "rest")
        val secondPersonal = meaning(2, useCount = 6, imageKey = "cold", situation = "학교")
        val incoming = listOf(firstSample, firstPersonal, secondSample, secondPersonal)
        val original = incoming.toList()

        val ordered = HomeMeaningsOrder.personalFirst(incoming)

        assertEquals(listOf(7L, 2L, 8L, 3L), ordered.map { it.id })
        assertEquals(original, incoming)
        ordered.forEach { entry -> assertSame(original.single { it.id == entry.id }, entry) }
    }

    @Test fun sampleOnlyListKeepsItsIncomingOrder() {
        val samples = listOf(meaning(4, isSample = true), meaning(1, isSample = true))

        assertEquals(samples, HomeMeaningsOrder.personalFirst(samples))
    }

    @Test fun personalOnlyListKeepsItsIncomingOrder() {
        val personal = listOf(meaning(5), meaning(2))

        assertEquals(personal, HomeMeaningsOrder.personalFirst(personal))
    }

    @Test fun emptyListStaysEmpty() {
        assertTrue(HomeMeaningsOrder.personalFirst(emptyList()).isEmpty())
    }
}
