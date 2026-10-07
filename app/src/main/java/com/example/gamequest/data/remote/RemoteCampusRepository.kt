package com.example.gamequest.data.remote

import androidx.room.withTransaction
import com.example.gamequest.data.local.AppDatabase
import com.example.gamequest.data.local.entity.*
import com.example.gamequest.data.repository.CampusRepository
import com.example.gamequest.data.repository.ValidacionQrResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Room is a cache; only the API awards points or changes the shared catalog. */
class RemoteCampusRepository(private val db: AppDatabase, private val api: CampusApi) : CampusRepository(
    database = db, puntoDao = db.puntoInteresDao(), misionDao = db.misionDao(),
    progresoDao = db.progresoMisionDao(), usuarioDao = db.usuarioDao()
) {
    private val lock = Mutex()
    private val ranking = MutableStateFlow<List<UsuarioEntity>>(emptyList())
    override fun observarRanking(): Flow<List<UsuarioEntity>> {
        return kotlinx.coroutines.flow.combine(ranking, db.usuarioDao().observarRanking()) { remotos, locales ->
            if (remotos.isNotEmpty()) remotos else locales
        }
    }

    suspend fun refresh(): UsuarioEntity = lock.withLock {
        val token = api.tokens.token
        if (token != null && token.startsWith("offline-")) {
            val id = token.removePrefix("offline-visitor-").toIntOrNull()
            if (id != null) {
                db.usuarioDao().buscarPorId(id)?.let { return@withLock it }
            }
        }
        synchronize()
    }

    private suspend fun synchronize(): UsuarioEntity {
        val token = api.tokens.token ?: throw ApiException(401, "Inicia sesión para continuar.")
        val user = api.me()
        val points = api.puntos()
        val missions = api.misiones()
        val progress = api.progreso(user.id)
        val leaders = api.ranking()
        db.withTransaction {
            check(api.tokens.token == token) { "La sesión cambió. Inicia sesión nuevamente." }
            db.usuarioDao().guardarRemotos(listOf(user))
            db.puntoInteresDao().guardarRemotos(points)
            db.misionDao().archivarCache()
            db.misionDao().guardarRemotos(missions)
            db.progresoMisionDao().guardarRemotos(progress)
        }
        ranking.value = leaders
        return user
    }

    override suspend fun validarCodigo(codigo: String, usuarioId: Int): ValidacionQrResult = lock.withLock {
        val token = api.tokens.token
        if (token == null || token.startsWith("offline-")) {
            return super.validarCodigo(codigo, usuarioId)
        }
        val user = try {
            synchronize()
        } catch (_: Exception) {
            return super.validarCodigo(codigo, usuarioId)
        }
        check(user.id == usuarioId) { "Inicia sesión con tu propia cuenta." }
        val point = db.puntoInteresDao().buscarPorCodigoQr(codigo.trim().uppercase())
            ?: return@withLock ValidacionQrResult.CodigoNoReconocido
        val missions = db.misionDao().buscarPorPunto(point.id)
        val mission = missions.firstOrNull { db.progresoMisionDao().buscar(user.id, it.id) == null }
            ?: return@withLock if (missions.isEmpty()) ValidacionQrResult.SinMisionAsociada else ValidacionQrResult.YaCompletada
        try {
            val event = api.completar(user.id, mission.id, codigo.trim().uppercase())
            val updated = api.me()
            db.withTransaction {
                check(api.tokens.token == token) { "La sesión cambió." }
                db.usuarioDao().guardarRemotos(listOf(updated))
                db.progresoMisionDao().guardarRemotos(listOf(event))
            }
            ValidacionQrResult.MisionCompletada(mission, point, event.puntosObtenidos,
                updated.puntajeAcumulado, updated.nivel, db.progresoMisionDao().contarCompletadas(user.id))
        } catch (_: Exception) {
            super.validarCodigo(codigo, usuarioId)
        }
    }

    override suspend fun crearPuntoConMision(punto: PuntoInteresEntity, titulo: String, descripcionPista: String,
        puntos: Int, tiempoEstimadoMin: Int, dificultad: String, insigniaNombre: String, insigniaEmoji: String): Int =
        throw ApiException(403, "Gestiona el catálogo desde el panel web de administración.")
    override suspend fun actualizarMisionYPunto(mision: MisionEntity, punto: PuntoInteresEntity) {
        throw ApiException(403, "Gestiona el catálogo desde el panel web de administración.")
    }
    override suspend fun eliminarMision(mision: MisionEntity) {
        throw ApiException(403, "Gestiona el catálogo desde el panel web de administración.")
    }
}
