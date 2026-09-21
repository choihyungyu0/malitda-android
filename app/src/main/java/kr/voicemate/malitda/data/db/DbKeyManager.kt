package kr.voicemate.malitda.data.db

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * DB 암호화 키 관리.
 *  - 무작위 32바이트 패스프레이즈를 생성해 Android Keystore의 AES-GCM 키로 감싼 뒤 SharedPreferences에 보관한다.
 *  - Keystore 키는 기기 밖으로 나가지 않으며 앱 삭제 시 함께 사라진다.
 */
class DbKeyManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("malitda_keys", Context.MODE_PRIVATE)

    fun getOrCreateKey(): ByteArray {
        val wrapped = prefs.getString(PREF_WRAPPED, null)
        val iv = prefs.getString(PREF_IV, null)
        if (wrapped != null && iv != null) {
            val existing = runCatching { unwrap(Base64.decode(wrapped, Base64.NO_WRAP), Base64.decode(iv, Base64.NO_WRAP)) }.getOrNull()
            if (existing != null) return existing
            // Keystore 키가 무효화된 경우(기기 초기화 등): 기존 DB는 복구 불가 → 새 키를 만든다(DB는 새로 생성됨).
        }
        val raw = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val (ct, newIv) = wrap(raw)
        prefs.edit()
            .putString(PREF_WRAPPED, Base64.encodeToString(ct, Base64.NO_WRAP))
            .putString(PREF_IV, Base64.encodeToString(newIv, Base64.NO_WRAP))
            .apply()
        return raw
    }

    private fun keystoreKey(): SecretKey {
        val ks = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (ks.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val gen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        gen.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return gen.generateKey()
    }

    private fun wrap(raw: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keystoreKey())
        return cipher.doFinal(raw) to cipher.iv
    }

    private fun unwrap(ct: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, keystoreKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(ct)
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ALIAS = "malitda_db_wrap_key"
        private const val PREF_WRAPPED = "db_key_wrapped"
        private const val PREF_IV = "db_key_iv"
    }
}
