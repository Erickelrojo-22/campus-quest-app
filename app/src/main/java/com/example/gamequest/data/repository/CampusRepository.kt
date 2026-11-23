package com.example.gamequest.data.repository

import com.example.gamequest.data.local.entity.MisionEntity
import com.example.gamequest.data.local.entity.ProgresoMisionEntity
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import kotlinx.coroutines.flow.Flow

/** Misión combinada con su punto de interés y el estado de avance del usuario actual. */
data class MisionConEstado(
    val mision: MisionEntity,
    val punto: PuntoInteresEntity,
    val completada: Boolean
)

/** Progreso detallado con datos de la misión y el lugar donde se completó (timeline/historial). */
data class ProgresoDetallado(
    val progreso: ProgresoMisionEntity,
    val mision: MisionEntity?,
    val punto: PuntoInteresEntity?
)

sealed class ValidacionQrResult {
    data class MisionCompletada(
        val mision: MisionEntity,
        val punto: PuntoInteresEntity,
        val puntosGanados: Int,
        val nuevoPuntajeTotal: Int,
        val nuevoNivel: Int,
        val totalInsignias: Int
    ) : ValidacionQrResult()

    data object YaCompletada : ValidacionQrResult()
    data object CodigoNoReconocido : ValidacionQrResult()
    data object SinMisionAsociada : ValidacionQrResult()
}

/**
 * Contrato completo del catálogo del campus que consumen los ViewModels.
 * Amplía las lecturas de [CampusDataRepository] con la validación de QR y el
 * CRUD del tutor. Room es la fuente única de verdad; las implementaciones
 * ([OfflineFirstCampusRepository] y su variante remota) deciden cómo se
 * sincroniza esa caché y en qué hilo se ejecuta cada operación.
 */
interface CampusRepository : CampusDataRepository {
    fun observarPuntos(): Flow<List<PuntoInteresEntity>>

    /** RF-10 a RF-13: valida un código QR, registra el progreso y otorga puntos. */
    suspend fun validarCodigo(codigo: String, usuarioId: Int): ValidacionQrResult

    /** RF-16 / RF-17: CRUD del catálogo, reservado al tutor local. */
    suspend fun crearPuntoConMision(
        punto: PuntoInteresEntity,
        titulo: String,
        descripcionPista: String,
        puntos: Int,
        tiempoEstimadoMin: Int,
        dificultad: String,
        insigniaNombre: String,
        insigniaEmoji: String
    ): Int

    suspend fun actualizarMisionYPunto(mision: MisionEntity, punto: PuntoInteresEntity)

    suspend fun eliminarMision(mision: MisionEntity)

    fun generarCodigoQr(categoria: String): String
}
