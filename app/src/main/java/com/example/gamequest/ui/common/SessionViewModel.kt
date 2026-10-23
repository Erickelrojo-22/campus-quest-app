package com.example.gamequest.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.AppContainer
import kotlinx.coroutines.launch

class SessionViewModel(private val container: AppContainer) : ViewModel() {
    val state = container.sessionCoordinator.state
    init { viewModelScope.launch { container.sessionCoordinator.restore() } }
    fun retry() { viewModelScope.launch { container.sessionCoordinator.retryRestore() } }
    fun iniciarSesion(usuarioId: Int) { viewModelScope.launch { container.sessionCoordinator.acceptLogin(usuarioId) } }
    fun cerrarSesion() { viewModelScope.launch { container.sessionCoordinator.logout() } }
}
