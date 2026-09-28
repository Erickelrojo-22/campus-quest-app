package com.example.gamequest.ui.auth

import com.example.gamequest.data.local.dao.UsuarioDao
import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.repository.AuthRepository
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
class RegisterViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDao: FakeUsuarioDao
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeDao = FakeUsuarioDao()
        authRepository = AuthRepository(fakeDao)
        viewModel = RegisterViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun registrar_fallaSiNombreEstaVacio() {
        viewModel.onNombresChange("")
        viewModel.onCorreoChange("estudiante@live.uleam.edu.ec")
        viewModel.onCarreraChange("Software")
        viewModel.onContrasenaChange("123456")
        viewModel.onConfirmarContrasenaChange("123456")

        var llamado = false
        viewModel.registrar { llamado = true }

        assertEquals("Escribe tus nombres completos.", viewModel.uiState.value.error)
        assertTrue(!llamado)
    }

    @Test
    fun registrar_fallaSiCorreoNoEsInstitucional() {
        viewModel.onNombresChange("Juan Perez")
        viewModel.onCorreoChange("juan@gmail.com")
        viewModel.onCarreraChange("Software")
        viewModel.onContrasenaChange("123456")
        viewModel.onConfirmarContrasenaChange("123456")

        var llamado = false
        viewModel.registrar { llamado = true }

        assertTrue(viewModel.uiState.value.error?.contains("institucional") == true)
        assertTrue(!llamado)
    }

    @Test
    fun registrar_fallaSiContrasenasNoCoinciden() {
        viewModel.onNombresChange("Juan Perez")
        viewModel.onCorreoChange("juan@live.uleam.edu.ec")
        viewModel.onCarreraChange("Software")
        viewModel.onContrasenaChange("123456")
        viewModel.onConfirmarContrasenaChange("654321")

        var llamado = false
        viewModel.registrar { llamado = true }

        assertEquals("Las contraseñas no coinciden.", viewModel.uiState.value.error)
        assertTrue(!llamado)
    }

    @Test
    fun registrar_exitosoConDatosValidos() {
        viewModel.onNombresChange("Erick Moreira")
        viewModel.onCorreoChange("e1351519127@live.uleam.edu.ec")
        viewModel.onCarreraChange("Ingeniería de Software")
        viewModel.onContrasenaChange("clave12345")
        viewModel.onConfirmarContrasenaChange("clave12345")

        var usuarioCreado: UsuarioEntity? = null
        viewModel.registrar { usuarioCreado = it }

        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(usuarioCreado)
        assertEquals("Erick Moreira", usuarioCreado?.nombres)
        assertNull(viewModel.uiState.value.error)
    }

    private class FakeUsuarioDao : UsuarioDao {
        private val usuarios = mutableListOf<UsuarioEntity>()
        private var nextId = 1

        override suspend fun insertar(usuario: UsuarioEntity): Long {
            val conId = usuario.copy(id = nextId++)
            usuarios.add(conId)
            return conId.id.toLong()
        }

        override suspend fun actualizar(usuario: UsuarioEntity) {}
        override suspend fun buscarPorCorreo(correo: String): UsuarioEntity? {
            return usuarios.find { it.correoInstitucional.equals(correo.trim(), ignoreCase = true) }
        }
        override suspend fun buscarPorNombre(nombre: String): UsuarioEntity? = null
        override suspend fun buscarPorId(id: Int): UsuarioEntity? = usuarios.find { it.id == id }
        override fun observarPorId(id: Int): Flow<UsuarioEntity?> = flowOf(usuarios.find { it.id == id })
        override fun observarRanking(): Flow<List<UsuarioEntity>> = flowOf(usuarios)
        override suspend fun sumarPuntos(usuarioId: Int, puntos: Int, nivel: Int) {}
    }
}
