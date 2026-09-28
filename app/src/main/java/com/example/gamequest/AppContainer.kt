package com.example.gamequest

import android.content.Context
import com.example.gamequest.data.local.AppDatabase
import com.example.gamequest.data.preferences.UserPreferencesRepository
import com.example.gamequest.data.repository.AuthRepository
import com.example.gamequest.data.repository.CampusRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * Contenedor de dependencias manual (sin framework de inyección): crea y
 * expone una única instancia de la base de datos y de los repositorios
 * mientras dure la aplicación.
 */
class AppContainer(context: Context) {

    private val applicationScope = CoroutineScope(SupervisorJob())

    private val database = AppDatabase.getInstance(context, applicationScope)

    val preferencesRepository = UserPreferencesRepository(context)

    val authRepository = AuthRepository(database.usuarioDao())

    val campusRepository = CampusRepository(
        database = database,
        puntoDao = database.puntoInteresDao(),
        misionDao = database.misionDao(),
        progresoDao = database.progresoMisionDao(),
        usuarioDao = database.usuarioDao()
    )
}
