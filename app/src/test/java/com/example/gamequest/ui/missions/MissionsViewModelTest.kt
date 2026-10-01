package com.example.gamequest.ui.missions

import com.example.gamequest.data.local.entity.Dificultad
import com.example.gamequest.data.local.entity.MisionEntity
import com.example.gamequest.data.local.entity.PuntoInteresEntity
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MissionsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeCampusRepository
    private lateinit var viewModel: MissionsViewModel

    private val puntoPrueba = PuntoInteresEntity(
        id = 1,
        nombre = "Biblioteca",
        categoria = "Académico",
        descripcion = "Libros",
        horarioAtencion = "8am - 6pm",
        tramites = "Préstamos",
        posX = 0.5f,
        posY = 0.5f,
        codigoQr = "CQ-BIB-001"
    )

    private val misionActiva = MisionConEstado(
        mision = MisionEntity(
            id = 101,
            titulo = "Buscar libro",
            descripcionPista = "Estante A",
            puntos = 30,
            tiempoEstimadoMin = 10,
            dificultad = Dificultad.BAJA,
            puntoInteresId = 1,
            insigniaNombre = "Lector",
            insigniaEmoji = "📖"
        ),
        punto = puntoPrueba,
        completada = false
    )

    private val misionCompletada = MisionConEstado(
        mision = MisionEntity(
            id = 102,
            titulo = "Visitar software",
            descripcionPista = "Bloque C",
            puntos = 50,
            tiempoEstimadoMin = 15,
            dificultad = Dificultad.MEDIA,
            puntoInteresId = 1,
            insigniaNombre = "Coder",
            insigniaEmoji = "💻"
        ),
        punto = puntoPrueba,
        completada = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCampusRepository(listOf(misionActiva, misionCompletada))
        viewModel = MissionsViewModel(fakeRepository, usuarioId = 1)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun filtroInicial_esActivas() {
        assertEquals(FiltroMisiones.ACTIVAS, viewModel.filtro.value)
    }

    @Test
    fun filtradoActivas_soloRetornaMisionesNoCompletadas() = runTest {
        backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.misionesFiltradas.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val activas = viewModel.misionesFiltradas.value
        assertEquals(1, activas.size)
        assertEquals(101, activas.first().mision.id)
        assertTrue(!activas.first().completada)
    }

    @Test
    fun filtradoCompletadas_soloRetornaMisionesCompletadas() = runTest {
        backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.misionesFiltradas.collect()
        }
        viewModel.onFiltroChange(FiltroMisiones.COMPLETADAS)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(FiltroMisiones.COMPLETADAS, viewModel.filtro.value)
        val completadas = viewModel.misionesFiltradas.value
        assertEquals(1, completadas.size)
        assertEquals(102, completadas.first().mision.id)
        assertTrue(completadas.first().completada)
    }

    @Test
    fun filtradoTodas_retornaTodasLasMisiones() = runTest {
        backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.misionesFiltradas.collect()
        }
        viewModel.onFiltroChange(FiltroMisiones.TODAS)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(FiltroMisiones.TODAS, viewModel.filtro.value)
        val todas = viewModel.misionesFiltradas.value
        assertEquals(2, todas.size)
    }

    @Test
    fun contadores_reflejanConteoCorrecto() = runTest {
        backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.contadorActivas.collect()
        }
        backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.contadorCompletadas.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.contadorActivas.value)
        assertEquals(1, viewModel.contadorCompletadas.value)
    }

    private class FakeCampusRepository(
        private val misionesConEstado: List<MisionConEstado>
    ) : CampusRepository() {
        override fun observarMisionesConEstado(usuarioId: Int): Flow<List<MisionConEstado>> =
            flowOf(misionesConEstado)
    }
}
