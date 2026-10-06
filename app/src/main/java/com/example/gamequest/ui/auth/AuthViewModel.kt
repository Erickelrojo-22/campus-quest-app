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
    val modoInstitucional: Boolean = true,
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val cargando: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel encargado del estado de autenticación y de las entradas
 * del formulario de inicio de sesión (RF-01 / RF-02).
 */
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onModoInstitucionalChange(esInstitucional: Boolean) {
        _uiState.value = _uiState.value.copy(modoInstitucional = esInstitucional, error = null)
    }

    fun onNombreChange(valor: String) {
        _uiState.value = _uiState.value.copy(nombre = valor, error = null)
    }

    fun onSeleccionarUsuarioPrueba(nombrePrueba: String) {
        _uiState.value = _uiState.value.copy(nombre = nombrePrueba, error = null)
    }

    fun onCorreoChange(valor: String) {
        _uiState.value = _uiState.value.copy(correo = valor, error = null)
    }

    fun onContrasenaChange(valor: String) {
        _uiState.value = _uiState.value.copy(contrasena = valor, error = null)
    }

    fun entrar(
        nombre: String,
        esTutor: Boolean,
        onExito: (UsuarioEntity) -> Unit
    ) {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        viewModelScope.launch {
            when (val resultado = authRepository.entrarConNombre(nombre, esTutor)) {
                is AuthResult.Exito -> {
                    _uiState.value = _uiState.value.copy(cargando = false, error = null)
                    onExito(resultado.usuario)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun entrarConCredenciales(
        correo: String,
        contrasena: String,
        onExito: (UsuarioEntity) -> Unit
    ) {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        viewModelScope.launch {
            when (val resultado = authRepository.iniciarSesionConCredenciales(correo, contrasena)) {
                is AuthResult.Exito -> {
                    _uiState.value = _uiState.value.copy(cargando = false, error = null)
                    onExito(resultado.usuario)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun iniciarSesion(onExito: (UsuarioEntity) -> Unit) {
        val estadoActual = _uiState.value
        // Evita envíos duplicados (tecla "Done" del teclado + botón).
        if (estadoActual.cargando) return
        if (estadoActual.modoInstitucional) {
            entrarConCredenciales(estadoActual.correo, estadoActual.contrasena, onExito)
        } else {
            entrar(estadoActual.nombre, false, onExito)
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

