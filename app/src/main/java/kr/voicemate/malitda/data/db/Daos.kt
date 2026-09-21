package kr.voicemate.malitda.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY id") fun all(): Flow<List<ProfileEntity>>
    @Query("SELECT * FROM profiles ORDER BY id") suspend fun allOnce(): List<ProfileEntity>
    @Query("SELECT * FROM profiles WHERE id = :id") suspend fun byId(id: Long): ProfileEntity?
    @Query("SELECT * FROM profiles WHERE id = :id") fun observe(id: Long): Flow<ProfileEntity?>
    @Insert suspend fun insert(p: ProfileEntity): Long
    @Update suspend fun update(p: ProfileEntity)
    @Query("DELETE FROM profiles WHERE id = :id") suspend fun delete(id: Long)
}

@Dao
interface ExpressionDao {
    @Query("SELECT * FROM expressions WHERE profileId = :profileId ORDER BY favorite DESC, updatedAt DESC")
    fun observeAll(profileId: Long): Flow<List<ExpressionEntity>>
    @Query("SELECT * FROM expressions WHERE profileId = :profileId ORDER BY updatedAt DESC LIMIT :limit")
    fun observeRecent(profileId: Long, limit: Int): Flow<List<ExpressionEntity>>
    @Query("SELECT COUNT(*) FROM expressions WHERE profileId = :profileId") fun observeCount(profileId: Long): Flow<Int>
    @Query("SELECT COUNT(*) FROM expressions WHERE profileId = :profileId") suspend fun count(profileId: Long): Int
    @Query("SELECT * FROM expressions WHERE id = :id") suspend fun byId(id: Long): ExpressionEntity?
    @Query("SELECT * FROM expressions WHERE id = :id") fun observe(id: Long): Flow<ExpressionEntity?>
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insert(e: ExpressionEntity): Long
    @Update suspend fun update(e: ExpressionEntity)
    @Query("DELETE FROM expressions WHERE id = :id") suspend fun delete(id: Long)
    @Query("DELETE FROM expressions WHERE profileId = :profileId") suspend fun deleteAll(profileId: Long)
}

@Dao
interface CorrectionDao {
    @Query("SELECT * FROM corrections WHERE profileId = :profileId ORDER BY lastUsedAt DESC")
    fun observeAll(profileId: Long): Flow<List<CorrectionEntity>>
    @Query("SELECT * FROM corrections WHERE profileId = :profileId") suspend fun all(profileId: Long): List<CorrectionEntity>
    @Query("SELECT * FROM corrections WHERE profileId = :profileId AND sourceKey = :sourceKey LIMIT 1")
    suspend fun findExact(profileId: Long, sourceKey: String): CorrectionEntity?
    @Query("SELECT COUNT(*) FROM corrections WHERE profileId = :profileId") fun observeCount(profileId: Long): Flow<Int>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(c: CorrectionEntity): Long
    @Update suspend fun update(c: CorrectionEntity)
    @Delete suspend fun delete(c: CorrectionEntity)
    @Query("DELETE FROM corrections WHERE id = :id") suspend fun deleteById(id: Long)
    @Query("DELETE FROM corrections WHERE profileId = :profileId") suspend fun deleteAll(profileId: Long)
}

@Dao
interface CounterDao {
    @Query("SELECT * FROM counters WHERE profileId = :profileId") fun observe(profileId: Long): Flow<CounterEntity?>
    @Query("SELECT * FROM counters WHERE profileId = :profileId") suspend fun get(profileId: Long): CounterEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(c: CounterEntity)
    @Query("DELETE FROM counters WHERE profileId = :profileId") suspend fun delete(profileId: Long)
}
