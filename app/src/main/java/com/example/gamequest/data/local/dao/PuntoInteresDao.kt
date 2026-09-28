package com.example.gamequest.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PuntoInteresDao {

    @Insert
    suspend fun insertar(punto: PuntoInteresEntity): Long

    @Update
    suspend fun actualizar(punto: PuntoInteresEntity)

    @Delete
    suspend fun eliminar(punto: PuntoInteresEntity)

    @Query("SELECT * FROM punto_interes ORDER BY nombre")
    fun observarTodos(): Flow<List<PuntoInteresEntity>>

    @Query("SELECT * FROM punto_interes WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): PuntoInteresEntity?

    @Query("SELECT * FROM punto_interes WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<PuntoInteresEntity?>

    @Query("SELECT * FROM punto_interes WHERE codigoQr = :codigo LIMIT 1")
    suspend fun buscarPorCodigoQr(codigo: String): PuntoInteresEntity?

    @Query("SELECT COUNT(*) FROM punto_interes")
    suspend fun contar(): Int

    @Query("SELECT EXISTS(SELECT 1 FROM punto_interes WHERE codigoQr = :codigo AND id != :puntoId)")
    suspend fun existeOtroConCodigoQr(codigo: String, puntoId: Int): Boolean
}
