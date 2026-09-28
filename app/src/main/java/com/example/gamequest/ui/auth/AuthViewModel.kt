package com.example.gamequest.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.repository.AuthRepository
import com.example.gamequest.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val cargando: Boolean = false,
    val error: String? = null
)

/** Acceso simplificado: solo se pide el nombre para entrar. */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun entrar(
        nombre: String,
        esTutor: Boolean,
        onExito: (UsuarioEntity) -> Unit
    ) {
        _uiState.value = AuthUiState(cargando = true)
        viewModelScope.launch {
            when (val resultado = authRepository.entrarConNombre(nombre, esTutor)) {
                is AuthResult.Exito -> {
                    _uiState.value = AuthUiState()
                    onExito(resultado.usuario)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState(error = resultado.mensaje)
                }
            }
        }
    }

    fun entrarConCredenciales(
        correo: String,
        contrasena: String,
        onExito: (UsuarioEntity) -> Unit
    ) {
        _uiState.value = AuthUiState(cargando = true)
        viewModelScope.launch {
            when (val resultado = authRepository.iniciarSesionConCredenciales(correo, contrasena)) {
                is AuthResult.Exito -> {
                    _uiState.value = AuthUiState()
                    onExito(resultado.usuario)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState(error = resultado.mensaje)
                }
            }
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

