package com.example.gamequest.util

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hash de contraseña con salt y coste adaptativo. El formato permite migrar
 * hashes antiguos SHA-256 durante el siguiente inicio de sesión.
 */
object PasswordHasher {

    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    fun hash(password: String): String {
        val salt = ByteArray(SALT_LENGTH).also(SecureRandom()::nextBytes)
        val derived = derive(password, salt, ITERATIONS)
        return "pbkdf2-sha256\$$ITERATIONS\$${salt.toHex()}\$${derived.toHex()}"
    }

    fun matches(password: String, storedHash: String): Boolean {
        val parts = storedHash.split('$')
        if (parts.size == 4 && parts[0] == "pbkdf2-sha256") {
            val iterations = parts[1].toIntOrNull() ?: return false
            val salt = parts[2].fromHex() ?: return false
            val expected = parts[3].fromHex() ?: return false
            val actual = derive(password, salt, iterations)
            return MessageDigest.isEqual(actual, expected)
        }

        // Compatibilidad temporal con cuentas creadas antes de PBKDF2.
        val legacy = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray(Charsets.UTF_8)).toHex()
        return MessageDigest.isEqual(legacy.toByteArray(), storedHash.lowercase().toByteArray())
    }

    fun needsRehash(storedHash: String): Boolean = !storedHash.startsWith("pbkdf2-sha256$")

    private fun derive(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH)
        return try {
            SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.fromHex(): ByteArray? {
        if (length % 2 != 0 || any { it !in "0123456789abcdefABCDEF" }) return null
        return runCatching {
            chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        }.getOrNull()
    }
}
