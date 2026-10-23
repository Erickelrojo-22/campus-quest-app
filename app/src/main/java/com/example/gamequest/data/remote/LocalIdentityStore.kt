package com.example.gamequest.data.remote

import androidx.room.withTransaction
import com.example.gamequest.data.local.AppDatabase
import com.example.gamequest.data.local.entity.ProgresoMisionEntity
import com.example.gamequest.data.local.entity.UsuarioEntity

/** Only this boundary translates server identities into local foreign keys. */
class LocalIdentityStore(private val db: AppDatabase) {
    suspend fun guardarUsuario(dto: RemoteUserDto): UsuarioEntity =
        db.usuarioDao().guardarUsuarioRemoto(dto.toLocal())

    suspend fun guardarProgreso(dto: RemoteProgressDto, localUserId: Int): ProgresoMisionEntity = db.withTransaction {
        val user = requireNotNull(db.usuarioDao().buscarPorId(localUserId))
        require(user.remoteId == dto.remoteUserId) { "El progreso pertenece a otra cuenta." }
        val byRemote = db.progresoMisionDao().buscarPorRemoteId(dto.remoteId)
        require(byRemote == null || (byRemote.usuarioId == localUserId && byRemote.misionId == dto.misionId)) {
            "La identidad del progreso no coincide."
        }
        val previous = byRemote ?: db.progresoMisionDao().buscar(localUserId, dto.misionId)
        require(previous?.remoteId == null || previous.remoteId == dto.remoteId) { "Progreso remoto inconsistente." }
        val row = ProgresoMisionEntity(previous?.id ?: 0, localUserId, dto.misionId, dto.fechaHora,
            dto.puntosObtenidos, dto.codigoQrValidado, dto.estado, dto.remoteId)
        if (previous == null) row.copy(id = db.progresoMisionDao().insertar(row).toInt())
        else { db.progresoMisionDao().guardarRemotos(listOf(row)); row }
    }
}
