package com.example.gamequest.ui.badges

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.data.local.entity.MisionEntity
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.repository.CampusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BadgeEarnedState(
    val mision: MisionEntity? = null,
    val usuario: UsuarioEntity? = null,
    val totalInsignias: Int = 0
)

class BadgeEarnedViewModel(
    private val campusRepository: CampusRepository,
    misionId: Int,
    private val usuarioId: Int
) : ViewModel() {

    private val _estado = MutableStateFlow(BadgeEarnedState())
    val estado: StateFlow<BadgeEarnedState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val mision = campusRepository.buscarMisionPorId(misionId)
            val total = campusRepository.observarTotalInsignias(usuarioId)
            viewModelScope.launch {
                total.collect { totalActual ->
                    _estado.value = _estado.value.copy(totalInsignias = totalActual)
                }
            }
            viewModelScope.launch {
                campusRepository.observarUsuario(usuarioId).collect { usuario ->
                    _estado.value = _estado.value.copy(usuario = usuario)
                }
            }
            _estado.value = _estado.value.copy(mision = mision)
        }
    }
}
