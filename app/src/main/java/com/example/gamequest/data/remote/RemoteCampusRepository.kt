package com.example.gamequest.data.remote

import androidx.room.withTransaction
import com.example.gamequest.data.local.AppDatabase
import com.example.gamequest.data.local.entity.*
import com.example.gamequest.data.model.RankingEntry
import com.example.gamequest.data.repository.CampusRepository
import com.example.gamequest.data.repository.ValidacionQrResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Translates server identities to local primary keys at the cache boundary. */
class RemoteCampusRepository(private val db: AppDatabase, private val api: CampusGateway) : CampusRepository(
    database = db, puntoDao = db.puntoInteresDao(), misionDao = db.misionDao(),
    progresoDao = db.progresoMisionDao(), usuarioDao = db.usuarioDao()
), com.example.gamequest.data.session.SessionAccess {
    override suspend fun resolverIdentidad() = resolveIdentity()
    override suspend fun sincronizarCatalogoYProgreso(localUserId: Int) = syncCatalogAndProgress(localUserId)
    private val lock = Mutex()
    private val identities = LocalIdentityStore(db)
    private val ranking = MutableStateFlow<List<RankingEntry>>(emptyList())
    override fun observarRanking(): Flow<List<RankingEntry>> =
        combine(ranking, db.usuarioDao().observarRanking()) { remotos, locales ->
            if (remotos.isNotEmpty()) remotos else locales.map(RankingEntry::local)
        }

    suspend fun resolveIdentity(): UsuarioEntity {
        val snapshot = api.tokens.snapshot
        val token = snapshot.token ?: throw ApiException(401, "Inicia sesión para continuar.")
        if (token.startsWith("offline-")) {
            val id = token.removePrefix("offline-visitor-").toIntOrNull()
            val user = id?.let { db.usuarioDao().buscarPorId(it) }
            require(user != null && user.remoteId == null && user.contrasenaHash.isBlank() &&
                user.correoInstitucional.isBlank() && user.rol == Rol.ESTUDIANTE &&
                (snapshot.localUserId == null || snapshot.localUserId == id)) { "Visitante local inválido." }
            return user
        }
        val dto = api.me()
        return db.withTransaction {
            check(api.tokens.snapshot == snapshot) { "La sesión cambió." }
            val bound = snapshot.localUserId?.let { db.usuarioDao().buscarPorId(it) }
            require(snapshot.localUserId == null || bound?.remoteId == dto.remoteId) { "El vínculo de sesión no coincide." }
            val resolved = identities.guardarUsuario(dto)
            check(api.tokens.snapshot == snapshot) { "La sesión cambió." }
            resolved
        }
    }

    suspend fun syncCatalogAndProgress(localUserId: Int) = lock.withLock {
        val snapshot = api.tokens.snapshot
        val token = snapshot.token ?: throw ApiException(401, "Inicia sesión para continuar.")
        if (token.startsWith("offline-")) return@withLock
        val user = requireNotNull(db.usuarioDao().buscarPorId(localUserId))
        require(snapshot.localUserId == localUserId) { "La sesión no pertenece a esta cuenta." }
        val remoteId = requireNotNull(user.remoteId)
        val points = api.puntos()
        val missions = api.misiones()
        val progress = api.progreso(remoteId)
        val leaders = api.ranking()
        db.withTransaction {
            check(api.tokens.snapshot == snapshot) { "La sesión cambió." }
            db.puntoInteresDao().guardarRemotos(points)
            db.misionDao().archivarCache()
            db.misionDao().guardarRemotos(missions)
            progress.forEach { identities.guardarProgreso(it, user.id) }
            check(api.tokens.snapshot == snapshot) { "La sesión cambió." }
        }
        if (api.tokens.snapshot == snapshot) ranking.value = leaders
    }

    suspend fun refresh(): UsuarioEntity {
        val user = resolveIdentity()
        if (api.tokens.snapshot.localUserId == user.id) syncCatalogAndProgress(user.id)
        return user
    }

    override suspend fun validarCodigo(codigo: String, usuarioId: Int): ValidacionQrResult = lock.withLock {
        val snapshot = api.tokens.snapshot
        val token = snapshot.token ?: throw ApiException(401, "Inicia sesión para continuar.")
        require(snapshot.localUserId == usuarioId) { "Inicia sesión con tu propia cuenta." }
        if (token.startsWith("offline-")) {
            require(resolveIdentity().id == usuarioId)
            return@withLock super.validarCodigo(codigo, usuarioId)
        }
        val user = try { resolveIdentity() } catch (e: CancellationException) { throw e
        } catch (e: Exception) {
            if (e.permiteFallbackOffline() && api.tokens.snapshot == snapshot) return@withLock super.validarCodigo(codigo, usuarioId)
            throw e
        }
        require(user.id == usuarioId)
        val point = db.puntoInteresDao().buscarPorCodigoQr(codigo.trim().uppercase())
            ?: return@withLock ValidacionQrResult.CodigoNoReconocido
        val missions = db.misionDao().buscarPorPunto(point.id)
        val mission = missions.firstOrNull { db.progresoMisionDao().buscar(user.id, it.id) == null }
            ?: return@withLock if (missions.isEmpty()) ValidacionQrResult.SinMisionAsociada else ValidacionQrResult.YaCompletada
        // Keep persistence outside the transport catch so a disk error cannot award points.
        val event = try { api.completar(requireNotNull(user.remoteId), mission.id, codigo.trim().uppercase())
        } catch (e: CancellationException) { throw e
        } catch (e: Exception) {
            if (e.permiteFallbackOffline() && api.tokens.snapshot == snapshot) return@withLock super.validarCodigo(codigo, usuarioId)
            throw e
        }
        val updated = api.me()
        require(updated.remoteId == user.remoteId) { "La cuenta cambió." }
        db.withTransaction {
            check(api.tokens.snapshot == snapshot) { "La sesión cambió." }
            identities.guardarUsuario(updated)
            identities.guardarProgreso(event, user.id)
            check(api.tokens.snapshot == snapshot) { "La sesión cambió." }
        }
        ValidacionQrResult.MisionCompletada(mission, point, event.puntosObtenidos,
            updated.puntajeAcumulado, updated.nivel, db.progresoMisionDao().contarCompletadas(user.id))
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
