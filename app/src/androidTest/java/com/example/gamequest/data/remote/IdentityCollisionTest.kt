package com.example.gamequest.data.remote

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gamequest.data.local.AppDatabase
import com.example.gamequest.data.local.SeedData
import com.example.gamequest.data.local.entity.*
import com.example.gamequest.data.repository.AuthRepository
import com.example.gamequest.data.repository.AuthResult
import com.example.gamequest.data.repository.ValidacionQrResult
import java.io.IOException
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class IdentityCollisionTest {
    private lateinit var db: AppDatabase
    private val tokens = object : TokenStore {
        override var snapshot = SessionSnapshot(null)
        override fun save(value: String, localUserId: Int?) { snapshot = SessionSnapshot(value, localUserId) }
        override fun clear() { snapshot = SessionSnapshot(null) }
        override fun clearIfCurrent(expectedToken: String): Boolean { if (token != expectedToken) return false; clear(); return true }
    }
    private val gateway = object : CampusGateway {
        override val tokens = this@IdentityCollisionTest.tokens
        override var onUnauthorized: (String) -> Unit = {}
        override suspend fun authenticate(path: String, body: JSONObject) = AuthPayload("server-token", remote(51))
        override suspend fun me() = remote(51)
        override suspend fun logout(token: String) = Unit
        override suspend fun puntos(): List<PuntoInteresEntity> = emptyList()
        override suspend fun misiones(): List<MisionEntity> = emptyList()
        override suspend fun progreso(id: Int): List<RemoteProgressDto> = emptyList()
        override suspend fun completar(id: Int, mision: Int, codigo: String): RemoteProgressDto = error("Not used")
        override suspend fun ranking(): List<com.example.gamequest.data.model.RankingEntry> = emptyList()
    }

    private fun remote(id: Int) = RemoteUserDto(id, "Remote", "", "", 0, 1, "estudiante")
    private fun user(id: Int, name: String) = UsuarioEntity(id, name, "", "", "")

    @Before fun setup() = runBlocking {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java).build()
        val seed = SeedData.puntosConMisiones().first()
        val point = db.puntoInteresDao().insertar(seed.punto)
        db.misionDao().insertar(seed.mision.copy(id = 1, puntoInteresId = point.toInt()))
        Unit
    }
    @After fun cleanup() { db.close() }

    @Test fun visitanteYRemoto_mismoNumero_noSeMezclan() = runBlocking {
        db.usuarioDao().insertar(user(51, "Offline Visitor"))
        db.progresoMisionDao().insertar(ProgresoMisionEntity(usuarioId = 51, misionId = 1, fechaHora = 10, puntosObtenidos = 20, codigoQrValidado = "TEST"))
        val result = AuthRepository(db.usuarioDao(), gateway).entrarConNombre("Remote", false) as AuthResult.Exito
        assertNotEquals("Remote id is not a local primary key", 51, result.usuario.id)
        assertEquals("Offline Visitor", db.usuarioDao().buscarPorId(51)?.nombres)
        assertEquals(1, db.progresoMisionDao().contarCompletadas(51))
        assertEquals(0, db.progresoMisionDao().contarCompletadas(result.usuario.id))
    }
    @Test fun eventoLocalYRemoto_mismoNumero_noSeSobrescriben() = runBlocking {
        db.usuarioDao().insertar(user(17, "Visitor"))
        db.usuarioDao().insertar(user(42, "Remote"))
        db.progresoMisionDao().insertar(ProgresoMisionEntity(id = 1, usuarioId = 17, misionId = 1, fechaHora = 10, puntosObtenidos = 20, codigoQrValidado = "LOCAL"))
        persistRemoteProgress()
        assertEquals(1, db.progresoMisionDao().contarCompletadas(17))
        assertEquals("LOCAL", db.progresoMisionDao().buscar(17, 1)?.codigoQrValidado)
    }
    private suspend fun persistRemoteProgress() {
        val store = LocalIdentityStore(db)
        val user = store.guardarUsuario(remote(42))
        store.guardarProgreso(RemoteProgressDto(1, 42, 1, 20, 30, "REMOTE", "completada"), user.id)
    }
    @Test fun snapshotRepetido_noDuplicaCuentaNiEvento() = runBlocking {
        val store = LocalIdentityStore(db)
        val first = store.guardarUsuario(remote(51))
        val event = RemoteProgressDto(1, 51, 1, 20, 30, "REMOTE", "completada")
        val a = store.guardarProgreso(event, first.id)
        val second = store.guardarUsuario(remote(51))
        val b = store.guardarProgreso(event, second.id)
        assertEquals(first.id, second.id)
        assertEquals(a.id, b.id)
        assertEquals(1, db.progresoMisionDao().contarCompletadas(first.id))
    }
    @Test fun progresoDeOtroUsuario_seRechaza() = runBlocking {
        val store = LocalIdentityStore(db)
        val user = store.guardarUsuario(remote(51))
        try { store.guardarProgreso(RemoteProgressDto(1, 52, 1, 20, 30, "OTHER", "completada"), user.id); fail("Expected mismatch") }
        catch (_: IllegalArgumentException) { }
        assertEquals(0, db.progresoMisionDao().contarCompletadas(user.id))
    }
    @Test fun solicitudProgreso_usaRemoteId() = runBlocking {
        db.usuarioDao().insertar(user(8, "Bound").copy(remoteId = 51))
        tokens.save("server-token", 8)
        var requested = -1
        val api = object : CampusGateway by gateway {
            override suspend fun progreso(id: Int): List<RemoteProgressDto> { requested = id; return emptyList() }
        }
        val repo = RemoteCampusRepository(db, api)
        assertEquals(8, repo.resolveIdentity().id)
        repo.syncCatalogAndProgress(8)
        assertEquals(51, requested)
    }
    @Test fun completarConfirmado_perfilSinRed_conservaEventoYNoRepite() = runBlocking {
        db.usuarioDao().insertar(user(8, "Bound").copy(remoteId = 51))
        tokens.save("server-token", 8)
        var profiles = 0
        var completions = 0
        val api = object : CampusGateway by gateway {
            override suspend fun me(): RemoteUserDto {
                profiles++
                if (profiles == 2) throw IOException("Profile unavailable after accepted completion")
                return remote(51).copy(puntajeAcumulado = if (profiles > 2) 30 else 0)
            }
            override suspend fun completar(id: Int, mision: Int, codigo: String): RemoteProgressDto {
                completions++
                if (completions > 1) throw ApiException(409, "Already completed")
                return RemoteProgressDto(1, id, mision, 20, 30, codigo, "completada")
            }
        }
        val repo = RemoteCampusRepository(db, api)
        val code = requireNotNull(db.puntoInteresDao().buscarPorId(1)).codigoQr
        val result = repo.validarCodigo(code, 8) as ValidacionQrResult.MisionCompletada
        assertEquals(30, result.puntosGanados)
        assertEquals(30, result.nuevoPuntajeTotal)
        assertEquals(1, db.progresoMisionDao().buscar(8, 1)?.remoteId)
        assertEquals(1, db.progresoMisionDao().contarCompletadas(8))
        assertEquals(ValidacionQrResult.YaCompletada, repo.validarCodigo(code, 8))
        assertEquals(1, completions)
        assertEquals(30, db.usuarioDao().buscarPorId(8)?.puntajeAcumulado)
    }
}
