package com.example.gamequest.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gamequest.data.local.entity.MisionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MisionDao {

    @Insert
    suspend fun insertar(mision: MisionEntity): Long

    @Update
    suspend fun actualizar(mision: MisionEntity)

    @Delete
    suspend fun eliminar(mision: MisionEntity)

    @Query("SELECT * FROM mision ORDER BY id")
    fun observarTodas(): Flow<List<MisionEntity>>

    @Query("SELECT * FROM mision WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): MisionEntity?

    @Query("SELECT * FROM mision WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<MisionEntity?>

    @Query("SELECT * FROM mision WHERE puntoInteresId = :puntoId")
    suspend fun buscarPorPunto(puntoId: Int): List<MisionEntity>

    @Query("SELECT COUNT(*) FROM mision")
    suspend fun contar(): Int
}
