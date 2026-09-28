package com.example.gamequest.util

import java.security.MessageDigest

/**
 * Hash unidireccional simple para contraseñas (requisito no funcional de
 * seguridad: "las contraseñas no se almacenan en texto plano").
 */
object PasswordHasher {

    fun hash(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun matches(password: String, hash: String): Boolean = hash(password) == hash
}
