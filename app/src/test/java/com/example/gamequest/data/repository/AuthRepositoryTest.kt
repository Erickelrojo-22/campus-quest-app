package com.example.gamequest.data.repository

import com.example.gamequest.data.local.dao.UsuarioDao
import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.util.PasswordHasher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pruebas unitarias para AuthRepository validando las reglas de negocio
 * del proceso de registro y autenticación (RF-02 y RF-03).
 */
class AuthRepositoryTest {

    private lateinit var fakeUsuarioDao: FakeUsuarioDao
    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        fakeUsuarioDao = FakeUsuarioDao()
        repository = AuthRepository(fakeUsuarioDao)
    }

    @Test
    fun validarCorreoInstitucional_aceptaDominioOficial() {
        assertTrue(repository.validarCorreoInstitucional("e1351519127@live.uleam.edu.ec"))
        assertTrue(repository.validarCorreoInstitucional("estudiante.software@live.uleam.edu.ec"))
    }

    @Test
    fun validarCorreoInstitucional_rechazaDominioExterno() {
        assertFalse(repository.validarCorreoInstitucional("estudiante@gmail.com"))
        assertFalse(repository.validarCorreoInstitucional("estudiante@hotmail.com"))
        assertFalse(repository.validarCorreoInstitucional("sinarroba.live.uleam.edu.ec"))
    }

    @Test
    fun registrar_exitosoConCredencialesValidas() = runBlocking {
        val resultado = repository.registrar(
            nombres = "Erick Moreira",
            correo = "e1351519127@live.uleam.edu.ec",
            carrera = "Software",
            contrasena = "clave12345",
            esTutor = false
        )

        assertTrue(resultado is AuthResult.Exito)
        val usuario = (resultado as AuthResult.Exito).usuario
        assertEquals("Erick Moreira", usuario.nombres)
        assertEquals("e1351519127@live.uleam.edu.ec", usuario.correoInstitucional)
        assertTrue(PasswordHasher.matches("clave12345", usuario.contrasenaHash))
        assertEquals(Rol.ESTUDIANTE, usuario.rol)
    }

    @Test
    fun registrar_rechazaContrasenaDemasiadoCorta() = runBlocking {
        val resultado = repository.registrar(
            nombres = "Erick Moreira",
            correo = "e1351519127@live.uleam.edu.ec",
            carrera = "Software",
            contrasena = "123",
            esTutor = false
        )

        assertTrue(resultado is AuthResult.Error)
        assertTrue((resultado as AuthResult.Error).mensaje.contains("al menos 6 caracteres"))
    }

    @Test
    fun registrar_rechazaCorreoDuplicado() = runBlocking {
        repository.registrar(
            nombres = "Primer Usuario",
            correo = "e1351519127@live.uleam.edu.ec",
            carrera = "Software",
            contrasena = "clave12345"
        )

        val resultadoDuplicado = repository.registrar(
            nombres = "Segundo Usuario",
            correo = "e1351519127@live.uleam.edu.ec",
            carrera = "Software",
            contrasena = "otraClave99"
        )

        assertTrue(resultadoDuplicado is AuthResult.Error)
        assertTrue((resultadoDuplicado as AuthResult.Error).mensaje.contains("Ya existe una cuenta"))
    }

    @Test
    fun iniciarSesionConCredenciales_exitosoConPasswordCorrecto() = runBlocking {
        repository.registrar(
            nombres = "Erick Moreira",
            correo = "e1351519127@live.uleam.edu.ec",
            carrera = "Software",
            contrasena = "claveSegura2026"
        )

        val login = repository.iniciarSesionConCredenciales("e1351519127@live.uleam.edu.ec", "claveSegura2026")
        assertTrue(login is AuthResult.Exito)
        assertEquals("Erick Moreira", (login as AuthResult.Exito).usuario.nombres)
    }

    @Test
    fun iniciarSesionConCredenciales_fallaConPasswordIncorrecto() = runBlocking {
        repository.registrar(
            nombres = "Erick Moreira",
            correo = "e1351519127@live.uleam.edu.ec",
            carrera = "Software",
            contrasena = "claveSegura2026"
        )

        val login = repository.iniciarSesionConCredenciales("e1351519127@live.uleam.edu.ec", "claveEquivocada")
        assertTrue(login is AuthResult.Error)
        assertEquals("Contraseña incorrecta.", (login as AuthResult.Error).mensaje)
    }

    @Test
    fun iniciarSesionConCredenciales_rechazaCorreoFueraDelDominio() = runBlocking {
        val login = repository.iniciarSesionConCredenciales("alguien@gmail.com", "clave12345")
        assertTrue(login is AuthResult.Error)
        assertTrue((login as AuthResult.Error).mensaje.contains("correo institucional válido"))
    }

    @Test
    fun entrarConNombre_creaYReutilizaPerfilDePrueba() = runBlocking {
        val primero = repository.entrarConNombre("Tester Estudiante", false)
        val segundo = repository.entrarConNombre("Tester Estudiante", false)

        assertTrue(primero is AuthResult.Exito)
        assertTrue(segundo is AuthResult.Exito)
        assertEquals(
            (primero as AuthResult.Exito).usuario.id,
            (segundo as AuthResult.Exito).usuario.id
        )
    }

    @Test
    fun entrarConNombre_noAbreCuentaEstudiantilRegistradaSinContrasena() = runBlocking {
        repository.registrar(
            nombres = "Erick Moreira",
            correo = "e1351519127@live.uleam.edu.ec",
            carrera = "Software",
            contrasena = "claveSegura2026"
        )

        val intento = repository.entrarConNombre("Erick Moreira", false)

        assertTrue(intento is AuthResult.Error)
        assertTrue((intento as AuthResult.Error).mensaje.contains("cuenta estudiantil"))
    }

    private class FakeUsuarioDao : UsuarioDao {
        private val usuarios = mutableListOf<UsuarioEntity>()
        private var nextId = 1

        override suspend fun insertar(usuario: UsuarioEntity): Long {
            val conId = usuario.copy(id = nextId++)
            usuarios.add(conId)
            return conId.id.toLong()
        }

        override suspend fun actualizar(usuario: UsuarioEntity) {
            val idx = usuarios.indexOfFirst { it.id == usuario.id }
            if (idx != -1) usuarios[idx] = usuario
        }

        override suspend fun actualizarContrasena(usuarioId: Int, hash: String) {
            val usuario = usuarios.find { it.id == usuarioId } ?: return
            actualizar(usuario.copy(contrasenaHash = hash))
        }

        override suspend fun buscarPorCorreo(correo: String): UsuarioEntity? {
            return usuarios.find { it.correoInstitucional.equals(correo.trim(), ignoreCase = true) }
        }

        override suspend fun buscarPorNombre(nombre: String): UsuarioEntity? {
            return usuarios.find { it.nombres.equals(nombre.trim(), ignoreCase = true) }
        }

        override suspend fun buscarPorId(id: Int): UsuarioEntity? {
            return usuarios.find { it.id == id }
        }

        override fun observarPorId(id: Int): Flow<UsuarioEntity?> {
            return flowOf(usuarios.find { it.id == id })
        }

        override fun observarRanking(): Flow<List<UsuarioEntity>> {
            return flowOf(usuarios.sortedByDescending { it.puntajeAcumulado })
        }

        override suspend fun sumarPuntos(usuarioId: Int, puntos: Int, nivel: Int) {
            val u = usuarios.find { it.id == usuarioId }
            if (u != null) {
                actualizar(u.copy(puntajeAcumulado = u.puntajeAcumulado + puntos, nivel = nivel))
            }
        }
    }
}
