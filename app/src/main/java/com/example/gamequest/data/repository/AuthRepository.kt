package com.example.gamequest.data.repository

import com.example.gamequest.data.local.dao.UsuarioDao
import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.util.PasswordHasher


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

    suspend fun registrar(
        nombres: String,
        correo: String,
        carrera: String,
        contrasena: String,
        esTutor: Boolean = false
    ): AuthResult {
        val nom = nombres.trim()
        val mail = correo.trim().lowercase()
        val carr = carrera.trim()
        val pass = contrasena.trim()

        if (nom.isBlank()) return AuthResult.Error("El nombre no puede estar vacío.")
        if (!validarCorreoInstitucional(mail)) {
            return AuthResult.Error("Debe ingresar un correo institucional válido ($DOMINIO_INSTITUCIONAL).")
        }
        if (carr.isBlank()) return AuthResult.Error("Debes especificar tu carrera.")
        if (pass.length < MIN_CONTRASENA_LENGTH) {
            return AuthResult.Error("La contraseña debe tener al menos $MIN_CONTRASENA_LENGTH caracteres.")
        }

        val existente = usuarioDao.buscarPorCorreo(mail)
        if (existente != null) {
            return AuthResult.Error("Ya existe una cuenta registrada con este correo.")
        }

        val hash = PasswordHasher.hash(pass)
        val nuevoUsuario = UsuarioEntity(
            nombres = nom,
            correoInstitucional = mail,
            carrera = carr,
            contrasenaHash = hash,
            rol = if (esTutor) Rol.TUTOR else Rol.ESTUDIANTE
        )
        val id = usuarioDao.insertar(nuevoUsuario)
        return AuthResult.Exito(nuevoUsuario.copy(id = id.toInt()))
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
