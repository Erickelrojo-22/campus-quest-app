package com.example.gamequest.ui.badges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.repository.CampusRepository
import com.example.gamequest.data.repository.MisionConEstado
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class BadgesUiState(
    val usuario: UsuarioEntity? = null,
    val obtenidas: List<MisionConEstado> = emptyList(),
    val pendientes: List<MisionConEstado> = emptyList(),
    val ranking: List<UsuarioEntity> = emptyList()
)

/** RF-14 insignias y progreso, RF-15 ranking del campus. */
class BadgesViewModel(
    campusRepository: CampusRepository,
    usuarioId: Int
) : ViewModel() {

    val estado: StateFlow<BadgesUiState> = combine(
        campusRepository.observarUsuario(usuarioId),
        campusRepository.observarMisionesConEstado(usuarioId),
        campusRepository.observarRanking()
    ) { usuario, misiones, ranking ->
        BadgesUiState(
            usuario = usuario,
            obtenidas = misiones.filter { it.completada },
            pendientes = misiones.filter { !it.completada },
            ranking = ranking.take(10)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BadgesUiState())
}
