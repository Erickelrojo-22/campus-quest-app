package com.example.gamequest.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gamequest.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Update
    suspend fun actualizar(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuario WHERE correoInstitucional = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuario WHERE LOWER(nombres) = LOWER(:nombre) LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): UsuarioEntity?

    @Query("SELECT * FROM usuario WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): UsuarioEntity?

    @Query("SELECT * FROM usuario WHERE id = :id LIMIT 1")
    fun observarPorId(id: Int): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuario ORDER BY puntajeAcumulado DESC")
    fun observarRanking(): Flow<List<UsuarioEntity>>

    @Query("UPDATE usuario SET puntajeAcumulado = puntajeAcumulado + :puntos, nivel = :nivel WHERE id = :usuarioId")
    suspend fun sumarPuntos(usuarioId: Int, puntos: Int, nivel: Int)
}
