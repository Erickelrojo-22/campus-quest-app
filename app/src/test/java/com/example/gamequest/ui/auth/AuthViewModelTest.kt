package com.example.gamequest.ui.auth

import com.example.gamequest.data.local.dao.UsuarioDao
import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.repository.AuthRepository
import com.example.gamequest.util.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeUsuarioDao
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeUsuarioDao()
        authRepository = AuthRepository(fakeDao)
        viewModel = AuthViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onModoInstitucionalChange_actualizaEstadoYBorraError() {
        viewModel.onModoInstitucionalChange(true)
        assertTrue(viewModel.uiState.value.modoInstitucional)

        viewModel.onModoInstitucionalChange(false)
        assertTrue(!viewModel.uiState.value.modoInstitucional)
    }

    @Test
    fun onInputsChange_actualizaValoresEnUiState() {
        viewModel.onNombreChange("Erick Moreira")
        viewModel.onCorreoChange("e1351519127@live.uleam.edu.ec")
        viewModel.onContrasenaChange("password123")

        val state = viewModel.uiState.value
        assertEquals("Erick Moreira", state.nombre)
        assertEquals("e1351519127@live.uleam.edu.ec", state.correo)
        assertEquals("password123", state.contrasena)
    }

    @Test
    fun entrarRapido_conNombreValido_iniciaSesion() {
        viewModel.onModoInstitucionalChange(false)
        viewModel.onNombreChange("Aventurero Uleam")

        var usuarioResult: UsuarioEntity? = null
        viewModel.iniciarSesion { usuarioResult = it }

        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(usuarioResult)
        assertEquals("Aventurero Uleam", usuarioResult?.nombres)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun entrarInstitucional_conCredencialesValidas_iniciaSesion() {
        val hash = PasswordHasher.hash("secreto123")
        fakeDao.insertarDirecto(
            UsuarioEntity(
                id = 10,
                nombres = "Estudiante Uleam",
                correoInstitucional = "estudiante@live.uleam.edu.ec",
                carrera = "Ingeniería de Software",
                contrasenaHash = hash,
                rol = Rol.ESTUDIANTE
            )
        )

        viewModel.onModoInstitucionalChange(true)
        viewModel.onCorreoChange("estudiante@live.uleam.edu.ec")
        viewModel.onContrasenaChange("secreto123")

        var usuarioResult: UsuarioEntity? = null
        viewModel.iniciarSesion { usuarioResult = it }

        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(usuarioResult)
        assertEquals(10, usuarioResult?.id)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun entrarInstitucional_credencialesIncorrectas_muestraError() {
        viewModel.onModoInstitucionalChange(true)
        viewModel.onCorreoChange("noexiste@live.uleam.edu.ec")
        viewModel.onContrasenaChange("badpass")

        var usuarioResult: UsuarioEntity? = null
        viewModel.iniciarSesion { usuarioResult = it }

        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(usuarioResult)
        assertNotNull(viewModel.uiState.value.error)
    }

    private class FakeUsuarioDao : UsuarioDao {
        override suspend fun guardarRemotos(items: List<UsuarioEntity>) {
            items.forEach { item ->
                usuarios.removeAll { it.id == item.id }
                usuarios.add(item)
            }
        }

        private val usuarios = mutableListOf<UsuarioEntity>()
        private var nextId = 1

        fun insertarDirecto(usuario: UsuarioEntity) {
            usuarios.add(usuario)
        }

        override suspend fun insertar(usuario: UsuarioEntity): Long {
            val conId = usuario.copy(id = nextId++)
            usuarios.add(conId)
            return conId.id.toLong()
        }

        override suspend fun actualizar(usuario: UsuarioEntity) {}
        override suspend fun actualizarContrasena(usuarioId: Int, hash: String) {}
        override suspend fun buscarPorCorreo(correo: String): UsuarioEntity? {
            return usuarios.find { it.correoInstitucional.equals(correo.trim(), ignoreCase = true) }
        }
        override suspend fun buscarPorNombre(nombre: String): UsuarioEntity? {
            return usuarios.find { it.nombres.equals(nombre.trim(), ignoreCase = true) }
        }
        override suspend fun buscarPorId(id: Int): UsuarioEntity? = usuarios.find { it.id == id }
        override fun observarPorId(id: Int): Flow<UsuarioEntity?> = flowOf(usuarios.find { it.id == id })
        override fun observarRanking(): Flow<List<UsuarioEntity>> = flowOf(usuarios)
        override suspend fun sumarPuntos(usuarioId: Int, puntos: Int, nivel: Int) {}
    }
}
