package kr.voicemate.malitda.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Isolated preferences only: no personal data, microphone, model loading or external service. */
@RunWith(AndroidJUnit4::class)
class ListeningTimeLimitStoreTest {
    private lateinit var prefsScope: CoroutineScope
    private lateinit var prefsFile: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var store: SettingsStore

    @Before fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        prefsScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        prefsFile = File(context.cacheDir, "listening-limit-${UUID.randomUUID()}.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create(scope = prefsScope, produceFile = { prefsFile })
        store = SettingsStore(context, dataStore)
    }

    @After fun tearDown(): Unit = runBlocking {
        prefsScope.cancel()
        prefsScope.coroutineContext[Job]?.join()
        prefsFile.delete()
        Unit
    }

    @Test fun choicesAreStoredForOnlyTheSelectedProfile() = runBlocking {
        assertEquals(20, store.profilePreferences(1).first().listenTimeLimitSeconds)
        for (seconds in listOf(60, 0, 20)) {
            store.setProfilePreferences(1, ProfilePreferences(listenTimeLimitSeconds = seconds))
            assertEquals(seconds, store.profilePreferences(1).first().listenTimeLimitSeconds)
            assertEquals(seconds, dataStore.data.first()[intPreferencesKey("listen_time_limit_1")])
            assertEquals(20, store.profilePreferences(2).first().listenTimeLimitSeconds)
        }
    }

    @Test fun invalidStoredOrRequestedLimitsFallBackToTheDefault() = runBlocking {
        dataStore.edit { it[intPreferencesKey("listen_time_limit_1")] = -99 }
        assertEquals(20, store.profilePreferences(1).first().listenTimeLimitSeconds)
        store.setProfilePreferences(1, ProfilePreferences(listenTimeLimitSeconds = Int.MAX_VALUE))
        assertEquals(20, dataStore.data.first()[intPreferencesKey("listen_time_limit_1")])
    }

    @Test fun clearingAProfileRemovesItsLimitAndKeepsTheOtherProfile() = runBlocking {
        store.setProfilePreferences(1, ProfilePreferences(listenTimeLimitSeconds = 0))
        store.setProfilePreferences(2, ProfilePreferences(listenTimeLimitSeconds = 60))
        store.clearProfile(1)
        assertNull(dataStore.data.first()[intPreferencesKey("listen_time_limit_1")])
        assertEquals(20, store.profilePreferences(1).first().listenTimeLimitSeconds)
        assertEquals(60, store.profilePreferences(2).first().listenTimeLimitSeconds)
    }
}
