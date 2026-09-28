package com.example.gamequest.data.repository

import com.example.gamequest.data.local.dao.UsuarioDao
import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.data.local.entity.UsuarioEntity

sealed class AuthResult {
    data class Exito(val usuario: UsuarioEntity) : AuthResult()
    data class Error(val mensaje: String) : AuthResult()
}

/**
 * Acceso simplificado: el usuario solo escribe su nombre para entrar.
 * Si el nombre ya existe se reutiliza la cuenta (con su progreso); si no, se crea una nueva.
 */
class AuthRepository(private val usuarioDao: UsuarioDao) {

    companion object {
        const val DOMINIO_INSTITUCIONAL = "@live.uleam.edu.ec"
        const val MIN_CONTRASENA_LENGTH = 6
    }

    fun validarCorreoInstitucional(correo: String): Boolean {
        val email = correo.trim().lowercase()
        return email.contains("@") && email.endsWith(DOMINIO_INSTITUCIONAL) && email.length > DOMINIO_INSTITUCIONAL.length
    }


    suspend fun entrarConNombre(nombre: String, esTutor: Boolean): AuthResult {
        val nombreNormalizado = nombre.trim()
        if (nombreNormalizado.isBlank()) return AuthResult.Error("Escribe tu nombre para continuar.")

        usuarioDao.buscarPorNombre(nombreNormalizado)?.let { return AuthResult.Exito(it) }

        val nuevo = UsuarioEntity(
            nombres = nombreNormalizado,
            correoInstitucional = "",
            carrera = "",
            contrasenaHash = "",
            rol = if (esTutor) Rol.TUTOR else Rol.ESTUDIANTE
        )
        val id = usuarioDao.insertar(nuevo)
        return AuthResult.Exito(nuevo.copy(id = id.toInt()))
    }
}
