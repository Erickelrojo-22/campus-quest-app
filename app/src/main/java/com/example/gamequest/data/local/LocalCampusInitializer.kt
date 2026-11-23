package com.example.gamequest.data.local

import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Seeds only an empty, new local catalog. Deleted missions are never re-created. */
class LocalCampusInitializer(
    private val database: AppDatabase,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun initialize() = withContext(ioDispatcher) { seedIfEmpty() }

    private suspend fun seedIfEmpty() = database.withTransaction {
        if (database.puntoInteresDao().contar() != 0 || database.misionDao().contar() != 0) return@withTransaction
        SeedData.puntosConMisiones().forEach { seed ->
            val pointId = database.puntoInteresDao().insertar(seed.punto)
            database.misionDao().insertar(seed.mision.copy(puntoInteresId = pointId.toInt()))
        }
    }
}
