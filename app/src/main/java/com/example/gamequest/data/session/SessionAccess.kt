package com.example.gamequest.data.session

import com.example.gamequest.data.local.entity.UsuarioEntity

interface SessionAccess {
    suspend fun resolverIdentidad(): UsuarioEntity
    suspend fun sincronizarCatalogoYProgreso(localUserId: Int)
}
