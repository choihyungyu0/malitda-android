package kr.voicemate.malitda.ui.vm

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.InputStream
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kr.voicemate.malitda.data.db.AppDatabase
import kr.voicemate.malitda.data.settings.ListeningTimeLimit
import kr.voicemate.malitda.data.settings.ProfilePreferences
import kr.voicemate.malitda.data.settings.SettingsStore
import kr.voicemate.malitda.di.AppContainer
import kr.voicemate.malitda.stt.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises real session boundaries with a paused settings read and a fake engine; never opens a microphone. */
@RunWith(AndroidJUnit4::class)
class ListeningTimeLimitSessionTest {
    private lateinit var db: AppDatabase
    private lateinit var c: AppContainer
    private lateinit var vm: SessionViewModel
    private lateinit var modelStore: ViewModelStore
    private lateinit var preferencesData: PausingPreferences
    private lateinit var engine: FakeEngine
    private var profileId = 0L

    @Before fun setUp(): Unit = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        db = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java).build()
        preferencesData = PausingPreferences()
        val settings = SettingsStore(app, preferencesData)
        settings.setV2Seeded(true)
        settings.acceptCurrentConsent()
        settings.setHaptics(false)
        engine = FakeEngine()
        c = AppContainer(app, db, settings, engine)
        profileId = c.profiles.ensureCurrent()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            modelStore = ViewModelStore()
            vm = SessionViewModel(c, prepareSpeechOnInit = false)
            modelStore.put("listening-limit", vm)
        }
        withTimeout(10_000) {
            vm.profileId.first { it == profileId }
            vm.loadedSettings.filterNotNull().first { it.consentAccepted }
            vm.profilePreferences.first { !it.haptics }
        }
    }

    @After fun tearDown(): Unit = runBlocking {
        InstrumentationRegistry.getInstrumentation().runOnMainSync { modelStore.clear(); c.tts.release() }
        c.appScope.cancel()
        db.close()
    }

    private suspend fun waitForStart(count: Int) {
        withTimeout(10_000) { while (engine.starts < count) delay(10) }
        withContext(Dispatchers.Main) { yield() }
    }

    @Test fun capturesTheActualStoredChoiceOnceBeforeStarting() = runBlocking {
        c.settings.setProfilePreferences(profileId, ProfilePreferences(haptics = false, listenTimeLimitSeconds = 60))
        withTimeout(10_000) { vm.profilePreferences.first { it.listenTimeLimitSeconds == 60 } }
        val gate = preferencesData.pauseNextRead()
        withContext(Dispatchers.Main) { vm.startListening() }
        withTimeout(10_000) { gate.entered.await() }
        assertEquals(0, engine.starts)
        assertTrue((vm.ui.value.listen as ListenState.Listening).isPreparing)
        c.settings.setProfilePreferences(profileId, ProfilePreferences(haptics = false, listenTimeLimitSeconds = 0))
        gate.resume.complete(Unit)
        waitForStart(1)
        assertFalse((vm.ui.value.listen as ListenState.Listening).isPreparing)
        assertEquals(ListeningTimeLimit.MANUAL, (vm.ui.value.listen as ListenState.Listening).timeLimit)

        c.settings.setProfilePreferences(profileId, ProfilePreferences(haptics = false, listenTimeLimitSeconds = 20))
        withTimeout(10_000) { vm.profilePreferences.first { it.listenTimeLimitSeconds == 20 } }
        assertEquals(ListeningTimeLimit.MANUAL, (vm.ui.value.listen as ListenState.Listening).timeLimit)
        withContext(Dispatchers.Main) { vm.stopListening() }
        assertEquals(1, engine.stops)
        assertEquals(ListenState.Processing, vm.ui.value.listen)
    }

    @Test fun stopCancelAndSessionResetBeforeTheReadFinishesNeverStartTheEngine() = runBlocking {
        val actions: List<() -> Unit> = listOf(vm::stopListening, vm::cancelListening, vm::resetSession)
        for (action in actions) {
            withContext(Dispatchers.Main) { vm.cancelListening() }
            val gate = preferencesData.pauseNextRead()
            withContext(Dispatchers.Main) { vm.startListening() }
            withTimeout(10_000) { gate.entered.await() }
            assertTrue((vm.ui.value.listen as ListenState.Listening).isPreparing)
            withContext(Dispatchers.Main) { action() }
            gate.resume.complete(Unit)
            // Await a turn on the main dispatcher after the resumed read performs its session guards.
            withContext(Dispatchers.Main) { yield() }
            assertEquals(0, engine.starts)
            assertEquals(ListenState.Idle, vm.ui.value.listen)
        }
    }

    @Test fun switchingProfileDuringTheReadCannotStartForEitherPerson() = runBlocking {
        val otherProfile = c.profiles.add("합성 듣기 설정 B")
        c.settings.setProfilePreferences(otherProfile, ProfilePreferences(haptics = false, listenTimeLimitSeconds = 60))
        val gate = preferencesData.pauseNextRead()
        withContext(Dispatchers.Main) { vm.startListening() }
        withTimeout(10_000) { gate.entered.await() }
        c.profiles.switchTo(otherProfile)
        withTimeout(10_000) { vm.profileId.first { it == otherProfile } }
        gate.resume.complete(Unit)
        withContext(Dispatchers.Main) { yield() }
        assertEquals(0, engine.starts)
        assertEquals(ListenState.Idle, vm.ui.value.listen)

        withContext(Dispatchers.Main) { vm.startListening() }
        waitForStart(1)
        assertEquals(ListeningTimeLimit.SIXTY_SECONDS, (vm.ui.value.listen as ListenState.Listening).timeLimit)
        withContext(Dispatchers.Main) { vm.cancelListening() }
        assertEquals(ListenState.Idle, vm.ui.value.listen)
    }

    @Test fun synchronousFinalErrorOrCancellationCannotRestoreTheListeningState() = runBlocking {
        val callbacks: List<(SttCallback) -> Unit> = listOf(
            { it.onFinal(SttResult("", emptyList(), 0, 0)) },
            { it.onError(IllegalStateException("합성 시작 오류")) },
            { vm.cancelListening() },
        )
        for ((index, callback) in callbacks.withIndex()) {
            engine.onStart = callback
            withContext(Dispatchers.Main) { vm.startListening() }
            waitForStart(index + 1)
            withContext(Dispatchers.Main) { yield() }
            assertEquals(ListenState.Idle, vm.ui.value.listen)
        }
    }

    private class ReadGate {
        val entered = CompletableDeferred<Unit>()
        val resume = CompletableDeferred<Unit>()
    }

    private class PausingPreferences : DataStore<Preferences> {
        private val values = MutableStateFlow(emptyPreferences())
        private val mutex = Mutex()
        private var nextGate: ReadGate? = null

        fun pauseNextRead(): ReadGate = synchronized(this) {
            check(nextGate == null)
            ReadGate().also { nextGate = it }
        }

        override val data: Flow<Preferences> = flow {
            val gate = synchronized(this@PausingPreferences) { nextGate.also { nextGate = null } }
            gate?.let { it.entered.complete(Unit); it.resume.await() }
            emitAll(values)
        }

        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences = mutex.withLock {
            transform(values.value).also { values.value = it }
        }
    }

    private class FakeEngine : SttEngine {
        override val name = "합성 듣기 설정 검사"
        override val prepareState = MutableStateFlow<PrepareState>(PrepareState.Ready(0, 0))
        @Volatile var starts = 0
        @Volatile var stops = 0
        var onStart: ((SttCallback) -> Unit)? = null
        override suspend fun prepare() = Unit
        override fun startListening(callback: SttCallback): Boolean { starts++; onStart?.invoke(callback); return true }
        override fun stop() { stops++ }
        override fun cancel() = Unit
        override suspend fun recognizeWav(input: InputStream): SttResult = error("Not used")
        override fun release() = Unit
    }
}
