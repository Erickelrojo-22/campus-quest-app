package com.example.gamequest.ui.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.data.repository.CampusRepository
import com.example.gamequest.data.repository.MisionConEstado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class FiltroMisiones { ACTIVAS, COMPLETADAS, TODAS }

/** RF-08 Listar misiones (activas, completadas, todas). */
class MissionsViewModel(
    campusRepository: CampusRepository,
    usuarioId: Int
) : ViewModel() {

    private val _filtro = MutableStateFlow(FiltroMisiones.ACTIVAS)
    val filtro: StateFlow<FiltroMisiones> = _filtro

    private val todasLasMisiones: StateFlow<List<MisionConEstado>> = campusRepository
        .observarMisionesConEstado(usuarioId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val misionesFiltradas: StateFlow<List<MisionConEstado>> = combine(todasLasMisiones, _filtro) { lista, filtro ->
        when (filtro) {
            FiltroMisiones.ACTIVAS -> lista.filter { !it.completada }
            FiltroMisiones.COMPLETADAS -> lista.filter { it.completada }
            FiltroMisiones.TODAS -> lista
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Los contadores dependen solo de la lista, no del filtro seleccionado;
    // antes recombinaban con _filtro y se recalculaban al cambiar de pestaña.
    val contadorActivas: StateFlow<Int> = todasLasMisiones
        .map { lista -> lista.count { !it.completada } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val contadorCompletadas: StateFlow<Int> = todasLasMisiones
        .map { lista -> lista.count { it.completada } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun onFiltroChange(nuevo: FiltroMisiones) {
        _filtro.value = nuevo
    }
}
