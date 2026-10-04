package com.example.gamequest.data.repository

import com.example.gamequest.data.local.dao.UsuarioDao
import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.util.PasswordHasher
import com.example.gamequest.data.remote.CampusApi
import kotlinx.coroutines.CancellationException
import org.json.JSONObject


sealed class AuthResult {
    data class Exito(val usuario: UsuarioEntity) : AuthResult()
    data class Error(val mensaje: String) : AuthResult()
}

/**
 * Acceso simplificado: el usuario solo escribe su nombre para entrar.
 * Si el nombre ya existe se reutiliza la cuenta (con su progreso); si no, se crea una nueva.
 */
class AuthRepository(private val usuarioDao: UsuarioDao, private val api: CampusApi? = null) {

    val minimumPasswordLength: Int get() = if (api != null) 8 else MIN_CONTRASENA_LENGTH

    private suspend fun remote(path: String, body: JSONObject): AuthResult = try {
        val user = api!!.authenticate(path, body)
        usuarioDao.guardarRemotos(listOf(user))
        AuthResult.Exito(user)
    } catch (e: CancellationException) { throw e
    } catch (e: Exception) { AuthResult.Error(e.message ?: "No se pudo conectar con el servidor.") }

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
        if (api != null) return remote("register", JSONObject().put("nombres", nombres.trim())
            .put("correo", correo.trim().lowercase()).put("carrera", carrera.trim()).put("contrasena", contrasena))
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
            // El rol tutor no se concede desde un formulario público.
            rol = Rol.ESTUDIANTE
        )
        val id = usuarioDao.insertar(nuevoUsuario)
        return AuthResult.Exito(nuevoUsuario.copy(id = id.toInt()))
    }

    suspend fun iniciarSesionConCredenciales(correo: String, contrasena: String): AuthResult {
        if (api != null) return remote("login", JSONObject().put("correo", correo.trim().lowercase()).put("contrasena", contrasena))
        val mail = correo.trim().lowercase()
        val pass = contrasena.trim()

        if (mail.isBlank()) return AuthResult.Error("Ingresa tu correo institucional.")
        if (pass.isBlank()) return AuthResult.Error("Ingresa tu contraseña.")

        val usuario = usuarioDao.buscarPorCorreo(mail)
            ?: return AuthResult.Error("No existe una cuenta registrada con este correo.")

        if (usuario.contrasenaHash.isBlank()) {
            return AuthResult.Error("Esta cuenta debe completar su registro institucional.")
        }
        if (!PasswordHasher.matches(pass, usuario.contrasenaHash)) {
            return AuthResult.Error("Contraseña incorrecta.")
        }
        if (PasswordHasher.needsRehash(usuario.contrasenaHash)) {
            usuarioDao.actualizarContrasena(usuario.id, PasswordHasher.hash(pass))
        }

        return AuthResult.Exito(usuario)
    }




    suspend fun entrarConNombre(nombre: String, esTutor: Boolean): AuthResult {
        if (api != null) return remote("visitor", JSONObject().put("nombres", nombre.trim()))
        val nombreNormalizado = nombre.trim()
        if (nombreNormalizado.isBlank()) return AuthResult.Error("Escribe tu nombre para continuar.")

        usuarioDao.buscarPorNombre(nombreNormalizado)?.let {
            if (it.rol == Rol.TUTOR) {
                return AuthResult.Error("Las cuentas tutor deben entrar con correo y contraseña.")
            }
            return AuthResult.Exito(it)
        }

        val nuevo = UsuarioEntity(
            nombres = nombreNormalizado,
            correoInstitucional = "",
            carrera = "",
            contrasenaHash = "",
            rol = Rol.ESTUDIANTE
        )
        val id = usuarioDao.insertar(nuevo)
        return AuthResult.Exito(nuevo.copy(id = id.toInt()))
    }
}
