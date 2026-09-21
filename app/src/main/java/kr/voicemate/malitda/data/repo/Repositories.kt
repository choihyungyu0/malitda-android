package kr.voicemate.malitda.data.repo

import kr.voicemate.malitda.data.db.CorrectionDao
import kr.voicemate.malitda.data.db.CorrectionEntity
import kr.voicemate.malitda.data.db.CounterDao
import kr.voicemate.malitda.data.db.CounterEntity
import kr.voicemate.malitda.data.db.ExpressionDao
import kr.voicemate.malitda.data.db.ExpressionEntity
import kr.voicemate.malitda.data.db.ProfileDao
import kr.voicemate.malitda.data.db.ProfileEntity
import kr.voicemate.malitda.data.settings.SettingsStore
import kr.voicemate.malitda.domain.Category
import kr.voicemate.malitda.domain.Limits
import kr.voicemate.malitda.domain.TextNormalizer
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class ProfileRepository(private val dao: ProfileDao, private val settings: SettingsStore) {
    val all: Flow<List<ProfileEntity>> = dao.all()
    fun observe(id: Long): Flow<ProfileEntity?> = dao.observe(id)

    /** 현재 프로필이 없으면 기본 사용자를 만든다. 반환값은 현재 프로필 id. */
    suspend fun ensureCurrent(): Long {
        val cur = settings.current().currentProfileId
        if (cur != 0L && dao.byId(cur) != null) return cur
        val existing = dao.allOnce().firstOrNull()
        val id = existing?.id ?: dao.insert(ProfileEntity(name = DEFAULT_NAME, createdAt = System.currentTimeMillis()))
        settings.setCurrentProfile(id)
        return id
    }

    suspend fun add(name: String): Long {
        val id = dao.insert(ProfileEntity(name = name.trim().ifBlank { DEFAULT_NAME }, createdAt = System.currentTimeMillis()))
        return id
    }
    suspend fun rename(id: Long, name: String) { dao.byId(id)?.let { dao.update(it.copy(name = name.trim().ifBlank { DEFAULT_NAME })) } }
    suspend fun switchTo(id: Long) = settings.setCurrentProfile(id)
    suspend fun delete(id: Long) = dao.delete(id)

    companion object { const val DEFAULT_NAME = "친구" }
}

class ExpressionRepository(private val dao: ExpressionDao) {
    fun observeAll(profileId: Long) = dao.observeAll(profileId)
    fun observeRecent(profileId: Long, limit: Int = 3) = dao.observeRecent(profileId, limit)
    fun observeCount(profileId: Long) = dao.observeCount(profileId)
    fun observe(id: Long) = dao.observe(id)
    suspend fun byId(id: Long) = dao.byId(id)

    sealed interface SaveResult { data class Ok(val id: Long) : SaveResult; data object Empty : SaveResult; data object LimitReached : SaveResult; data object TooLong : SaveResult }

    suspend fun save(profileId: Long, id: Long?, category: Category, label: String, text: String): SaveResult {
        val t = text.trim(); val l = label.trim().ifBlank { t.take(20) }
        if (t.isBlank()) return SaveResult.Empty
        if (t.length > Limits.MAX_TEXT_LENGTH) return SaveResult.TooLong
        val now = System.currentTimeMillis()
        return if (id == null || id == 0L) {
            if (dao.count(profileId) >= Limits.MAX_EXPRESSIONS) return SaveResult.LimitReached
            SaveResult.Ok(dao.insert(ExpressionEntity(profileId = profileId, category = category.key, label = l, text = t, createdAt = now, updatedAt = now)))
        } else {
            val old = dao.byId(id) ?: return SaveResult.Empty
            dao.update(old.copy(category = category.key, label = l, text = t, updatedAt = now)); SaveResult.Ok(id)
        }
    }
    suspend fun toggleFavorite(id: Long) { dao.byId(id)?.let { dao.update(it.copy(favorite = !it.favorite)) } }
    suspend fun delete(id: Long) = dao.delete(id)
    suspend fun deleteAll(profileId: Long) = dao.deleteAll(profileId)
}

class CorrectionRepository(private val dao: CorrectionDao) {
    fun observeAll(profileId: Long) = dao.observeAll(profileId)
    fun observeCount(profileId: Long) = dao.observeCount(profileId)

    /** 규칙1용: 현재 원문과 완전히 같은 오인식이 저장돼 있으면 그 승인문장. */
    suspend fun exactFor(profileId: Long, raw: String): CorrectionEntity? =
        dao.findExact(profileId, TextNormalizer.normalize(raw))

    /** 규칙2용: 이 사용자가 승인했던 문장들의 정규화 집합. */
    suspend fun approvedTextsNormalized(profileId: Long): Set<String> =
        dao.all(profileId).map { TextNormalizer.normalize(it.approvedText) }.toSet()

    /** 승인 시 호출. 원문과 승인문장이 정규화 기준으로 다를 때만 저장한다. 같은 오인식은 최신 승인으로 교체. */
    suspend fun recordApproval(profileId: Long, raw: String, approved: String): Boolean {
        val key = TextNormalizer.normalize(raw)
        val a = approved.trim()
        if (key.isEmpty() || a.isEmpty() || key == TextNormalizer.normalize(a)) return false
        val now = System.currentTimeMillis()
        val prev = dao.findExact(profileId, key)
        dao.upsert(
            CorrectionEntity(
                id = prev?.id ?: 0, profileId = profileId, sourceKey = key, sourceRaw = raw.trim(), approvedText = a,
                createdAt = prev?.createdAt ?: now, lastUsedAt = now, useCount = prev?.useCount ?: 0,
            )
        )
        return true
    }

    suspend fun markUsed(profileId: Long, raw: String) {
        dao.findExact(profileId, TextNormalizer.normalize(raw))?.let { dao.update(it.copy(useCount = it.useCount + 1, lastUsedAt = System.currentTimeMillis())) }
    }
    suspend fun delete(id: Long) = dao.deleteById(id)
    suspend fun deleteAll(profileId: Long) = dao.deleteAll(profileId)
}

class CounterRepository(private val dao: CounterDao) {
    fun observe(profileId: Long) = dao.observe(profileId)
    private suspend fun getOrInit(profileId: Long) = dao.get(profileId) ?: CounterEntity(profileId = profileId)

    private fun touchDay(c: CounterEntity): CounterEntity {
        val today = LocalDate.now().toString()
        if (c.lastActiveDay == today) return c
        val yesterday = LocalDate.now().minusDays(1).toString()
        val streak = if (c.lastActiveDay == yesterday) c.streakDays + 1 else 1
        return c.copy(lastActiveDay = today, streakDays = streak)
    }

    suspend fun onApproval(profileId: Long): CounterEntity { val c = touchDay(getOrInit(profileId)).let { it.copy(approvals = it.approvals + 1) }; dao.upsert(c); return c }
    suspend fun onShare(profileId: Long) { val c = touchDay(getOrInit(profileId)).let { it.copy(shares = it.shares + 1) }; dao.upsert(c) }
    suspend fun onExpressionAdded(profileId: Long) { val c = touchDay(getOrInit(profileId)).let { it.copy(expressionsAdded = it.expressionsAdded + 1) }; dao.upsert(c) }
    suspend fun setMainCharacter(profileId: Long, id: String) { dao.upsert(getOrInit(profileId).copy(mainCharacter = id)) }
    suspend fun setFriendsSeen(profileId: Long, n: Int) { dao.upsert(getOrInit(profileId).copy(friendsSeen = n)) }
    suspend fun reset(profileId: Long) = dao.delete(profileId)
}
