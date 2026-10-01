package com.example.gamequest.ui.home

import com.example.gamequest.data.local.entity.Dificultad
import com.example.gamequest.data.local.entity.MisionEntity
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.repository.CampusRepository
import com.example.gamequest.data.repository.MisionConEstado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeCampusRepository
    private lateinit var viewModel: HomeViewModel

    private val usuarioPrueba = UsuarioEntity(
        id = 1,
        nombres = "Erick Moreira",
        correoInstitucional = "e1351519127@live.uleam.edu.ec",
        contrasenaHash = "hash",
        rol = Rol.ESTUDIANTE,
        puntajeAcumulado = 120,
        nivel = 2,
        carrera = "Software"
    )

    private val puntoBiblio = PuntoInteresEntity(
        id = 1,
        nombre = "Biblioteca Central",
        categoria = "Académico",
        descripcion = "Libros y estudio",
        horarioAtencion = "8am - 6pm",
        tramites = "Préstamos",
        posX = 0.5f,
        posY = 0.5f,
        codigoQr = "CQ-BIB-001"
    )

    private val puntoCafeteria = PuntoInteresEntity(
        id = 2,
        nombre = "Cafetería Central",
        categoria = "Servicios",
        descripcion = "Comida",
        horarioAtencion = "7am - 5pm",
        tramites = "Snacks",
        posX = 0.4f,
        posY = 0.4f,
        codigoQr = "CQ-CAF-002"
    )

    private val misionBiblio = MisionConEstado(
        mision = MisionEntity(
            id = 101,
            titulo = "Encuentra la biblioteca",
            descripcionPista = "Bloque B",
            puntos = 50,
            tiempoEstimadoMin = 15,
            dificultad = Dificultad.MEDIA,
            puntoInteresId = 1,
            insigniaNombre = "Lector",
            insigniaEmoji = "📚"
        ),
        punto = puntoBiblio,
        completada = false
    )

    private val misionCafeteria = MisionConEstado(
        mision = MisionEntity(
            id = 102,
            titulo = "Descubre la cafetería",
            descripcionPista = "Plaza central",
            puntos = 20,
            tiempoEstimadoMin = 5,
            dificultad = Dificultad.BAJA,
            puntoInteresId = 2,
            insigniaNombre = "Comensal",
            insigniaEmoji = "☕"
        ),
        punto = puntoCafeteria,
        completada = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCampusRepository(
            usuario = usuarioPrueba,
            misionesConEstado = listOf(misionBiblio, misionCafeteria)
        )
        viewModel = HomeViewModel(fakeRepository, usuarioId = 1)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun busquedaVacia_resultadosBusquedaEstaVacia() = runTest {
        backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.resultadosBusqueda.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.resultadosBusqueda.value.isEmpty())
    }

    @Test
    fun busquedaPorTexto_filtraPorTituloOLugar() = runTest {
        backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.resultadosBusqueda.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onBusquedaChange("biblioteca")
        testDispatcher.scheduler.advanceUntilIdle()

        val resultados = viewModel.resultadosBusqueda.value
        assertEquals(1, resultados.size)
        assertEquals("Encuentra la biblioteca", resultados.first().mision.titulo)
    }

    @Test
    fun busquedaPorCategoria_filtraPorServicios() = runTest {
        backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.resultadosBusqueda.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onBusquedaChange("Servicios")
        testDispatcher.scheduler.advanceUntilIdle()

        val resultados = viewModel.resultadosBusqueda.value
        assertEquals(1, resultados.size)
        assertEquals("Cafetería Central", resultados.first().punto.nombre)
    }

    @Test
    fun misionSugerida_retornaPrimeraMisionIncompleta() = runTest {
        backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.misionSugerida.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val sugerida = viewModel.misionSugerida.value
        assertNotNull(sugerida)
        assertEquals(101, sugerida?.mision?.id)
        assertTrue(sugerida?.completada == false)
    }

    private class FakeCampusRepository(
        private val usuario: UsuarioEntity,
        private val misionesConEstado: List<MisionConEstado>
    ) : CampusRepository() {
        override fun observarUsuario(id: Int): Flow<UsuarioEntity?> = flowOf(usuario)
        override fun observarMisionesConEstado(usuarioId: Int): Flow<List<MisionConEstado>> =
            flowOf(misionesConEstado)
    }
}
