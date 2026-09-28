package com.example.gamequest.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.data.preferences.UserPreferences
import com.example.gamequest.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** RF-18 configurar preferencias, RF-19 persistirlas localmente con DataStore. */
class SettingsViewModel(private val preferencesRepository: UserPreferencesRepository) : ViewModel() {

    val preferencias: StateFlow<UserPreferences> = preferencesRepository.preferencias
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    fun setTemaOscuro(valor: Boolean) = viewModelScope.launch { preferencesRepository.setTemaOscuro(valor) }
    fun setTamanoTexto(valor: String) = viewModelScope.launch { preferencesRepository.setTamanoTexto(valor) }
    fun setRecordatoriosMision(valor: Boolean) = viewModelScope.launch { preferencesRepository.setRecordatoriosMision(valor) }
    fun setAvisosCampus(valor: Boolean) = viewModelScope.launch { preferencesRepository.setAvisosCampus(valor) }
    fun setSonidoVibracion(valor: Boolean) = viewModelScope.launch { preferencesRepository.setSonidoVibracion(valor) }
    fun setCampusPorDefecto(valor: String) = viewModelScope.launch { preferencesRepository.setCampusPorDefecto(valor) }
    fun setDescargarMapaSinConexion(valor: Boolean) = viewModelScope.launch { preferencesRepository.setDescargarMapaSinConexion(valor) }
}
