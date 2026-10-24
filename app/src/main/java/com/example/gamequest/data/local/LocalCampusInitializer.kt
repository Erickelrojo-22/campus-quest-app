package com.example.gamequest.data.local

import androidx.room.withTransaction

/** Seeds only an empty, new local catalog. Deleted missions are never re-created. */
class LocalCampusInitializer(private val database: AppDatabase) {
    suspend fun initialize() = database.withTransaction {
        if (database.puntoInteresDao().contar() != 0 || database.misionDao().contar() != 0) return@withTransaction
        SeedData.puntosConMisiones().forEach { seed ->
            val pointId = database.puntoInteresDao().insertar(seed.punto)
            database.misionDao().insertar(seed.mision.copy(puntoInteresId = pointId.toInt()))
        }
    }
}
