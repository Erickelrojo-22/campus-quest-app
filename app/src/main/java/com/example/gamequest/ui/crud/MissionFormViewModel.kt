package com.example.gamequest.ui.crud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamequest.data.local.entity.Dificultad
import com.example.gamequest.data.local.entity.MisionEntity
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import com.example.gamequest.data.repository.CampusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MissionFormState(
    val esEdicion: Boolean = false,
    val nombreLugar: String = "",
    val categoria: String = "Académico",
    val horarioAtencion: String = "",
    val tramites: String = "",
    val titulo: String = "",
    val descripcionPista: String = "",
    val puntos: String = "30",
    val tiempoEstimadoMin: String = "10",
    val dificultad: String = Dificultad.MEDIA,
    val insigniaNombre: String = "",
    val insigniaEmoji: String = "🎖",
    val codigoQr: String = "",
    val guardando: Boolean = false,
    val guardadoOk: Boolean = false,
    val error: String? = null
)

/** RF-16 CRUD de misiones y puntos de interés, RF-17 generación de código QR. */
class MissionFormViewModel(
    private val campusRepository: CampusRepository,
    private val puntoIdEditar: Int?
) : ViewModel() {

    private val _state = MutableStateFlow(MissionFormState(esEdicion = puntoIdEditar != null))
    val state: StateFlow<MissionFormState> = _state.asStateFlow()

    private var puntoOriginal: PuntoInteresEntity? = null
    private var misionOriginal: MisionEntity? = null

    init {
        if (puntoIdEditar != null) {
            viewModelScope.launch {
                val punto = campusRepository.buscarPuntoPorId(puntoIdEditar)
                puntoOriginal = punto
                if (punto != null) {
                    misionOriginal = campusRepository.buscarMisionPorPuntoId(punto.id)
                }
                if (punto != null && misionOriginal != null) {
                    val m = misionOriginal!!
                    _state.value = _state.value.copy(
                        nombreLugar = punto.nombre,
                        categoria = punto.categoria,
                        horarioAtencion = punto.horarioAtencion,
                        tramites = punto.tramites,
                        titulo = m.titulo,
                        descripcionPista = m.descripcionPista,
                        puntos = m.puntos.toString(),
                        tiempoEstimadoMin = m.tiempoEstimadoMin.toString(),
                        dificultad = m.dificultad,
                        insigniaNombre = m.insigniaNombre,
                        insigniaEmoji = m.insigniaEmoji,
                        codigoQr = punto.codigoQr
                    )
                }
            }
        }
    }

    fun onNombreLugar(v: String) { _state.value = _state.value.copy(nombreLugar = v) }
    fun onCategoria(v: String) { _state.value = _state.value.copy(categoria = v) }
    fun onHorario(v: String) { _state.value = _state.value.copy(horarioAtencion = v) }
    fun onTramites(v: String) { _state.value = _state.value.copy(tramites = v) }
    fun onTitulo(v: String) { _state.value = _state.value.copy(titulo = v) }
    fun onDescripcionPista(v: String) { _state.value = _state.value.copy(descripcionPista = v) }
    fun onPuntos(v: String) { _state.value = _state.value.copy(puntos = v.filter { it.isDigit() }) }
    fun onTiempo(v: String) { _state.value = _state.value.copy(tiempoEstimadoMin = v.filter { it.isDigit() }) }
    fun onDificultad(v: String) { _state.value = _state.value.copy(dificultad = v) }
    fun onInsigniaNombre(v: String) { _state.value = _state.value.copy(insigniaNombre = v) }
    fun onInsigniaEmoji(v: String) { _state.value = _state.value.copy(insigniaEmoji = v) }
    fun onCodigoQr(v: String) { _state.value = _state.value.copy(codigoQr = v) }

    fun generarCodigoQr() {
        _state.value = _state.value.copy(codigoQr = campusRepository.generarCodigoQr(_state.value.categoria))
    }

    fun guardar(onExito: () -> Unit) {
        val s = _state.value
        if (s.nombreLugar.isBlank() || s.titulo.isBlank() || s.codigoQr.isBlank()) {
            _state.value = s.copy(error = "Completa el nombre del lugar, el título de la misión y el código QR.")
            return
        }
        _state.value = s.copy(guardando = true, error = null)
        viewModelScope.launch {
            if (s.esEdicion && puntoOriginal != null && misionOriginal != null) {
                val puntoActualizado = puntoOriginal!!.copy(
                    nombre = s.nombreLugar,
                    categoria = s.categoria,
                    horarioAtencion = s.horarioAtencion,
                    tramites = s.tramites,
                    codigoQr = s.codigoQr.uppercase()
                )
                val misionActualizada = misionOriginal!!.copy(
                    titulo = s.titulo,
                    descripcionPista = s.descripcionPista,
                    puntos = s.puntos.toIntOrNull() ?: 0,
                    tiempoEstimadoMin = s.tiempoEstimadoMin.toIntOrNull() ?: 0,
                    dificultad = s.dificultad,
                    insigniaNombre = s.insigniaNombre,
                    insigniaEmoji = s.insigniaEmoji
                )
                campusRepository.actualizarMisionYPunto(misionActualizada, puntoActualizado)
            } else {
                campusRepository.crearPuntoConMision(
                    punto = PuntoInteresEntity(
                        nombre = s.nombreLugar,
                        categoria = s.categoria,
                        descripcion = s.tramites,
                        horarioAtencion = s.horarioAtencion,
                        tramites = s.tramites,
                        posX = (0.15f..0.85f).random(),
                        posY = (0.2f..0.85f).random(),
                        codigoQr = s.codigoQr.uppercase()
                    ),
                    titulo = s.titulo,
                    descripcionPista = s.descripcionPista,
                    puntos = s.puntos.toIntOrNull() ?: 0,
                    tiempoEstimadoMin = s.tiempoEstimadoMin.toIntOrNull() ?: 0,
                    dificultad = s.dificultad,
                    insigniaNombre = s.insigniaNombre,
                    insigniaEmoji = s.insigniaEmoji
                )
            }
            _state.value = _state.value.copy(guardando = false, guardadoOk = true)
            onExito()
        }
    }
}

private fun ClosedFloatingPointRange<Float>.random(): Float =
    start + (endInclusive - start) * kotlin.random.Random.nextFloat()
