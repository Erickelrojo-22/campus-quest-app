package com.example.gamequest.data.remote

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Session secrets are encrypted with a key that never leaves Android Keystore. */
class SessionTokenStore(context: Context) : TokenStore {
    private val prefs = context.getSharedPreferences("remote_session", Context.MODE_PRIVATE)
    private val alias = "campus_quest_session_v1"
    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
    @Volatile override var snapshot: SessionSnapshot = SessionSnapshot(read(), prefs.getInt("localUserId", -1).takeIf { it > 0 })
        private set
    private fun read(): String? = runCatching {
        val encrypted = prefs.getString("ciphertext", null) ?: return@runCatching null
        val iv = prefs.getString("iv", null) ?: return@runCatching null
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)))
        String(cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP)), Charsets.UTF_8)
    }.getOrNull()
    @Synchronized override fun save(value: String, localUserId: Int?) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        check(prefs.edit().putString("ciphertext", Base64.encodeToString(cipher.doFinal(value.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP))
            .putString("iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .putInt("localUserId", localUserId ?: -1).commit())
        snapshot = SessionSnapshot(value, localUserId)
    }
    @Synchronized override fun clearIfCurrent(expectedToken: String): Boolean {
        if (snapshot.token != expectedToken) return false
        clear()
        return true
    }
    @Synchronized override fun clear() {
        check(prefs.edit().clear().commit())
        snapshot = SessionSnapshot(null)
    }
}
