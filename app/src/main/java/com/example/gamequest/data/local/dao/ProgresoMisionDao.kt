package com.example.gamequest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gamequest.data.local.entity.ProgresoMisionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgresoMisionDao {

    @Insert
    suspend fun insertar(progreso: ProgresoMisionEntity): Long

    @Query("SELECT * FROM progreso_mision WHERE usuarioId = :usuarioId")
    fun observarPorUsuario(usuarioId: Int): Flow<List<ProgresoMisionEntity>>

    @Query("SELECT misionId FROM progreso_mision WHERE usuarioId = :usuarioId")
    fun observarMisionesCompletadasIds(usuarioId: Int): Flow<List<Int>>

    @Query("SELECT * FROM progreso_mision WHERE usuarioId = :usuarioId AND misionId = :misionId LIMIT 1")
    suspend fun buscar(usuarioId: Int, misionId: Int): ProgresoMisionEntity?

    @Query("SELECT COUNT(*) FROM progreso_mision WHERE usuarioId = :usuarioId")
    fun observarTotalCompletadas(usuarioId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM progreso_mision WHERE usuarioId = :usuarioId")
    suspend fun contarCompletadas(usuarioId: Int): Int
}
