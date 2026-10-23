package com.example.gamequest.data.remote

import com.example.gamequest.data.local.entity.*
import org.json.JSONObject

open class FakeCampusGateway(override val tokens: TokenStore = FakeTokenStore()) : CampusGateway {
    override var onUnauthorized: () -> Unit = {}
    var authentication: suspend (String, JSONObject) -> AuthPayload = { _, _ -> error("Unexpected authentication") }
    override suspend fun authenticate(path: String, body: JSONObject) = authentication(path, body)
    override suspend fun me(): RemoteUserDto = error("Unexpected me")
    override suspend fun logout(token: String) = Unit
    override suspend fun puntos(): List<PuntoInteresEntity> = emptyList()
    override suspend fun misiones(): List<MisionEntity> = emptyList()
    override suspend fun progreso(id: Int): List<RemoteProgressDto> = emptyList()
    override suspend fun completar(id: Int, mision: Int, codigo: String): RemoteProgressDto = error("Unexpected completion")
    override suspend fun ranking(): List<com.example.gamequest.data.model.RankingEntry> = emptyList()
}
