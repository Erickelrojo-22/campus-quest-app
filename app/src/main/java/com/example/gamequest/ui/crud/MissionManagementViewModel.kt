package com.example.gamequest.ui.crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.data.local.entity.MisionEntity
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import com.example.gamequest.data.repository.CampusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** RF-16: gestión (crear, consultar, editar, eliminar) de misiones y puntos de interés. */
class MissionManagementViewModel(private val campusRepository: CampusRepository) : ViewModel() {

    val misiones: StateFlow<List<Pair<MisionEntity, PuntoInteresEntity>>> = campusRepository
        .observarMisionesConPunto()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _misionAEliminar = MutableStateFlow<MisionEntity?>(null)
    val misionAEliminar: StateFlow<MisionEntity?> = _misionAEliminar.asStateFlow()

    fun solicitarEliminacion(mision: MisionEntity) {
        _misionAEliminar.value = mision
    }

    fun cancelarEliminacion() {
        _misionAEliminar.value = null
    }

    fun confirmarEliminacion() {
        val seleccionada = _misionAEliminar.value ?: return
        _misionAEliminar.value = null
        viewModelScope.launch { campusRepository.eliminarMision(seleccionada) }
    }

    fun eliminar(mision: MisionEntity) {
        viewModelScope.launch { campusRepository.eliminarMision(mision) }
    }
}
