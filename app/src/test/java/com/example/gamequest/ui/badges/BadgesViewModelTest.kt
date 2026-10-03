package com.example.gamequest.ui.badges

import com.example.gamequest.data.local.entity.Dificultad
import com.example.gamequest.data.local.entity.MisionEntity
import com.example.gamequest.data.local.entity.PuntoInteresEntity
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
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BadgesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeCampusRepository
    private lateinit var viewModel: BadgesViewModel

    private val usuarioPrueba = UsuarioEntity(
        id = 1,
        nombres = "Erick Moreira",
        correoInstitucional = "e1351519127@live.uleam.edu.ec",
        carrera = "Ingeniería de Software",
        contrasenaHash = "hash",
        puntajeAcumulado = 120,
        nivel = 2
    )

    private val mision1 = MisionConEstado(
        mision = MisionEntity(
            id = 1,
            titulo = "Conocer la Biblioteca",
            descripcionPista = "Entra a la biblioteca",
            puntos = 40,
            tiempoEstimadoMin = 15,
            dificultad = Dificultad.BAJA,
            puntoInteresId = 1,
            insigniaNombre = "Lector",
            insigniaEmoji = "📚"
        ),
        punto = PuntoInteresEntity(
            id = 1,
            nombre = "Biblioteca",
            categoria = "Académico",
            descripcion = "Edificio de libros",
            horarioAtencion = "8:00 - 18:00",
            tramites = "Préstamos",
            posX = 0.4f,
            posY = 0.5f,
            codigoQr = "CQ-BIB-001"
        ),
        completada = true
    )

    private val mision2 = MisionConEstado(
        mision = MisionEntity(
            id = 2,
            titulo = "Paso por el Coliseo",
            descripcionPista = "Visita la cancha",
            puntos = 50,
            tiempoEstimadoMin = 20,
            dificultad = Dificultad.MEDIA,
            puntoInteresId = 2,
            insigniaNombre = "Atleta",
            insigniaEmoji = "⚽"
        ),
        punto = PuntoInteresEntity(
            id = 2,
            nombre = "Coliseo Universitario",
            categoria = "Recreación",
            descripcion = "Canchas",
            horarioAtencion = "7:00 - 20:00",
            tramites = "Deportes",
            posX = 0.7f,
            posY = 0.3f,
            codigoQr = "CQ-COL-002"
        ),
        completada = false
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCampusRepository(
            usuario = usuarioPrueba,
            misionesConEstado = listOf(mision1, mision2),
            ranking = listOf(usuarioPrueba)
        )
        viewModel = BadgesViewModel(fakeRepository, usuarioId = 1)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun estadoInicial_clasificaObtenidasYPendientes() = runTest {
        backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.estado.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.estado.value
        assertEquals("Erick Moreira", estado.usuario?.nombres)
        assertEquals(1, estado.obtenidas.size)
        assertEquals("Lector", estado.obtenidas.first().mision.insigniaNombre)
        assertEquals(1, estado.pendientes.size)
        assertEquals("Atleta", estado.pendientes.first().mision.insigniaNombre)
        assertNull(estado.insigniaSeleccionada)
    }

    @Test
    fun seleccionarInsignia_actualizaInsigniaSeleccionada() = runTest {
        backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.estado.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.seleccionarInsignia(mision1)
        testDispatcher.scheduler.advanceUntilIdle()

        val estado = viewModel.estado.value
        assertNotNull(estado.insigniaSeleccionada)
        assertEquals("Lector", estado.insigniaSeleccionada?.mision?.insigniaNombre)
    }

    @Test
    fun cerrarDetalleInsignia_limpiaSeleccion() = runTest {
        backgroundScope.launch(kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)) {
            viewModel.estado.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.seleccionarInsignia(mision1)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.estado.value.insigniaSeleccionada)

        viewModel.cerrarDetalleInsignia()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNull(viewModel.estado.value.insigniaSeleccionada)
    }

    private class FakeCampusRepository(
        private val usuario: UsuarioEntity,
        private val misionesConEstado: List<MisionConEstado>,
        private val ranking: List<UsuarioEntity>
    ) : CampusRepository() {
        override fun observarUsuario(id: Int): Flow<UsuarioEntity?> = flowOf(usuario)
        override fun observarMisionesConEstado(usuarioId: Int): Flow<List<MisionConEstado>> =
            flowOf(misionesConEstado)
        override fun observarRanking(): Flow<List<UsuarioEntity>> = flowOf(ranking)
    }
}
