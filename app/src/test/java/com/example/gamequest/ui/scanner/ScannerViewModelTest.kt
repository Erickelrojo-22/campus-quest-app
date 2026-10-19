package com.example.gamequest.ui.scanner

import com.example.gamequest.data.local.entity.Dificultad
import com.example.gamequest.data.local.entity.MisionEntity
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import com.example.gamequest.data.repository.CampusRepository
import com.example.gamequest.data.repository.ValidacionQrResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScannerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeCampusRepository
    private lateinit var viewModel: ScannerViewModel

    private val punto = PuntoInteresEntity(
        id = 1,
        nombre = "Laboratorio",
        categoria = "Académico",
        descripcion = "Lab de cómputo",
        horarioAtencion = "8:00 - 16:00",
        tramites = "Prácticas",
        posX = 0.5f,
        posY = 0.5f,
        codigoQr = "CQ-LAB-001"
    )

    private val mision = MisionEntity(
        id = 1,
        titulo = "Visitar laboratorio",
        descripcionPista = "Entra al lab",
        puntos = 30,
        tiempoEstimadoMin = 10,
        dificultad = Dificultad.BAJA,
        puntoInteresId = 1,
        insigniaNombre = "Científico",
        insigniaEmoji = "🔬"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCampusRepository()
        viewModel = ScannerViewModel(fakeRepository, usuarioId = 1)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun dialogoManual_controlDeAperturaYCierre() {
        assertFalse(viewModel.mostrarDialogoManual.value)

        viewModel.abrirDialogoManual()
        assertTrue(viewModel.mostrarDialogoManual.value)

        viewModel.cerrarDialogoManual()
        assertFalse(viewModel.mostrarDialogoManual.value)
    }

    @Test
    fun onCodigoManualChange_actualizaValor() {
        viewModel.onCodigoManualChange("CQ-LAB-001")
        assertEquals("CQ-LAB-001", viewModel.codigoManual.value)
    }

    @Test
    fun validarCodigoManual_cierraDialogoYEvalua() = runTest {
        fakeRepository.resultadoAEntregar = ValidacionQrResult.MisionCompletada(
            mision = mision,
            punto = punto,
            puntosGanados = 30,
            nuevoPuntajeTotal = 150,
            nuevoNivel = 2,
            totalInsignias = 1
        )

        viewModel.abrirDialogoManual()
        viewModel.onCodigoManualChange("CQ-LAB-001")
        viewModel.validarCodigoManual()

        assertFalse(viewModel.mostrarDialogoManual.value)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ScannerUiState.Resultado)
        val res = (viewModel.uiState.value as ScannerUiState.Resultado).resultado
        assertTrue(res is ValidacionQrResult.MisionCompletada)
    }

    @Test
    fun validar_errorDelRepositorio_exponeMensajeRecuperable() = runTest {
        fakeRepository.errorAEntregar = IllegalStateException("El servidor rechazó el código.")

        viewModel.validar("CQ-LAB-001")
        testDispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.uiState.value
        assertTrue(estado is ScannerUiState.Error)
        assertEquals("El servidor rechazó el código.", (estado as ScannerUiState.Error).mensaje)
    }

    @Test
    fun reiniciar_restableceEstadoAEscaneando() {
        viewModel.validar("CQ-LAB-001")
        viewModel.reiniciar()
        assertTrue(viewModel.uiState.value is ScannerUiState.Escaneando)
    }

    private class FakeCampusRepository : CampusRepository() {
        var resultadoAEntregar: ValidacionQrResult = ValidacionQrResult.CodigoNoReconocido
        var errorAEntregar: Exception? = null

        override suspend fun validarCodigo(codigo: String, usuarioId: Int): ValidacionQrResult {
            errorAEntregar?.let { throw it }
            return resultadoAEntregar
        }
    }
}
