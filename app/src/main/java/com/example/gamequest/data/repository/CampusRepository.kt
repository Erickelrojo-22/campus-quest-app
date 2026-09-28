package com.example.gamequest.data.repository

import com.example.gamequest.data.local.dao.MisionDao
import com.example.gamequest.data.local.dao.ProgresoMisionDao
import com.example.gamequest.data.local.dao.PuntoInteresDao
import com.example.gamequest.data.local.dao.UsuarioDao
import com.example.gamequest.data.local.entity.MisionEntity
import com.example.gamequest.data.local.entity.ProgresoMisionEntity
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import com.example.gamequest.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Misión combinada con su punto de interés y el estado de avance del usuario actual. */
data class MisionConEstado(
    val mision: MisionEntity,
    val punto: PuntoInteresEntity,
    val completada: Boolean
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
 * Repositorio central del campus: puntos de interés, misiones y progreso
 * (RF-05 a RF-17). Toda la información se sirve desde Room, por lo que la
 * aplicación funciona igual con o sin conexión (RF-20).
 */
class CampusRepository(
    private val puntoDao: PuntoInteresDao,
    private val misionDao: MisionDao,
    private val progresoDao: ProgresoMisionDao,
    private val usuarioDao: UsuarioDao
) {

    fun observarPuntos(): Flow<List<PuntoInteresEntity>> = puntoDao.observarTodos()

    fun observarUsuario(id: Int): Flow<UsuarioEntity?> = usuarioDao.observarPorId(id)

    fun observarRanking(): Flow<List<UsuarioEntity>> = usuarioDao.observarRanking()

    /** Lista de misiones combinada con su punto de interés y si el usuario ya la completó. */
    fun observarMisionesConEstado(usuarioId: Int): Flow<List<MisionConEstado>> =
        combine(
            misionDao.observarTodas(),
            puntoDao.observarTodos(),
            progresoDao.observarMisionesCompletadasIds(usuarioId)
        ) { misiones, puntos, completadasIds ->
            val puntosPorId = puntos.associateBy { it.id }
            misiones.mapNotNull { mision ->
                val punto = puntosPorId[mision.puntoInteresId] ?: return@mapNotNull null
                MisionConEstado(
                    mision = mision,
                    punto = punto,
                    completada = mision.id in completadasIds
                )
            }
        }

    /** RF-16: listado de misiones con su punto de interés, para la pantalla de gestión (CRUD). */
    fun observarMisionesConPunto(): Flow<List<Pair<MisionEntity, PuntoInteresEntity>>> =
        combine(misionDao.observarTodas(), puntoDao.observarTodos()) { misiones, puntos ->
            val puntosPorId = puntos.associateBy { it.id }
            misiones.mapNotNull { mision ->
                val punto = puntosPorId[mision.puntoInteresId] ?: return@mapNotNull null
                mision to punto
            }
        }

    suspend fun buscarPuntoPorId(id: Int): PuntoInteresEntity? = puntoDao.buscarPorId(id)

    suspend fun buscarMisionPorId(id: Int): MisionEntity? = misionDao.buscarPorId(id)

    /** Misión(es) asociada(s) a un punto de interés (relación 1 a 1 en esta etapa). */
    suspend fun buscarMisionPorPuntoId(puntoId: Int): MisionEntity? =
        misionDao.buscarPorPunto(puntoId).firstOrNull()

    fun observarMisionPorId(id: Int): Flow<MisionEntity?> = misionDao.observarPorId(id)

    fun observarPuntoPorId(id: Int): Flow<PuntoInteresEntity?> = puntoDao.observarPorId(id)

    fun observarTotalInsignias(usuarioId: Int): Flow<Int> = progresoDao.observarTotalCompletadas(usuarioId)

    /**
     * RF-10 / RF-11 / RF-12 / RF-13: valida un código QR (o ingresado a mano),
     * registra el progreso, otorga puntos y actualiza el nivel del usuario.
     */
    suspend fun validarCodigo(codigo: String, usuarioId: Int): ValidacionQrResult {
        val codigoNormalizado = codigo.trim().uppercase()
        val punto = puntoDao.buscarPorCodigoQr(codigoNormalizado)
            ?: return ValidacionQrResult.CodigoNoReconocido

        val misionesDelPunto = misionDao.buscarPorPunto(punto.id)
        val mision = misionesDelPunto.firstOrNull { m ->
            progresoDao.buscar(usuarioId, m.id) == null
        }

        if (mision == null) {
            val yaHayMision = misionesDelPunto.isNotEmpty()
            return if (yaHayMision) ValidacionQrResult.YaCompletada else ValidacionQrResult.SinMisionAsociada
        }

        progresoDao.insertar(
            ProgresoMisionEntity(
                usuarioId = usuarioId,
                misionId = mision.id,
                fechaHora = System.currentTimeMillis(),
                puntosObtenidos = mision.puntos,
                codigoQrValidado = codigoNormalizado
            )
        )

        val usuario = usuarioDao.buscarPorId(usuarioId)
        val nuevoPuntaje = (usuario?.puntajeAcumulado ?: 0) + mision.puntos
        val nuevoNivel = 1 + (nuevoPuntaje / 100)
        usuarioDao.sumarPuntos(usuarioId, mision.puntos, nuevoNivel)

        val totalInsignias = progresoDao.contarCompletadas(usuarioId)

        return ValidacionQrResult.MisionCompletada(
            mision = mision,
            punto = punto,
            puntosGanados = mision.puntos,
            nuevoPuntajeTotal = nuevoPuntaje,
            nuevoNivel = nuevoNivel,
            totalInsignias = totalInsignias
        )
    }

    // ---- CRUD de misiones y puntos de interés (RF-16, RF-17) ----

    suspend fun crearPuntoConMision(
        punto: PuntoInteresEntity,
        titulo: String,
        descripcionPista: String,
        puntos: Int,
        tiempoEstimadoMin: Int,
        dificultad: String,
        insigniaNombre: String,
        insigniaEmoji: String
    ): Int {
        val puntoId = puntoDao.insertar(punto).toInt()
        misionDao.insertar(
            MisionEntity(
                titulo = titulo,
                descripcionPista = descripcionPista,
                puntos = puntos,
                tiempoEstimadoMin = tiempoEstimadoMin,
                dificultad = dificultad,
                puntoInteresId = puntoId,
                insigniaNombre = insigniaNombre,
                insigniaEmoji = insigniaEmoji
            )
        )
        return puntoId
    }

    suspend fun actualizarMisionYPunto(mision: MisionEntity, punto: PuntoInteresEntity) {
        misionDao.actualizar(mision)
        puntoDao.actualizar(punto)
    }

    suspend fun eliminarMision(mision: MisionEntity) {
        misionDao.eliminar(mision)
    }

    fun generarCodigoQr(categoria: String): String {
        val prefijo = categoria.take(3).uppercase().ifBlank { "CQ0" }
        val sufijo = (100..999).random()
        return "CQ-$prefijo-$sufijo"
    }
}
