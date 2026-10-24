package com.example.gamequest.data.session

import com.example.gamequest.data.local.dao.UsuarioDao
import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.remote.TokenStore

/** Resolves a Room-backed session and never performs network I/O. */
class LocalSessionAccess(private val tokens: TokenStore, private val users: UsuarioDao) : SessionAccess {
    override suspend fun resolverIdentidad(): UsuarioEntity {
        val snapshot = tokens.snapshot
        val token = requireNotNull(snapshot.token) { "No hay sesión local." }
        val id = requireNotNull(snapshot.localUserId) { "La sesión local no tiene usuario." }
        val user = requireNotNull(users.buscarPorId(id)) { "La cuenta local ya no existe." }
        val visitor = token == "offline-visitor-$id" && user.remoteId == null &&
            user.contrasenaHash.isBlank() && user.correoInstitucional.isBlank() && user.rol == Rol.ESTUDIANTE
        val account = token.startsWith("local-account-") && user.remoteId == null &&
            user.contrasenaHash.isNotBlank() && user.correoInstitucional.isNotBlank()
        require(visitor || account) { "La sesión local no es válida." }
        return user
    }

    override suspend fun sincronizarCatalogoYProgreso(localUserId: Int) = Unit
}
