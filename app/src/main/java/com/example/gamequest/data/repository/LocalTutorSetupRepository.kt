package com.example.gamequest.data.repository

import androidx.room.withTransaction
import com.example.gamequest.data.local.AppDatabase
import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.session.LocalAccountSessionCommitter
import com.example.gamequest.util.PasswordHasher
import kotlinx.coroutines.flow.first

/** A local install can bootstrap exactly one tutor; public registration cannot elevate roles. */
class LocalTutorSetupRepository(
    private val database: AppDatabase,
    private val auth: AuthRepository,
    private val session: LocalAccountSessionCommitter
) : TutorSetupRepository {
    override suspend fun isSetupRequired(): Boolean = buscarTutor() == null

    override suspend fun configureTutor(nombres: String, correo: String, carrera: String, contrasena: String): AuthResult {
        val name = nombres.trim()
        val email = correo.trim().lowercase()
        val degree = carrera.trim()
        val password = contrasena.trim()
        if (name.isBlank() || degree.isBlank()) return AuthResult.Error("Completa los datos del tutor.")
        if (!auth.validarCorreoInstitucional(email)) return AuthResult.Error("Ingresa un correo institucional válido.")
        if (password.length < auth.minimumPasswordLength) return AuthResult.Error("La contraseña debe tener al menos ${auth.minimumPasswordLength} caracteres.")
        val user = try {
            database.withTransaction {
                check(buscarTutor() == null) { "El tutor local ya fue configurado." }
                check(database.usuarioDao().buscarPorCorreo(email) == null) { "Ya existe una cuenta con este correo." }
                val draft = UsuarioEntity(nombres = name, correoInstitucional = email, carrera = degree,
                    contrasenaHash = PasswordHasher.hash(password), rol = Rol.TUTOR)
                draft.copy(id = database.usuarioDao().insertar(draft).toInt())
            }
        } catch (error: IllegalStateException) {
            return AuthResult.Error(error.message ?: "No se pudo configurar el tutor.")
        }
        session.commitLocalAccount(user.id)
        return AuthResult.Exito(user)
    }

    private suspend fun buscarTutor() = database.usuarioDao().observarRanking().first().firstOrNull { it.rol == Rol.TUTOR }
}
