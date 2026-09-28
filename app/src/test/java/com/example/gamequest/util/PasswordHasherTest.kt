package com.example.gamequest.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

/**
 * Pruebas unitarias para la utilidad de hash y verificación de contraseñas.
 * Garantiza el cumplimiento del requisito de seguridad (RF no funcional).
 */
class PasswordHasherTest {

    @Test
    fun hash_incluyeAlgoritmoSaltYCoste() {
        val hash = PasswordHasher.hash("ClaveSegura2026")
        assertEquals(4, hash.split('$').size)
        assertTrue(hash.startsWith("pbkdf2-sha256$"))
    }

    @Test
    fun hash_usaSaltAleatorio() {
        val hash1 = PasswordHasher.hash("MiContrasena123")
        val hash2 = PasswordHasher.hash("MiContrasena123")
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun hash_generaDiferentesHashesParaDiferentesEntradas() {
        val hash1 = PasswordHasher.hash("PasswordA")
        val hash2 = PasswordHasher.hash("PasswordB")
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun matches_retornaTrueCuandoLaContrasenaCoincide() {
        val raw = "SuperSecretPass"
        val hashed = PasswordHasher.hash(raw)
        assertTrue(PasswordHasher.matches(raw, hashed))
    }

    @Test
    fun matches_retornaFalseCuandoLaContrasenaNoCoincide() {
        val raw = "SuperSecretPass"
        val hashed = PasswordHasher.hash(raw)
        assertFalse(PasswordHasher.matches("OtraContrasena", hashed))
    }

    @Test
    fun matches_aceptaHashLegacyParaPermitirMigracion() {
        val legacy = MessageDigest.getInstance("SHA-256")
            .digest("ClaveSegura2026".toByteArray())
            .joinToString("") { "%02x".format(it) }
        assertTrue(PasswordHasher.matches("ClaveSegura2026", legacy))
        assertTrue(PasswordHasher.needsRehash(legacy))
    }
}
