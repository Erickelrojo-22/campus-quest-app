package com.example.gamequest

import android.content.Context
import com.example.gamequest.data.local.AppDatabase
import com.example.gamequest.data.local.LocalCampusInitializer
import com.example.gamequest.data.preferences.UserPreferencesRepository
import com.example.gamequest.data.repository.AuthRepository
import com.example.gamequest.data.repository.CampusRepository
import com.example.gamequest.data.repository.OfflineFirstCampusRepository
import com.example.gamequest.data.repository.LocalCrudAuthorizer
import com.example.gamequest.data.repository.LocalTutorSetupRepository
import com.example.gamequest.data.repository.TutorSetupRepository
import com.example.gamequest.data.remote.CampusApi
import com.example.gamequest.data.remote.SessionTokenStore
import com.example.gamequest.data.remote.RemoteCampusRepository
import com.example.gamequest.data.session.LocalSessionAccess
import com.example.gamequest.data.session.SessionCoordinator
import com.example.gamequest.data.session.SessionAccess
import com.example.gamequest.di.CampusDataMode
import com.example.gamequest.util.SoundEffectManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Contenedor de dependencias manual (sin framework de inyección): crea y
 * expone una única instancia de la base de datos y de los repositorios
 * mientras dure la aplicación.
 */
class AppContainer(context: Context) : CampusQuestDependencies {

    private val applicationScope = CoroutineScope(SupervisorJob())

    /** Único dispatcher de E/S que comparten todos los repositorios y fuentes de datos. */
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    private val mode = CampusDataMode.current()
    private val database = if (mode == CampusDataMode.LOCAL) AppDatabase.getInstance(context) else AppDatabase.getRemoteInstance(context)
    private val tokens = SessionTokenStore(context)
    val api: CampusApi? = if (mode == CampusDataMode.REMOTE) CampusApi(tokens) else null

    override val preferencesRepository = UserPreferencesRepository(context, ioDispatcher)

    override val soundEffectManager = SoundEffectManager(context, preferencesRepository, applicationScope)

    override val campusRepository: CampusRepository = if (mode == CampusDataMode.REMOTE) {
        RemoteCampusRepository(database, requireNotNull(api), ioDispatcher)
    } else {
        OfflineFirstCampusRepository(database, database.puntoInteresDao(), database.misionDao(), database.progresoMisionDao(),
            database.usuarioDao(), LocalCrudAuthorizer(tokens, database.usuarioDao()), ioDispatcher)
    }

    private val sessionAccess: SessionAccess = if (mode == CampusDataMode.REMOTE) {
        campusRepository as RemoteCampusRepository
    } else {
        LocalSessionAccess(tokens, database.usuarioDao(), ioDispatcher)
    }
    private val revokeRemote: suspend (String) -> Unit = { token -> api?.logout(token) ?: Unit }

    override val sessionCoordinator = SessionCoordinator(
        tokens, preferencesRepository, sessionAccess, database.usuarioDao()::buscarPorId, revokeRemote, applicationScope
    )

    override val authRepository = AuthRepository(database.usuarioDao(), api, sessionCoordinator, sessionCoordinator, ioDispatcher)
    override val tutorSetupRepository: TutorSetupRepository? = if (mode == CampusDataMode.LOCAL) {
        LocalTutorSetupRepository(database, authRepository, sessionCoordinator, ioDispatcher)
    } else null

    init {
        if (mode == CampusDataMode.LOCAL) applicationScope.launch { LocalCampusInitializer(database, ioDispatcher).initialize() }
        api?.onUnauthorized = { token -> applicationScope.launch { sessionCoordinator.unauthorized(token) } }
    }
}
