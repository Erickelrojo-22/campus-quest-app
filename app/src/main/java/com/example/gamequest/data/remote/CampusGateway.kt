package com.example.gamequest.data.remote

import com.example.gamequest.data.local.entity.*
import org.json.JSONObject

data class AuthPayload(val accessToken: String, val user: UsuarioEntity)

interface CampusGateway {
    val tokens: TokenStore
    var onUnauthorized: () -> Unit
    suspend fun authenticate(path: String, body: JSONObject): AuthPayload
    suspend fun me(): UsuarioEntity
    suspend fun logout(token: String)
    suspend fun puntos(): List<PuntoInteresEntity>
    suspend fun misiones(): List<MisionEntity>
    suspend fun progreso(id: Int): List<ProgresoMisionEntity>
    suspend fun completar(id: Int, mision: Int, codigo: String): ProgresoMisionEntity
    suspend fun ranking(): List<UsuarioEntity>
}
