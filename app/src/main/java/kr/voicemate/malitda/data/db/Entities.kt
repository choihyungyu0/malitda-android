package kr.voicemate.malitda.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
)

@Entity(tableName = "expressions", indices = [Index("profileId")])
data class ExpressionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val category: String,
    val label: String,
    val text: String,
    val favorite: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)

/** M1 승인 교정: (사용자, 정규화된 오인식) 당 승인문장 하나. */
@Entity(tableName = "corrections", indices = [Index(value = ["profileId", "sourceKey"], unique = true)])
data class CorrectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val sourceKey: String,
    val sourceRaw: String,
    val approvedText: String,
    val createdAt: Long,
    val lastUsedAt: Long,
    val useCount: Int = 0,
)

/** 리워드·통계용 카운터(문장 내용은 저장하지 않음). */
@Entity(tableName = "counters")
data class CounterEntity(
    @PrimaryKey val profileId: Long,
    val approvals: Int = 0,
    val shares: Int = 0,
    val expressionsAdded: Int = 0,
    val lastActiveDay: String = "",
    val streakDays: Int = 0,
    val mainCharacter: String? = null,
    val friendsSeen: Int = 1,
)
