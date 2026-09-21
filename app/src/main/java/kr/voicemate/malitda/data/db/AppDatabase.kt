package kr.voicemate.malitda.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [ProfileEntity::class, ExpressionEntity::class, CorrectionEntity::class, CounterEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun expressionDao(): ExpressionDao
    abstract fun correctionDao(): CorrectionDao
    abstract fun counterDao(): CounterDao

    companion object {
        const val NAME = "malitda.db"

        /** SQLCipher로 암호화된 로컬 DB. 키는 Android Keystore가 감싼 무작위 32바이트. */
        fun build(context: Context, passphrase: ByteArray): AppDatabase {
            System.loadLibrary("sqlcipher")
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NAME)
                .openHelperFactory(SupportOpenHelperFactory(passphrase))
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }
}
