package com.example.gamequest.data.local.dao

import androidx.room.Upsert
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gamequest.data.local.entity.MisionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MisionDao {

    @Upsert
    suspend fun guardarRemotos(items: List<MisionEntity>)


    @Insert
    suspend fun insertar(mision: MisionEntity): Long

    @Update
    suspend fun actualizar(mision: MisionEntity)

    @Query("UPDATE mision SET activa = 0")
    suspend fun archivarCache()

    @Query("SELECT * FROM mision ORDER BY id")
    fun observarHistorialCatalogo(): Flow<List<MisionEntity>>

    @Query("UPDATE mision SET activa = 0 WHERE id = :misionId")
    suspend fun archivar(misionId: Int)

    @Query("SELECT * FROM mision WHERE activa = 1 ORDER BY id")
    fun observarTodas(): Flow<List<MisionEntity>>

    @Query("SELECT * FROM mision WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): MisionEntity?

    @Query("SELECT * FROM mision WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<MisionEntity?>

    @Query("SELECT * FROM mision WHERE puntoInteresId = :puntoId AND activa = 1")
    suspend fun buscarPorPunto(puntoId: Int): List<MisionEntity>

    @Query("SELECT COUNT(*) FROM mision")
    suspend fun contar(): Int
}
