package com.example.gamequest.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.repository.CampusRepository
import com.example.gamequest.data.repository.MisionConEstado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** RF-05, RF-06, RF-07: mapa del campus, ficha del punto y buscador. */
class HomeViewModel(
    private val campusRepository: CampusRepository,
    private val usuarioId: Int
) : ViewModel() {

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda

    val usuario: StateFlow<UsuarioEntity?> = campusRepository.observarUsuario(usuarioId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val misiones: StateFlow<List<MisionConEstado>> = campusRepository
        .observarMisionesConEstado(usuarioId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resultadosBusqueda: StateFlow<List<MisionConEstado>> = combine(misiones, _busqueda) { lista, query ->
        if (query.isBlank()) emptyList()
        else lista.filter { estado ->
            estado.punto.nombre.contains(query, ignoreCase = true) ||
                estado.mision.titulo.contains(query, ignoreCase = true) ||
                estado.punto.categoria.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Depende solo de la lista de misiones; antes recombinaba con el texto de
    // búsqueda (que ignora), recalculándose en cada pulsación del teclado.
    val misionSugerida: StateFlow<MisionConEstado?> = misiones
        .map { lista -> lista.firstOrNull { !it.completada } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onBusquedaChange(valor: String) {
        _busqueda.value = valor
    }
}
