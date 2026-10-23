package com.example.gamequest

import android.content.Context
import androidx.room.Room
import com.example.gamequest.data.local.AppDatabase
import com.example.gamequest.data.local.SeedData
import com.example.gamequest.data.local.entity.*
import com.example.gamequest.data.model.RankingEntry
import com.example.gamequest.data.preferences.*
import com.example.gamequest.data.remote.*
import com.example.gamequest.data.repository.AuthRepository
import com.example.gamequest.data.session.SessionCoordinator
import com.example.gamequest.util.SoundEffectManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject

/** Disposable fixtures; no object here constructs a production HTTP client. */
class FakeCampusQuestDependencies(context: Context, val db: AppDatabase = Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).build()) : CampusQuestDependencies, AutoCloseable {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val preferences = MutableStateFlow(UserPreferences())
    override val preferencesRepository = object : UserPreferencesRepository() {
        override val preferencias = preferences
        override suspend fun setUsuarioActivoId(id: Int) { preferences.value = preferences.value.copy(usuarioActivoId=id) }
        override suspend fun cerrarSesion() = setUsuarioActivoId(-1)
    }
    val tokens = object : TokenStore {
        override var snapshot = SessionSnapshot(null)
        override fun save(value: String,localUserId: Int?) { snapshot=SessionSnapshot(value,localUserId) }
        override fun clear() { snapshot=SessionSnapshot(null) }
        override fun clearIfCurrent(expectedToken: String): Boolean { if(token != expectedToken) return false; clear(); return true }
    }
    var authFailure: Int? = null
    var meFailure: Int? = null
    val remoteUser = RemoteUserDto(51,"Remote", "", "Software",30,1,"estudiante")
    var remoteProgress: List<RemoteProgressDto> = emptyList()
    val gateway = object : CampusGateway {
        override val tokens = this@FakeCampusQuestDependencies.tokens
        override var onUnauthorized: (String) -> Unit = {}
        override suspend fun authenticate(path: String,body: JSONObject): AuthPayload {
            authFailure?.let { throw ApiException(it,"Rejected") }
            return AuthPayload("remote-51",remoteUser)
        }
        override suspend fun me(): RemoteUserDto {
            meFailure?.let { status ->
                if(status==401) tokens.token?.let(onUnauthorized)
                throw ApiException(status,"Expired")
            }
            return remoteUser
        }
        override suspend fun logout(token: String) = Unit
        override suspend fun puntos() = listOf(SeedData.puntosConMisiones().first().punto.copy(id=1))
        override suspend fun misiones() = listOf(SeedData.puntosConMisiones().first().mision.copy(id=1,puntoInteresId=1))
        override suspend fun progreso(id: Int) = remoteProgress
        override suspend fun completar(id: Int,mision: Int,codigo: String): RemoteProgressDto = error("Unexpected mutation")
        override suspend fun ranking(): List<RankingEntry> = emptyList()
    }
    override val campusRepository = RemoteCampusRepository(db,gateway)
    override val sessionCoordinator = SessionCoordinator(tokens,preferencesRepository,campusRepository,db.usuarioDao()::buscarPorId,gateway::logout,scope)
    override val authRepository = AuthRepository(db.usuarioDao(),gateway,sessionCoordinator)
    override val soundEffectManager = SoundEffectManager(context,preferencesRepository,scope)
    init { gateway.onUnauthorized = { token -> scope.launch { sessionCoordinator.unauthorized(token) } } }
    suspend fun seed() {
        db.usuarioDao().insertar(UsuarioEntity(8,"Account A","","","",remoteId=51))
        db.usuarioDao().insertar(UsuarioEntity(9,"Account B","","","",remoteId=77))
        val seed=SeedData.puntosConMisiones().first()
        val point=db.puntoInteresDao().insertar(seed.punto)
        db.misionDao().insertar(seed.mision.copy(id=1,puntoInteresId=point.toInt()))
    }
    override fun close() { scope.cancel(); soundEffectManager.release(); db.close() }
}
