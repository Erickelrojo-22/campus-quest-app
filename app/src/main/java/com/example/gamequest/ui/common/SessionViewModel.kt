package com.example.gamequest.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.AppContainer
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.preferences.UserPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Fuente única de verdad sobre la sesión activa: qué usuario inició sesión y
 * las preferencias persistidas con DataStore (RF-04, RF-18, RF-19).
 * Se crea una sola vez, a nivel de la Activity, y se comparte entre pantallas.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModel(private val container: AppContainer) : ViewModel() {

    val preferencias: StateFlow<UserPreferences> = container.preferencesRepository.preferencias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    val usuarioActual: StateFlow<UsuarioEntity?> = preferencias
        .flatMapLatest { prefs ->
            if (prefs.usuarioActivoId <= 0) {
                flowOf(null)
            } else {
                container.campusRepository.observarUsuario(prefs.usuarioActivoId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun iniciarSesion(usuarioId: Int) {
        viewModelScope.launch { container.preferencesRepository.setUsuarioActivoId(usuarioId) }
    }

    fun cerrarSesion() {
        viewModelScope.launch { container.preferencesRepository.cerrarSesion() }
    }
}
