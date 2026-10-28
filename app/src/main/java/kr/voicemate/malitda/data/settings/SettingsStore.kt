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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kr.voicemate.malitda.domain.GuardianPin
import kr.voicemate.malitda.domain.GuardianPinResult

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
    /** 음성인식 엔진 id(SttRouter.WHISPER / VOSK). 사전 비교 결과에 따라 기본값을 정한다. */
    val sttEngine: String = "whisper",
    /** v2 기본 의미 4종을 넣었는지(최초 1회만). */
    val v2Seeded: Boolean = false,
    /** 실증 기록. 기본 꺼짐 — 지원자가 설정에서 켰을 때만 대화 시도 결과를 남긴다. */
    val trialLogging: Boolean = false,
    val consentVersion: Int = 0,
    val consentAcceptedAt: Long = 0L,
    val guardianConfigured: Boolean = false,
    val guardianFailedAttempts: Int = 0,
    val guardianLockedUntil: Long = 0L,
)

class SettingsStore(context: Context, dataStoreOverride: DataStore<Preferences>? = null) {
    private val ds = dataStoreOverride ?: context.applicationContext.dataStore
    private val guardianMutex = Mutex()

    companion object { const val CURRENT_CONSENT_VERSION = 2 }

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
            sttEngine = p[K.sttEngine] ?: "whisper",
            v2Seeded = p[K.v2Seeded] ?: false,
            trialLogging = p[K.trialLogging] ?: false,
            consentVersion = p[K.consentVersion] ?: 0,
            consentAcceptedAt = p[K.consentAcceptedAt] ?: 0L,
            guardianConfigured = !p[K.guardianCredential].isNullOrBlank(),
            guardianFailedAttempts = p[K.guardianFailures] ?: 0,
            guardianLockedUntil = p[K.guardianLockedUntil] ?: 0L,
        )
    }

    suspend fun current(): Settings = settings.first()

    suspend fun setOnboarded(v: Boolean) { ds.edit { it[K.onboarded] = v } }
    suspend fun setConsent(v: Boolean) { if (v) acceptCurrentConsent() else revokeConsent() }
    suspend fun acceptCurrentConsent() { ds.edit {
        it[K.consent] = true
        it[K.consentVersion] = CURRENT_CONSENT_VERSION
        it[K.consentAcceptedAt] = System.currentTimeMillis()
    } }
    suspend fun revokeConsent() { ds.edit {
        it[K.consent] = false
        it[K.consentVersion] = 0
        it[K.trialLogging] = false
    } }
    suspend fun setFontScale(v: Float) { ds.edit { it[K.fontScale] = if (v.isFinite()) v.coerceIn(1f, 2f) else 1f } }
    suspend fun setTtsRate(v: Float) { ds.edit { it[K.ttsRate] = if (v.isFinite()) v.coerceIn(0.5f, 1.2f) else 1f } }
    suspend fun setHaptics(v: Boolean) { ds.edit { it[K.haptics] = v } }
    suspend fun setVisualEmphasis(v: Boolean) { ds.edit { it[K.visual] = v } }
    suspend fun setRewardEnabled(v: Boolean) { ds.edit { it[K.reward] = v } }
    suspend fun setCurrentProfile(id: Long) { ds.edit { it[K.profile] = id } }
    suspend fun setSttEngine(id: String) { ds.edit { it[K.sttEngine] = id } }
    suspend fun setV2Seeded(v: Boolean) { ds.edit { it[K.v2Seeded] = v } }
    suspend fun setTrialLogging(v: Boolean) { ds.edit { it[K.trialLogging] = v } }

    fun profilePreferences(profileId: Long): Flow<ProfilePreferences> = ds.data.map { p ->
        ProfilePreferences(
            confirmationMode = ConfirmationMode.parse(p[stringKey("confirmation_mode_$profileId")]),
            candidateCount = p[intKey("candidate_count_$profileId")] ?: 3,
            ttsRate = p[floatKey("tts_rate_$profileId")] ?: (p[K.ttsRate] ?: 1f),
            haptics = p[booleanKey("haptics_$profileId")] ?: (p[K.haptics] ?: true),
            allowSimilarExpressions = p[booleanKey("allow_similar_$profileId")] ?: true,
            listenTimeLimitSeconds = p[intKey("listen_time_limit_$profileId")] ?: ListeningTimeLimit.TWENTY_SECONDS.seconds,
        ).normalized()
    }

    suspend fun setProfilePreferences(profileId: Long, preferences: ProfilePreferences) {
        require(profileId > 0L)
        val v = preferences.normalized()
        ds.edit { p ->
            p[stringKey("confirmation_mode_$profileId")] = v.confirmationMode.name
            p[intKey("candidate_count_$profileId")] = v.candidateCount
            p[floatKey("tts_rate_$profileId")] = v.ttsRate
            p[booleanKey("haptics_$profileId")] = v.haptics
            p[booleanKey("allow_similar_$profileId")] = v.allowSimilarExpressions
            p[intKey("listen_time_limit_$profileId")] = v.listenTimeLimitSeconds
        }
    }

    /** A configured PIN cannot be overwritten from the locked setup route. */
    suspend fun setGuardianPin(pin: String): Boolean = guardianMutex.withLock {
        if (!GuardianPin.valid(pin) || !ds.data.first()[K.guardianCredential].isNullOrBlank()) return@withLock false
        val encoded = withContext(Dispatchers.Default) { GuardianPin.encode(pin) }
        var stored = false
        ds.edit { p ->
            if (p[K.guardianCredential].isNullOrBlank()) {
                p[K.guardianCredential] = encoded
                p[K.guardianFailures] = 0
                p[K.guardianLockedUntil] = 0L
                stored = true
            }
        }
        stored
    }

    suspend fun verifyGuardianPin(pin: String): GuardianPinResult = guardianMutex.withLock {
        val p = ds.data.first()
        val now = System.currentTimeMillis()
        val until = p[K.guardianLockedUntil] ?: 0L
        if (until > now) return@withLock GuardianPinResult.Locked((until - now + 999L) / 1000L)
        val encoded = p[K.guardianCredential] ?: return@withLock GuardianPinResult.NotConfigured
        val matches = withContext(Dispatchers.Default) { GuardianPin.matches(pin, encoded) }
        if (matches) {
            ds.edit { it[K.guardianFailures] = 0; it[K.guardianLockedUntil] = 0L }
            GuardianPinResult.Unlocked
        } else {
            val failures = ((p[K.guardianFailures] ?: 0) + 1).coerceAtMost(1_000_000)
            val delay = GuardianPin.cooldownMillis(failures)
            ds.edit { it[K.guardianFailures] = failures; it[K.guardianLockedUntil] = System.currentTimeMillis() + delay }
            if (delay > 0L) GuardianPinResult.Locked((delay + 999L) / 1000L)
            else GuardianPinResult.Incorrect(5 - failures % 5)
        }
    }

    /** Caller must first complete Android's device-credential confirmation. Never bind this to a plain button. */
    suspend fun clearGuardianPinWithDeviceCredential() = guardianMutex.withLock {
        ds.edit { it.remove(K.guardianCredential); it[K.guardianFailures] = 0; it[K.guardianLockedUntil] = 0L }
    }

    // ---------- 학생별 그림체·R4 익명 ID ----------
    fun artStyle(profileId: Long): Flow<String> =
        ds.data.map { kr.voicemate.malitda.domain.ArtStyles.normalize(it[stringKey("art_style_$profileId")]) }

    suspend fun setArtStyle(profileId: Long, styleId: String) {
        ds.edit { it[stringKey("art_style_$profileId")] = kr.voicemate.malitda.domain.ArtStyles.normalize(styleId) }
    }

    /** R4 서버에 보낼 익명 ID. 학생 이름·DB id 대신 이 무작위 값만 서버에 간다. */
    suspend fun r4AnonId(profileId: Long): String {
        val key = stringKey("r4_anon_$profileId")
        ds.data.first()[key]?.let { return it }
        val created = java.util.UUID.randomUUID().toString()
        var result = created
        ds.edit { p -> result = p[key] ?: created.also { p[key] = it } }
        return result
    }

    private fun stringKey(name: String) = androidx.datastore.preferences.core.stringPreferencesKey(name)
    private fun intKey(name: String) = androidx.datastore.preferences.core.intPreferencesKey(name)
    private fun floatKey(name: String) = floatPreferencesKey(name)
    private fun booleanKey(name: String) = booleanPreferencesKey(name)
    suspend fun clearProfile(profileId: Long) {
        require(profileId > 0L)
        ds.edit { p ->
            p.remove(stringKey("art_style_$profileId"))
            p.remove(stringKey("r4_anon_$profileId"))
            p.remove(stringKey("confirmation_mode_$profileId"))
            p.remove(intKey("candidate_count_$profileId"))
            p.remove(floatKey("tts_rate_$profileId"))
            p.remove(booleanKey("haptics_$profileId"))
            p.remove(booleanKey("allow_similar_$profileId"))
            p.remove(intKey("listen_time_limit_$profileId"))
            if (p[K.profile] == profileId) p[K.profile] = 0L
        }
    }
    suspend fun clearAll() { guardianMutex.withLock { ds.edit { it.clear() } } }

    private object K {
        val onboarded = booleanPreferencesKey("onboarded")
        val consent = booleanPreferencesKey("consent_accepted")
        val fontScale = floatPreferencesKey("font_scale")
        val ttsRate = floatPreferencesKey("tts_rate")
        val haptics = booleanPreferencesKey("haptics")
        val visual = booleanPreferencesKey("visual_emphasis")
        val reward = booleanPreferencesKey("reward_enabled")
        val profile = longPreferencesKey("current_profile")
        val sttEngine = androidx.datastore.preferences.core.stringPreferencesKey("stt_engine")
        val v2Seeded = booleanPreferencesKey("v2_seeded")
        val trialLogging = booleanPreferencesKey("trial_logging")
        val consentVersion = androidx.datastore.preferences.core.intPreferencesKey("consent_version")
        val consentAcceptedAt = longPreferencesKey("consent_accepted_at")
        val guardianCredential = androidx.datastore.preferences.core.stringPreferencesKey("guardian_credential_v1")
        val guardianFailures = androidx.datastore.preferences.core.intPreferencesKey("guardian_failures")
        val guardianLockedUntil = longPreferencesKey("guardian_locked_until")
    }
}
