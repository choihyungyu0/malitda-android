package kr.voicemate.malitda.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "malitda_settings")

data class Settings(
    val onboarded: Boolean = false,
    val consentAccepted: Boolean = false,
    val fontScale: Float = 1.0f,       // 보통 1.0 / 크게 1.2
    val ttsRate: Float = 1.0f,         // 느림 0.8 / 보통 1.0
    val haptics: Boolean = true,
    val visualEmphasis: Boolean = false,
    val rewardEnabled: Boolean = false,
    val currentProfileId: Long = 0L,
)

class SettingsStore(context: Context) {
    private val ds = context.applicationContext.dataStore

    val settings: Flow<Settings> = ds.data.map { p ->
        Settings(
            onboarded = p[K.onboarded] ?: false,
            consentAccepted = p[K.consent] ?: false,
            fontScale = p[K.fontScale] ?: 1.0f,
            ttsRate = p[K.ttsRate] ?: 1.0f,
            haptics = p[K.haptics] ?: true,
            visualEmphasis = p[K.visual] ?: false,
            rewardEnabled = p[K.reward] ?: false,
            currentProfileId = p[K.profile] ?: 0L,
        )
    }

    suspend fun current(): Settings = settings.first()

    suspend fun setOnboarded(v: Boolean) { ds.edit { it[K.onboarded] = v } }
    suspend fun setConsent(v: Boolean) { ds.edit { it[K.consent] = v } }
    suspend fun setFontScale(v: Float) { ds.edit { it[K.fontScale] = v } }
    suspend fun setTtsRate(v: Float) { ds.edit { it[K.ttsRate] = v } }
    suspend fun setHaptics(v: Boolean) { ds.edit { it[K.haptics] = v } }
    suspend fun setVisualEmphasis(v: Boolean) { ds.edit { it[K.visual] = v } }
    suspend fun setRewardEnabled(v: Boolean) { ds.edit { it[K.reward] = v } }
    suspend fun setCurrentProfile(id: Long) { ds.edit { it[K.profile] = id } }
    suspend fun clearAll() { ds.edit { it.clear() } }

    private object K {
        val onboarded = booleanPreferencesKey("onboarded")
        val consent = booleanPreferencesKey("consent_accepted")
        val fontScale = floatPreferencesKey("font_scale")
        val ttsRate = floatPreferencesKey("tts_rate")
        val haptics = booleanPreferencesKey("haptics")
        val visual = booleanPreferencesKey("visual_emphasis")
        val reward = booleanPreferencesKey("reward_enabled")
        val profile = longPreferencesKey("current_profile")
    }
}
