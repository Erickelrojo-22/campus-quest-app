package com.example.gamequest.data.repository

import com.example.gamequest.data.local.dao.UsuarioDao
import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.data.remote.TokenStore
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Enforces local tutor identity at the repository boundary, not only in Compose. */
class LocalCrudAuthorizer(private val tokens: TokenStore, private val users: UsuarioDao) {
    private val mutex = Mutex()

    suspend fun <T> withTutor(block: suspend () -> T): T = mutex.withLock {
        val snapshot = tokens.snapshot
        val id = requireNotNull(snapshot.localUserId) { "Inicia sesión como tutor para administrar el catálogo." }
        require(snapshot.token.orEmpty().startsWith("local-account-")) { "Esta sesión no puede administrar el catálogo." }
        val user = requireNotNull(users.buscarPorId(id)) { "La cuenta local ya no existe." }
        require(user.remoteId == null && user.rol == Rol.TUTOR) { "Solo el tutor local puede administrar el catálogo." }
        block()
    }
}
