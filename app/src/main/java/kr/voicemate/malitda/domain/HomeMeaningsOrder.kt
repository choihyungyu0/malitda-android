package kr.voicemate.malitda.domain

import kr.voicemate.malitda.data.db.MeaningEntity

/** Home display only: keep the incoming order within personal meanings and practice samples. */
object HomeMeaningsOrder {
    fun personalFirst(meanings: List<MeaningEntity>): List<MeaningEntity> {
        val (personal, samples) = meanings.partition { !it.isSample }
        return personal + samples
    }
}
