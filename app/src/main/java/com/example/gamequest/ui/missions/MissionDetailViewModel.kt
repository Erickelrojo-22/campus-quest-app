package com.example.gamequest.ui.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.data.local.entity.MisionEntity
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import com.example.gamequest.data.repository.CampusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class MisionDetailState(
    val mision: MisionEntity? = null,
    val punto: PuntoInteresEntity? = null,
    val completada: Boolean = false,
    val cargando: Boolean = true
)

/** RF-09: detalle de una misión. */
class MissionDetailViewModel(
    campusRepository: CampusRepository,
    misionId: Int,
    usuarioId: Int
) : ViewModel() {

    val estado: StateFlow<MisionDetailState> = combine(
        campusRepository.observarMisionPorId(misionId),
        campusRepository.observarMisionesConEstado(usuarioId)
    ) { mision, listaConEstado ->
        val estadoActual = listaConEstado.firstOrNull { it.mision.id == misionId }
        MisionDetailState(
            mision = mision,
            punto = estadoActual?.punto,
            completada = estadoActual?.completada ?: false,
            cargando = mision == null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MisionDetailState())
}
