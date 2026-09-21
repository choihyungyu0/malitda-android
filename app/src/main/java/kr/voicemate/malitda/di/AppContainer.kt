package kr.voicemate.malitda.di

import android.app.Application
import kr.voicemate.malitda.data.db.AppDatabase
import kr.voicemate.malitda.data.db.DbKeyManager
import kr.voicemate.malitda.data.repo.CorrectionRepository
import kr.voicemate.malitda.data.repo.CounterRepository
import kr.voicemate.malitda.data.repo.ExpressionRepository
import kr.voicemate.malitda.data.repo.ProfileRepository
import kr.voicemate.malitda.data.settings.SettingsStore
import kr.voicemate.malitda.metrics.Metrics
import kr.voicemate.malitda.stt.ModelInstaller
import kr.voicemate.malitda.stt.SttEngine
import kr.voicemate.malitda.stt.VoskSttEngine
import kr.voicemate.malitda.tts.TtsManager

/** 수동 DI 컨테이너. 앱 프로세스당 하나. */
class AppContainer(private val app: Application) {
    val settings: SettingsStore by lazy { SettingsStore(app) }
    val db: AppDatabase by lazy { AppDatabase.build(app, DbKeyManager(app).getOrCreateKey()) }
    val profiles: ProfileRepository by lazy { ProfileRepository(db.profileDao(), settings) }
    val expressions: ExpressionRepository by lazy { ExpressionRepository(db.expressionDao()) }
    val corrections: CorrectionRepository by lazy { CorrectionRepository(db.correctionDao()) }
    val counters: CounterRepository by lazy { CounterRepository(db.counterDao()) }
    val modelInstaller: ModelInstaller by lazy { ModelInstaller(app) }
    val stt: SttEngine by lazy { VoskSttEngine(app, modelInstaller) }
    val tts: TtsManager by lazy { TtsManager(app) }
    val metrics: Metrics by lazy { Metrics(app) }
}
