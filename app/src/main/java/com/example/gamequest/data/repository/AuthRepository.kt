package com.example.gamequest.data.repository

import com.example.gamequest.data.local.entity.UsuarioEntity

sealed class AuthResult {
    data class Exito(val usuario: UsuarioEntity) : AuthResult()
    data class Error(val mensaje: String) : AuthResult()
}

/**
 * Contrato de autenticación que consumen los ViewModels de acceso y registro.
 * La implementación ([DefaultAuthRepository]) decide si valida contra Room o
 * contra la API y ejecuta el trabajo fuera del hilo principal.
 */
interface AuthRepository {
    val minimumPasswordLength: Int

    fun validarCorreoInstitucional(correo: String): Boolean

    suspend fun registrar(
        nombres: String,
        correo: String,
        carrera: String,
        contrasena: String,
        esTutor: Boolean = false
    ): AuthResult

    suspend fun iniciarSesionConCredenciales(correo: String, contrasena: String): AuthResult

    suspend fun entrarConNombre(nombre: String, esTutor: Boolean): AuthResult

    companion object {
        const val DOMINIO_INSTITUCIONAL = "@live.uleam.edu.ec"
        const val MIN_CONTRASENA_LENGTH = 6
    }
}
