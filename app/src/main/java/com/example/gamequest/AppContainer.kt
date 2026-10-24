package com.example.gamequest

import android.content.Context
import com.example.gamequest.data.local.AppDatabase
import com.example.gamequest.data.preferences.UserPreferencesRepository
import com.example.gamequest.data.repository.AuthRepository
import com.example.gamequest.data.remote.CampusApi
import com.example.gamequest.data.remote.SessionTokenStore
import com.example.gamequest.data.remote.RemoteCampusRepository
import com.example.gamequest.util.SoundEffectManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Contenedor de dependencias manual (sin framework de inyección): crea y
 * expone una única instancia de la base de datos y de los repositorios
 * mientras dure la aplicación.
 */
class AppContainer(context: Context) : CampusQuestDependencies {

    private val applicationScope = CoroutineScope(SupervisorJob())

    private val database = AppDatabase.getRemoteInstance(context)

    val api = CampusApi(SessionTokenStore(context))

    override val preferencesRepository = UserPreferencesRepository(context)

    override val soundEffectManager = SoundEffectManager(context, preferencesRepository, applicationScope)

    override val campusRepository = RemoteCampusRepository(database, api)

    override val sessionCoordinator = com.example.gamequest.data.session.SessionCoordinator(
        api.tokens, preferencesRepository, campusRepository, database.usuarioDao()::buscarPorId,
        api::logout, applicationScope
    )

    override val authRepository = AuthRepository(database.usuarioDao(), api, sessionCoordinator)

    init { api.onUnauthorized = { token -> applicationScope.launch { sessionCoordinator.unauthorized(token) } } }
}
