package kr.voicemate.malitda.di

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
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
import kr.voicemate.malitda.stt.SttRouter
import kr.voicemate.malitda.stt.VoskSttEngine
import kr.voicemate.malitda.stt.WhisperSttEngine
import kr.voicemate.malitda.tts.TtsManager

/** 수동 DI 컨테이너. 앱 프로세스당 하나. */
class AppContainer(private val app: Application) {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val filesDir: java.io.File get() = app.filesDir
    val settings: SettingsStore by lazy { SettingsStore(app) }
    val db: AppDatabase by lazy { AppDatabase.build(app, DbKeyManager(app).getOrCreateKey()) }
    val profiles: ProfileRepository by lazy { ProfileRepository(db.profileDao(), settings) }
    val expressions: ExpressionRepository by lazy { ExpressionRepository(db.expressionDao()) }
    val corrections: CorrectionRepository by lazy { CorrectionRepository(db.correctionDao()) }
    val counters: CounterRepository by lazy { CounterRepository(db.counterDao()) }

    val vosk: SttEngine by lazy { VoskSttEngine(app, ModelInstaller.vosk(app)) }
    val whisper: SttEngine by lazy { WhisperSttEngine(app, ModelInstaller.whisper(app)) }
    /** 선택된 엔진 하나만 메모리에 올리는 라우터. 초기 선택은 설정값(기본 whisper). */
    val sttRouter: SttRouter by lazy {
        val initial = runBlocking { settings.current().sttEngine }
        SttRouter(linkedMapOf(SttRouter.WHISPER to whisper, SttRouter.VOSK to vosk), initial, appScope)
    }
    val stt: SttEngine get() = sttRouter

    val tts: TtsManager by lazy { TtsManager(app) }
    val metrics: Metrics by lazy { Metrics(app) }
}
