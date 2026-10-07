package com.example.gamequest.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.preferences.UserPreferencesRepository
import com.example.gamequest.data.repository.CampusRepository
import com.example.gamequest.data.repository.ProgresoDetallado
import com.example.gamequest.ui.components.CharacterAnimation
import com.example.gamequest.util.AvatarColor
import com.example.gamequest.util.CharacterSpecies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AvatarColorZone { PRIMARY, SECONDARY }

data class ProfileUiState(
    val usuario: UsuarioEntity? = null,
    val totalInsignias: Int = 0,
    val selectedSpecies: CharacterSpecies = CharacterSpecies.DUDE,
    val primaryColor: AvatarColor = AvatarColor.COBALT_BLUE,
    val secondaryColor: AvatarColor = AvatarColor.RUBY_RED,
    val spriteAnim: CharacterAnimation = CharacterAnimation.IDLE,
    val showEditor: Boolean = false,
    val showTimeline: Boolean = false,
    val showAdventurerCard: Boolean = false,
    val showOnboarding: Boolean = false,
    val showRanking: Boolean = false,
    val ranking: List<UsuarioEntity> = emptyList(),
    val historial: List<ProgresoDetallado> = emptyList(),
    // Estado del editor de avatar
    val editorSpecies: CharacterSpecies = CharacterSpecies.DUDE,
    val editorPrimary: AvatarColor = AvatarColor.COBALT_BLUE,
    val editorSecondary: AvatarColor = AvatarColor.RUBY_RED,
    val editorActiveZone: AvatarColorZone = AvatarColorZone.PRIMARY,
    val editorPreviewAnim: CharacterAnimation = CharacterAnimation.IDLE
)

/**
 * ViewModel encargado del estado de perfil, personalización de avatar,
 * vitrina de carné de aventurero e historial de progreso del estudiante (RF-14).
 */
class ProfileViewModel(
    private val campusRepository: CampusRepository,
    private val preferencesRepository: UserPreferencesRepository,
    usuarioId: Int
) : ViewModel() {

    private val _spriteAnim = MutableStateFlow(CharacterAnimation.IDLE)
    private val _showEditor = MutableStateFlow(false)
    private val _showTimeline = MutableStateFlow(false)
    private val _showAdventurerCard = MutableStateFlow(false)
    private val _showOnboarding = MutableStateFlow(false)
    private val _showRanking = MutableStateFlow(false)

    // Estado del editor de avatar
    private val _editorSpecies = MutableStateFlow(CharacterSpecies.DUDE)
    private val _editorPrimary = MutableStateFlow(AvatarColor.COBALT_BLUE)
    private val _editorSecondary = MutableStateFlow(AvatarColor.RUBY_RED)
    private val _editorActiveZone = MutableStateFlow(AvatarColorZone.PRIMARY)
    private val _editorPreviewAnim = MutableStateFlow(CharacterAnimation.IDLE)

    private val editorStateFlow = combine(
        _editorSpecies,
        _editorPrimary,
        _editorSecondary,
        _editorActiveZone,
        _editorPreviewAnim
    ) { sp, pr, sc, zone, anim ->
        EditorState(sp, pr, sc, zone, anim)
    }

    private val dialogStatesFlow = combine(
        _spriteAnim,
        _showEditor,
        _showTimeline,
        _showAdventurerCard,
        _showOnboarding
    ) { anim, editor, timeline, card, onboarding ->
        DialogStates(anim, editor, timeline, card, onboarding, _showRanking.value)
    }.combine(_showRanking) { states, rankingOpen ->
        states.copy(showRanking = rankingOpen)
    }

    private val uiControlFlow = combine(
        dialogStatesFlow,
        editorStateFlow
    ) { dialogs, editor ->
        Pair(dialogs, editor)
    }

    private data class UserData(
        val usuario: UsuarioEntity?,
        val totalInsignias: Int,
        val historial: List<ProgresoDetallado>,
        val ranking: List<UsuarioEntity>
    )

    private val userDataFlow = combine(
        campusRepository.observarUsuario(usuarioId),
        campusRepository.observarTotalInsignias(usuarioId),
        campusRepository.observarHistorialProgreso(usuarioId),
        campusRepository.observarRanking()
    ) { usuario, totalInsignias, historial, rankingList ->
        UserData(usuario, totalInsignias, historial, rankingList)
    }

    val uiState: StateFlow<ProfileUiState> = combine(
        userDataFlow,
        preferencesRepository.preferencias,
        uiControlFlow
    ) { userData, prefs, (dialogs, editor) ->
        val userSpecies = prefs?.characterSpecies ?: CharacterSpecies.DUDE
        val userPrimary = prefs?.avatarPrimaryColor ?: AvatarColor.COBALT_BLUE
        val userSecondary = prefs?.avatarSecondaryColor ?: AvatarColor.RUBY_RED

        ProfileUiState(
            usuario = userData.usuario,
            totalInsignias = userData.totalInsignias,
            selectedSpecies = userSpecies,
            primaryColor = userPrimary,
            secondaryColor = userSecondary,
            spriteAnim = dialogs.spriteAnim,
            showEditor = dialogs.showEditor,
            showTimeline = dialogs.showTimeline,
            showAdventurerCard = dialogs.showAdventurerCard,
            showOnboarding = dialogs.showOnboarding,
            showRanking = dialogs.showRanking,
            ranking = userData.ranking,
            historial = userData.historial,
            editorSpecies = editor.species,
            editorPrimary = editor.primary,
            editorSecondary = editor.secondary,
            editorActiveZone = editor.activeZone,
            editorPreviewAnim = editor.previewAnim
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    private data class DialogStates(
        val spriteAnim: CharacterAnimation,
        val showEditor: Boolean,
        val showTimeline: Boolean,
        val showAdventurerCard: Boolean,
        val showOnboarding: Boolean,
        val showRanking: Boolean = false
    )

    private data class EditorState(
        val species: CharacterSpecies,
        val primary: AvatarColor,
        val secondary: AvatarColor,
        val activeZone: AvatarColorZone,
        val previewAnim: CharacterAnimation
    )

    fun toggleSpriteAnim() {
        _spriteAnim.value = if (_spriteAnim.value == CharacterAnimation.IDLE) {
            CharacterAnimation.WALK
        } else {
            CharacterAnimation.IDLE
        }
    }

    fun abrirEditor() {
        val current = uiState.value
        _editorSpecies.value = current.selectedSpecies
        _editorPrimary.value = current.primaryColor
        _editorSecondary.value = current.secondaryColor
        _editorActiveZone.value = AvatarColorZone.PRIMARY
        _editorPreviewAnim.value = CharacterAnimation.IDLE
        _showEditor.value = true
    }

    fun cerrarEditor() {
        _showEditor.value = false
    }

    fun onEditorSpeciesChange(species: CharacterSpecies) {
        _editorSpecies.value = species
    }

    fun onEditorColorSelected(color: AvatarColor) {
        if (_editorActiveZone.value == AvatarColorZone.PRIMARY) {
            _editorPrimary.value = color
        } else {
            _editorSecondary.value = color
        }
    }

    fun onEditorActiveZoneChange(zone: AvatarColorZone) {
        _editorActiveZone.value = zone
    }

    fun toggleEditorPreviewAnim() {
        _editorPreviewAnim.value = if (_editorPreviewAnim.value == CharacterAnimation.IDLE) {
            CharacterAnimation.WALK
        } else {
            CharacterAnimation.IDLE
        }
    }

    fun restablecerEditor() {
        val current = uiState.value
        _editorSpecies.value = current.selectedSpecies
        _editorPrimary.value = current.primaryColor
        _editorSecondary.value = current.secondaryColor
    }

    fun guardarAvatar() {
        val sp = _editorSpecies.value
        val pr = _editorPrimary.value
        val sc = _editorSecondary.value
        viewModelScope.launch {
            preferencesRepository.setCharacterSpecies(sp)
            preferencesRepository.setAvatarPrimaryColor(pr)
            preferencesRepository.setAvatarSecondaryColor(sc)
        }
        _showEditor.value = false
    }

    fun abrirTimeline() {
        _showTimeline.value = true
    }

    fun cerrarTimeline() {
        _showTimeline.value = false
    }

    fun abrirAdventurerCard() {
        _showAdventurerCard.value = true
    }

    fun cerrarAdventurerCard() {
        _showAdventurerCard.value = false
    }

    fun abrirOnboarding() {
        _showOnboarding.value = true
    }

    fun cerrarOnboarding() {
        _showOnboarding.value = false
    }

    fun abrirRanking() {
        _showRanking.value = true
    }

    fun cerrarRanking() {
        _showRanking.value = false
    }
}
