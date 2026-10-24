package com.example.gamequest.data.remote

import com.example.gamequest.data.local.entity.*
import org.json.JSONObject

data class AuthPayload(val accessToken: String, val user: RemoteUserDto)

interface CampusGateway {
    val tokens: TokenStore
    var onUnauthorized: (String) -> Unit
    suspend fun authenticate(path: String, body: JSONObject): AuthPayload
    suspend fun me(): RemoteUserDto
    suspend fun logout(token: String)
    suspend fun puntos(): List<PuntoInteresEntity>
    suspend fun misiones(): List<MisionEntity>
    suspend fun progreso(id: Int): List<RemoteProgressDto>
    suspend fun completar(id: Int, mision: Int, codigo: String): RemoteProgressDto
    suspend fun ranking(): List<com.example.gamequest.data.model.RankingEntry>
}
