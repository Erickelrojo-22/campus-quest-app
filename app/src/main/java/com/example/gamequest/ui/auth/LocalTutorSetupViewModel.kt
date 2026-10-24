package com.example.gamequest.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.repository.AuthResult
import com.example.gamequest.data.repository.TutorSetupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LocalTutorSetupState(
    val nombres: String = "", val correo: String = "", val carrera: String = "", val contrasena: String = "",
    val cargando: Boolean = false, val error: String? = null
)

class LocalTutorSetupViewModel(private val repository: TutorSetupRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(LocalTutorSetupState())
    val state: StateFlow<LocalTutorSetupState> = mutableState.asStateFlow()
    fun nombres(value: String) { mutableState.value = mutableState.value.copy(nombres = value, error = null) }
    fun correo(value: String) { mutableState.value = mutableState.value.copy(correo = value, error = null) }
    fun carrera(value: String) { mutableState.value = mutableState.value.copy(carrera = value, error = null) }
    fun contrasena(value: String) { mutableState.value = mutableState.value.copy(contrasena = value, error = null) }
    fun save(onSuccess: (UsuarioEntity) -> Unit) {
        val current = mutableState.value
        if (current.cargando) return
        mutableState.value = current.copy(cargando = true, error = null)
        viewModelScope.launch {
            when (val result = repository.configureTutor(current.nombres, current.correo, current.carrera, current.contrasena)) {
                is AuthResult.Exito -> { mutableState.value = mutableState.value.copy(cargando = false); onSuccess(result.usuario) }
                is AuthResult.Error -> mutableState.value = mutableState.value.copy(cargando = false, error = result.mensaje)
            }
        }
    }
}
