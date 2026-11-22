package com.example.gamequest.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.data.session.SessionCoordinator
import kotlinx.coroutines.launch

/**
 * Expone el estado de sesión a la navegación. Recibe solo el
 * [SessionCoordinator] que necesita, no el contenedor completo: así no actúa
 * como localizador de servicios y puede probarse con un coordinador falso.
 */
class SessionViewModel(private val sessionCoordinator: SessionCoordinator) : ViewModel() {
    val state = sessionCoordinator.state
    init { viewModelScope.launch { sessionCoordinator.restore() } }
    fun retry() { viewModelScope.launch { sessionCoordinator.retryRestore() } }
    fun iniciarSesion(usuarioId: Int) { viewModelScope.launch { sessionCoordinator.acceptLogin(usuarioId) } }
    fun cerrarSesion() { viewModelScope.launch { sessionCoordinator.logout() } }
}
