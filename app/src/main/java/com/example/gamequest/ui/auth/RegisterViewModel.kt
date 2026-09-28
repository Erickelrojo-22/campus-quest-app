package com.example.gamequest.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.repository.AuthRepository
import com.example.gamequest.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val nombres: String = "",
    val correo: String = "",
    val carrera: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val esTutor: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel encargado del formulario y validaciones reactivas
 * para el registro de nuevos usuarios (RF-02).
 */
class RegisterViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNombresChange(valor: String) {
        _uiState.update { it.copy(nombres = valor, error = null) }
    }

    fun onCorreoChange(valor: String) {
        _uiState.update { it.copy(correo = valor, error = null) }
    }

    fun onCarreraChange(valor: String) {
        _uiState.update { it.copy(carrera = valor, error = null) }
    }

    fun onContrasenaChange(valor: String) {
        _uiState.update { it.copy(contrasena = valor, error = null) }
    }

    fun onConfirmarContrasenaChange(valor: String) {
        _uiState.update { it.copy(confirmarContrasena = valor, error = null) }
    }

    fun onEsTutorChange(valor: Boolean) {
        _uiState.update { it.copy(esTutor = valor) }
    }

    fun registrar(onExito: (UsuarioEntity) -> Unit) {
        val actual = _uiState.value
        if (actual.nombres.isBlank()) {
            _uiState.update { it.copy(error = "Escribe tus nombres completos.") }
            return
        }
        if (!authRepository.validarCorreoInstitucional(actual.correo)) {
            _uiState.update { it.copy(error = "El correo debe ser institucional (${AuthRepository.DOMINIO_INSTITUCIONAL}).") }
            return
        }
        if (actual.carrera.isBlank()) {
            _uiState.update { it.copy(error = "Selecciona o ingresa tu carrera.") }
            return
        }
        if (actual.contrasena.length < AuthRepository.MIN_CONTRASENA_LENGTH) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos ${AuthRepository.MIN_CONTRASENA_LENGTH} caracteres.") }
            return
        }
        if (actual.contrasena != actual.confirmarContrasena) {
            _uiState.update { it.copy(error = "Las contraseñas no coinciden.") }
            return
        }

        _uiState.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            when (val res = authRepository.registrar(
                nombres = actual.nombres,
                correo = actual.correo,
                carrera = actual.carrera,
                contrasena = actual.contrasena,
                esTutor = actual.esTutor
            )) {
                is AuthResult.Exito -> {
                    _uiState.update { it.copy(cargando = false) }
                    onExito(res.usuario)
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(cargando = false, error = res.mensaje) }
                }
            }
        }
    }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }
}
