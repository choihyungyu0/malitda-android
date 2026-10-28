package kr.voicemate.malitda

import android.app.Application
import android.os.SystemClock
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.io.InputStream
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kr.voicemate.malitda.data.db.AppDatabase
import kr.voicemate.malitda.data.settings.*
import kr.voicemate.malitda.di.AppContainer
import kr.voicemate.malitda.stt.*
import kr.voicemate.malitda.ui.vm.ListenState
import kr.voicemate.malitda.ui.vm.SessionViewModel
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Real wall-clock deadlines, synthetic STT only: no microphone or network provider. */
@RunWith(AndroidJUnit4::class)
class FinalSubmissionRuntimeQaTest {
    private val inst = InstrumentationRegistry.getInstrumentation()
    private suspend fun exercise(seconds: Int, action: suspend (SessionViewModel, ClockEngine) -> Unit) {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val db = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java).build()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val file = File(app.cacheDir, "final-clock-${UUID.randomUUID()}.preferences_pb")
        val settings = SettingsStore(app, PreferenceDataStoreFactory.create(scope = scope, produceFile = { file }))
        settings.setV2Seeded(true); settings.acceptCurrentConsent(); settings.setHaptics(false)
        val engine = ClockEngine()
        val c = AppContainer(app, db, settings, engine)
        val id = c.profiles.ensureCurrent()
        settings.setProfilePreferences(id, ProfilePreferences(haptics = false, listenTimeLimitSeconds = seconds))
        lateinit var vm: SessionViewModel
        val store = ViewModelStore()
        inst.runOnMainSync { vm = SessionViewModel(c, prepareSpeechOnInit = false); store.put("clock", vm) }
        try {
            withTimeout(10_000) { vm.profileId.first { it == id }; vm.profilePreferences.first { it.listenTimeLimitSeconds == seconds } }
            withContext(Dispatchers.Main) { vm.startListening() }
            withTimeout(10_000) { while (engine.startedAt == 0L) delay(10) }
            withContext(Dispatchers.Main) { yield() }
            assertFalse((vm.ui.value.listen as ListenState.Listening).isPreparing)
            action(vm, engine)
        } finally {
            inst.runOnMainSync { store.clear(); c.tts.release() }
            c.appScope.cancel(); scope.cancel(); db.close()
        }
    }
    @Test fun twentySecondsAutomaticallyStopsAtTheRealDeadline() = runBlocking {
        exercise(20) { vm, e ->
            delay(18_000); assertEquals(0, e.stops)
            withTimeout(7_000) { while (e.stops == 0) delay(20) }
            assertTrue("20s elapsed=${e.elapsed}", e.elapsed in 19_800..24_000)
            assertEquals(1, e.stops); assertEquals(ListenState.Processing, vm.ui.value.listen)
            withContext(Dispatchers.Main) { vm.cancelListening() }
            assertEquals(ListenState.Idle, vm.ui.value.listen)
        }
    }
    @Test fun sixtySecondsKeepsListeningPastTwentyAndStopsAtSixty() = runBlocking {
        exercise(60) { vm, e ->
            delay(21_000); assertEquals(0, e.stops)
            assertTrue(vm.ui.value.listen is ListenState.Listening)
            withTimeout(45_000) { while (e.stops == 0) delay(25) }
            assertTrue("60s elapsed=${e.elapsed}", e.elapsed in 59_800..65_000)
            assertEquals(1, e.stops); assertEquals(ListenState.Processing, vm.ui.value.listen)
        }
    }
    @Test fun manualChoiceKeepsListeningPastTwentyUntilThePersonStops() = runBlocking {
        exercise(0) { vm, e ->
            delay(21_000); assertEquals(0, e.stops)
            assertTrue(vm.ui.value.listen is ListenState.Listening)
            withContext(Dispatchers.Main) { vm.stopListening() }
            assertEquals(1, e.stops); assertTrue(e.elapsed >= 21_000)
            assertEquals(ListenState.Processing, vm.ui.value.listen)
            withContext(Dispatchers.Main) { vm.cancelListening() }
            assertEquals(ListenState.Idle, vm.ui.value.listen)
        }
    }
    private class ClockEngine : SttEngine {
        override val name = "Final synthetic deadline clock"
        override val prepareState = MutableStateFlow<PrepareState>(PrepareState.Ready(0, 0))
        @Volatile var startedAt = 0L
        @Volatile var stoppedAt = 0L
        @Volatile var stops = 0
        val elapsed get() = stoppedAt - startedAt
        override suspend fun prepare() = Unit
        override fun startListening(callback: SttCallback): Boolean { startedAt = SystemClock.elapsedRealtime(); return true }
        override fun stop() { stoppedAt = SystemClock.elapsedRealtime(); stops++ }
        override fun cancel() = Unit
        override suspend fun recognizeWav(input: InputStream): SttResult = error("No audio")
        override fun release() = Unit
    }
}
