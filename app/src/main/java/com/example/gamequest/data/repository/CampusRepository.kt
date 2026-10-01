package com.example.gamequest.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.withTransaction
import com.example.gamequest.data.local.AppDatabase

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
import kotlinx.coroutines.flow.flowOf

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
 * Repositorio central del campus: puntos de interés, misiones y progreso
 * (RF-05 a RF-17). Toda la información se sirve desde Room, por lo que la
 * aplicación funciona igual con o sin conexión (RF-20).
 */
open class CampusRepository(
    private val database: AppDatabase? = null,
    private val puntoDao: PuntoInteresDao? = null,
    private val misionDao: MisionDao? = null,
    private val progresoDao: ProgresoMisionDao? = null,
    private val usuarioDao: UsuarioDao? = null
) {

    protected open suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return database?.withTransaction { block() } ?: block()
    }

    open fun observarPuntos(): Flow<List<PuntoInteresEntity>> =
        puntoDao?.observarTodos() ?: flowOf(emptyList())

    open fun observarUsuario(id: Int): Flow<UsuarioEntity?> =
        usuarioDao?.observarPorId(id) ?: flowOf(null)

    open fun observarRanking(): Flow<List<UsuarioEntity>> =
        usuarioDao?.observarRanking() ?: flowOf(emptyList())

    /** Lista de misiones combinada con su punto de interés y si el usuario ya la completó. */
    open fun observarMisionesConEstado(usuarioId: Int): Flow<List<MisionConEstado>> {
        val misionesFlow = misionDao?.observarTodas() ?: flowOf(emptyList())
        val puntosFlow = puntoDao?.observarTodos() ?: flowOf(emptyList())
        val completadasFlow = progresoDao?.observarMisionesCompletadasIds(usuarioId) ?: flowOf(emptyList())

        return combine(misionesFlow, puntosFlow, completadasFlow) { misiones, puntos, completadasIds ->
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
    }

    /** RF-16: listado de misiones con su punto de interés, para la pantalla de gestión (CRUD). */
    open fun observarMisionesConPunto(): Flow<List<Pair<MisionEntity, PuntoInteresEntity>>> {
        val misionesFlow = misionDao?.observarTodas() ?: flowOf(emptyList())
        val puntosFlow = puntoDao?.observarTodos() ?: flowOf(emptyList())

        return combine(misionesFlow, puntosFlow) { misiones, puntos ->
            val puntosPorId = puntos.associateBy { it.id }
            misiones.mapNotNull { mision ->
                val punto = puntosPorId[mision.puntoInteresId] ?: return@mapNotNull null
                mision to punto
            }
        }
    }

    open suspend fun buscarPuntoPorId(id: Int): PuntoInteresEntity? = puntoDao?.buscarPorId(id)

    open suspend fun buscarMisionPorId(id: Int): MisionEntity? = misionDao?.buscarPorId(id)

    /** Misión(es) asociada(s) a un punto de interés (relación 1 a 1 en esta etapa). */
    open suspend fun buscarMisionPorPuntoId(puntoId: Int): MisionEntity? =
        misionDao?.buscarPorPunto(puntoId)?.firstOrNull()

    open fun observarMisionPorId(id: Int): Flow<MisionEntity?> =
        misionDao?.observarPorId(id) ?: flowOf(null)

    open fun observarPuntoPorId(id: Int): Flow<PuntoInteresEntity?> =
        puntoDao?.observarPorId(id) ?: flowOf(null)

    open fun observarTotalInsignias(usuarioId: Int): Flow<Int> =
        progresoDao?.observarTotalCompletadas(usuarioId) ?: flowOf(0)

    /** RF-15 / Timeline: Historial de actividades y puntos ganados por el usuario en orden cronológico. */
    open fun observarHistorialProgreso(usuarioId: Int): Flow<List<ProgresoDetallado>> {
        val progresoFlow = progresoDao?.observarPorUsuario(usuarioId) ?: flowOf(emptyList())
        val misionesFlow = misionDao?.observarTodas() ?: flowOf(emptyList())
        val puntosFlow = puntoDao?.observarTodos() ?: flowOf(emptyList())

        return combine(progresoFlow, misionesFlow, puntosFlow) { progresos, misiones, puntos ->
            val misionesPorId = misiones.associateBy { it.id }
            val puntosPorId = puntos.associateBy { it.id }
            progresos.map { prog ->
                val mision = misionesPorId[prog.misionId]
                val punto = mision?.let { puntosPorId[it.puntoInteresId] }
                ProgresoDetallado(
                    progreso = prog,
                    mision = mision,
                    punto = punto
                )
            }
        }
    }

    /**
     * RF-10 / RF-11 / RF-12 / RF-13: valida un código QR (o ingresado a mano),
     * registra el progreso, otorga puntos y actualiza el nivel del usuario.
     */
    open suspend fun validarCodigo(codigo: String, usuarioId: Int): ValidacionQrResult {
        return try {
            runInTransaction {
                validarCodigoEnTransaccion(codigo, usuarioId)
            }
        } catch (_: SQLiteConstraintException) {
            ValidacionQrResult.YaCompletada
        }
    }

    private suspend fun validarCodigoEnTransaccion(codigo: String, usuarioId: Int): ValidacionQrResult {
        val codigoNormalizado = codigo.trim().uppercase()
        val punto = puntoDao?.buscarPorCodigoQr(codigoNormalizado)
            ?: return ValidacionQrResult.CodigoNoReconocido

        val misionesDelPunto = misionDao?.buscarPorPunto(punto.id) ?: emptyList()
        val mision = misionesDelPunto.firstOrNull { m ->
            progresoDao?.buscar(usuarioId, m.id) == null
        }

        if (mision == null) {
            val yaHayMision = misionesDelPunto.isNotEmpty()
            return if (yaHayMision) ValidacionQrResult.YaCompletada else ValidacionQrResult.SinMisionAsociada
        }

        progresoDao?.insertar(
            ProgresoMisionEntity(
                usuarioId = usuarioId,
                misionId = mision.id,
                fechaHora = System.currentTimeMillis(),
                puntosObtenidos = mision.puntos,
                codigoQrValidado = codigoNormalizado
            )
        )

        val usuario = usuarioDao?.buscarPorId(usuarioId)
        val nuevoPuntaje = (usuario?.puntajeAcumulado ?: 0) + mision.puntos
        val nuevoNivel = 1 + (nuevoPuntaje / 100)
        usuarioDao?.sumarPuntos(usuarioId, mision.puntos, nuevoNivel)

        val totalInsignias = progresoDao?.contarCompletadas(usuarioId) ?: 1

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

    open suspend fun crearPuntoConMision(
        punto: PuntoInteresEntity,
        titulo: String,
        descripcionPista: String,
        puntos: Int,
        tiempoEstimadoMin: Int,
        dificultad: String,
        insigniaNombre: String,
        insigniaEmoji: String
    ): Int = runInTransaction {
        val puntoId = puntoDao!!.insertar(punto).toInt()
        misionDao!!.insertar(
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
        puntoId
    }

    open suspend fun actualizarMisionYPunto(mision: MisionEntity, punto: PuntoInteresEntity) {
        val existeOtro = puntoDao?.existeOtroConCodigoQr(punto.codigoQr, punto.id) ?: false
        require(!existeOtro) {
            "Ya existe otro punto con ese código QR."
        }
        misionDao?.actualizar(mision)
        puntoDao?.actualizar(punto)
    }

    open suspend fun eliminarMision(mision: MisionEntity) {
        misionDao?.archivar(mision.id)
    }

    open fun generarCodigoQr(categoria: String): String {
        val prefijo = categoria.take(3).uppercase().ifBlank { "CQ0" }
        return "CQ-$prefijo-${java.util.UUID.randomUUID().toString().replace("-", "").take(12).uppercase()}"
    }
}
