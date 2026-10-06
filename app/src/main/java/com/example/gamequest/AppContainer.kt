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

/**
 * Contenedor de dependencias manual (sin framework de inyección): crea y
 * expone una única instancia de la base de datos y de los repositorios
 * mientras dure la aplicación.
 */
class AppContainer(context: Context) {

    private val applicationScope = CoroutineScope(SupervisorJob())

    private val database = AppDatabase.getRemoteInstance(context)

    val api = CampusApi(SessionTokenStore(context))

    val preferencesRepository = UserPreferencesRepository(context)

    val soundEffectManager = SoundEffectManager(context, preferencesRepository, applicationScope)

    val authRepository = AuthRepository(database.usuarioDao(), api)

    val campusRepository = RemoteCampusRepository(database, api)
}
