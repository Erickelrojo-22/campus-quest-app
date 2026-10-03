package com.example.gamequest.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.data.repository.CampusRepository
import com.example.gamequest.data.repository.ValidacionQrResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ScannerUiState {
    data object Escaneando : ScannerUiState()
    data object Procesando : ScannerUiState()
    data class Resultado(val resultado: ValidacionQrResult) : ScannerUiState()
    data class Error(val mensaje: String) : ScannerUiState()
}

/** RF-10 escanear QR, RF-11 ingresar código manualmente, RF-12 registrar progreso, RF-13 otorgar insignias. */
class ScannerViewModel(
    private val campusRepository: CampusRepository,
    private val usuarioId: Int
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScannerUiState>(ScannerUiState.Escaneando)
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val _mostrarDialogoManual = MutableStateFlow(false)
    val mostrarDialogoManual: StateFlow<Boolean> = _mostrarDialogoManual.asStateFlow()

    private val _codigoManual = MutableStateFlow("")
    val codigoManual: StateFlow<String> = _codigoManual.asStateFlow()

    fun abrirDialogoManual() {
        _mostrarDialogoManual.value = true
    }

    fun cerrarDialogoManual() {
        _mostrarDialogoManual.value = false
    }

    fun onCodigoManualChange(codigo: String) {
        _codigoManual.value = codigo
    }

    fun validarCodigoManual() {
        val codigo = _codigoManual.value
        _mostrarDialogoManual.value = false
        validar(codigo)
    }

    fun validar(codigo: String) {
        if (_uiState.value !is ScannerUiState.Escaneando) return
        _uiState.value = ScannerUiState.Procesando
        viewModelScope.launch {
            runCatching { campusRepository.validarCodigo(codigo, usuarioId) }
                .onSuccess { _uiState.value = ScannerUiState.Resultado(it) }
                .onFailure { _uiState.value = ScannerUiState.Error(it.message ?: "No se pudo validar el código.") }
        }
    }

    fun reiniciar() {
        _uiState.value = ScannerUiState.Escaneando
    }
}
