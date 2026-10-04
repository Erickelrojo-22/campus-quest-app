package com.example.gamequest.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.AppContainer
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.preferences.UserPreferences
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModel(private val container: AppContainer) : ViewModel() {
    val preferencias: StateFlow<UserPreferences> = container.preferencesRepository.preferencias
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())
    val usuarioActual: StateFlow<UsuarioEntity?> = preferencias
        .flatMapLatest { prefs ->
            if (prefs.usuarioActivoId <= 0) flowOf(null)
            else container.campusRepository.observarUsuario(prefs.usuarioActivoId)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val ready = MutableStateFlow(false)
    val syncError = MutableStateFlow<String?>(null)
    val syncing = MutableStateFlow(false)

    init {
        container.api.onUnauthorized = {
            viewModelScope.launch {
                container.preferencesRepository.cerrarSesion()
                syncError.value = "Tu sesión expiró. Inicia sesión nuevamente."
            }
        }
        viewModelScope.launch {
            // A legacy local ID never grants access to a remote account.
            if (container.api.tokens.token == null) container.preferencesRepository.cerrarSesion()
            else refresh()
            ready.value = true
        }
    }

    suspend fun refresh() {
        if (container.api.tokens.token == null || syncing.value) return
        val token = container.api.tokens.token
        syncing.value = true
        try {
            val user = container.campusRepository.refresh()
            if (container.api.tokens.token == token) {
                container.preferencesRepository.setUsuarioActivoId(user.id)
                syncError.value = null
            }
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) { syncError.value = e.message ?: "No se pudo actualizar. Se muestra la última copia descargada."
        } finally { syncing.value = false }
    }
    fun retry() { viewModelScope.launch { refresh() } }
    fun iniciarSesion(usuarioId: Int) {
        viewModelScope.launch {
            container.preferencesRepository.setUsuarioActivoId(usuarioId)
            refresh()
        }
    }
    fun cerrarSesion() {
        val token = container.api.tokens.token
        container.api.tokens.clear()
        viewModelScope.launch {
            container.preferencesRepository.cerrarSesion()
            syncError.value = null
            if (token != null) {
                try { container.api.logout(token)
                } catch (e: CancellationException) { throw e
                } catch (_: Exception) { syncError.value = "Sesión cerrada en este dispositivo. El servidor no confirmó la revocación." }
            }
        }
    }
    override fun onCleared() {
        container.api.onUnauthorized = {}
        super.onCleared()
    }
}
