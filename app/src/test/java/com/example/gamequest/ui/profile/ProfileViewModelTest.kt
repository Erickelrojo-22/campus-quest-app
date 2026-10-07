package com.example.gamequest.ui.profile

import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.preferences.UserPreferences
import com.example.gamequest.data.preferences.UserPreferencesRepository
import com.example.gamequest.data.repository.CampusRepository
import com.example.gamequest.data.repository.ProgresoDetallado
import com.example.gamequest.ui.components.CharacterAnimation
import com.example.gamequest.util.AvatarColor
import com.example.gamequest.util.CharacterSpecies
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class ProfileViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeCampusRepository: FakeCampusRepository
    private lateinit var fakePreferencesRepository: FakeUserPreferencesRepository
    private lateinit var viewModel: ProfileViewModel

    private val usuarioPrueba = UsuarioEntity(
        id = 1,
        nombres = "Erick Moreira",
        correoInstitucional = "e1351519127@live.uleam.edu.ec",
        contrasenaHash = "hash",
        rol = Rol.ESTUDIANTE,
        puntajeAcumulado = 350,
        nivel = 3,
        carrera = "Ingeniería de Software"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeCampusRepository = FakeCampusRepository(usuarioPrueba, totalInsignias = 5)
        fakePreferencesRepository = FakeUserPreferencesRepository(
            UserPreferences(
                characterSpecies = CharacterSpecies.OWLET,
                avatarPrimaryColor = AvatarColor.EMERALD,
                avatarSecondaryColor = AvatarColor.AMBER
            )
        )
        viewModel = ProfileViewModel(
            campusRepository = fakeCampusRepository,
            preferencesRepository = fakePreferencesRepository,
            usuarioId = 1
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_inicializaConDatosDeRepositoriosYPreferencias() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Erick Moreira", state.usuario?.nombres)
        assertEquals(5, state.totalInsignias)
        assertEquals(CharacterSpecies.OWLET, state.selectedSpecies)
        assertEquals(AvatarColor.EMERALD, state.primaryColor)
        assertEquals(AvatarColor.AMBER, state.secondaryColor)
        assertEquals(CharacterAnimation.IDLE, state.spriteAnim)
        assertFalse(state.showEditor)
        assertFalse(state.showTimeline)
        assertFalse(state.showAdventurerCard)
        assertFalse(state.showOnboarding)
    }

    @Test
    fun toggleSpriteAnim_alternaEntreIdleYWalk() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CharacterAnimation.IDLE, viewModel.uiState.value.spriteAnim)

        viewModel.toggleSpriteAnim()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(CharacterAnimation.WALK, viewModel.uiState.value.spriteAnim)

        viewModel.toggleSpriteAnim()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(CharacterAnimation.IDLE, viewModel.uiState.value.spriteAnim)
    }

    @Test
    fun abrirEditor_inicializaDraftConPreferenciasActuales() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.abrirEditor()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.showEditor)
        assertEquals(CharacterSpecies.OWLET, state.editorSpecies)
        assertEquals(AvatarColor.EMERALD, state.editorPrimary)
        assertEquals(AvatarColor.AMBER, state.editorSecondary)
        assertEquals(AvatarColorZone.PRIMARY, state.editorActiveZone)

        viewModel.cerrarEditor()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showEditor)
    }

    @Test
    fun editarAvatarYGuardar_actualizaPreferenciasYPersiste() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.abrirEditor()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onEditorSpeciesChange(CharacterSpecies.DRAKE)
        viewModel.onEditorActiveZoneChange(AvatarColorZone.PRIMARY)
        viewModel.onEditorColorSelected(AvatarColor.CRIMSON)
        viewModel.onEditorActiveZoneChange(AvatarColorZone.SECONDARY)
        viewModel.onEditorColorSelected(AvatarColor.OBSIDIAN)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CharacterSpecies.DRAKE, viewModel.uiState.value.editorSpecies)
        assertEquals(AvatarColor.CRIMSON, viewModel.uiState.value.editorPrimary)
        assertEquals(AvatarColor.OBSIDIAN, viewModel.uiState.value.editorSecondary)

        viewModel.guardarAvatar()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showEditor)
        assertEquals(CharacterSpecies.DRAKE, fakePreferencesRepository.preferenciasState.value.characterSpecies)
        assertEquals(AvatarColor.CRIMSON, fakePreferencesRepository.preferenciasState.value.avatarPrimaryColor)
        assertEquals(AvatarColor.OBSIDIAN, fakePreferencesRepository.preferenciasState.value.avatarSecondaryColor)
    }

    @Test
    fun restablecerEditor_revierteCambiosNoGuardados() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.abrirEditor()
        viewModel.onEditorSpeciesChange(CharacterSpecies.PINK)
        viewModel.onEditorColorSelected(AvatarColor.ICE_BLUE)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CharacterSpecies.PINK, viewModel.uiState.value.editorSpecies)

        viewModel.restablecerEditor()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(CharacterSpecies.OWLET, viewModel.uiState.value.editorSpecies)
        assertEquals(AvatarColor.EMERALD, viewModel.uiState.value.editorPrimary)
    }

    @Test
    fun timelineDialog_abreYCierraCorrectamente() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showTimeline)

        viewModel.abrirTimeline()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showTimeline)

        viewModel.cerrarTimeline()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showTimeline)
    }

    @Test
    fun adventurerCardDialog_abreYCierraCorrectamente() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showAdventurerCard)

        viewModel.abrirAdventurerCard()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showAdventurerCard)

        viewModel.cerrarAdventurerCard()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showAdventurerCard)
    }

    @Test
    fun onboardingDialog_abreYCierraCorrectamente() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showOnboarding)

        viewModel.abrirOnboarding()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showOnboarding)

        viewModel.cerrarOnboarding()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showOnboarding)
    }

    @Test
    fun rankingDialog_abreYCierraCorrectamente() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showRanking)

        viewModel.abrirRanking()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.showRanking)

        viewModel.cerrarRanking()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.showRanking)
    }

    private class FakeCampusRepository(
        private val usuario: UsuarioEntity,
        private val totalInsignias: Int
    ) : CampusRepository() {
        override fun observarUsuario(id: Int): Flow<UsuarioEntity?> = MutableStateFlow(usuario)
        override fun observarTotalInsignias(usuarioId: Int): Flow<Int> = MutableStateFlow(totalInsignias)
        override fun observarHistorialProgreso(usuarioId: Int): Flow<List<ProgresoDetallado>> =
            MutableStateFlow(emptyList())
    }

    private class FakeUserPreferencesRepository(
        initialPrefs: UserPreferences
    ) : UserPreferencesRepository() {
        val preferenciasState = MutableStateFlow(initialPrefs)
        override val preferencias: Flow<UserPreferences> = preferenciasState

        override suspend fun setCharacterSpecies(species: CharacterSpecies) {
            preferenciasState.value = preferenciasState.value.copy(characterSpecies = species)
        }

        override suspend fun setAvatarPrimaryColor(color: AvatarColor) {
            preferenciasState.value = preferenciasState.value.copy(avatarPrimaryColor = color)
        }

        override suspend fun setAvatarSecondaryColor(color: AvatarColor) {
            preferenciasState.value = preferenciasState.value.copy(avatarSecondaryColor = color)
        }
    }
}
