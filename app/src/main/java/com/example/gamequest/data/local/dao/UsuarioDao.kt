package com.example.gamequest.data.local.dao

import androidx.room.Transaction
import androidx.room.Upsert
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.gamequest.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuario WHERE remoteId = :remoteId LIMIT 1")
    suspend fun buscarPorRemoteId(remoteId: Int): UsuarioEntity?

    @Transaction
    suspend fun guardarUsuarioRemoto(usuario: UsuarioEntity): UsuarioEntity {
        val remoteId = requireNotNull(usuario.remoteId)
        val anterior = buscarPorRemoteId(remoteId)
        val mapped = usuario.copy(id = anterior?.id ?: 0)
        if (anterior != null) {
            actualizar(mapped)
            return mapped
        }
        return mapped.copy(id = insertar(mapped).toInt())
    }

    @Upsert
    suspend fun guardarRemotos(items: List<UsuarioEntity>)


    @Insert
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Update
    suspend fun actualizar(usuario: UsuarioEntity)

    @Query("UPDATE usuario SET contrasenaHash = :hash WHERE id = :usuarioId")
    suspend fun actualizarContrasena(usuarioId: Int, hash: String)

    @Query("SELECT * FROM usuario WHERE correoInstitucional = :correo COLLATE NOCASE LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?


    @Query("SELECT * FROM usuario WHERE nombres = :nombre COLLATE NOCASE LIMIT 1")
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
