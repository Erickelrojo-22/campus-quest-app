package com.example.gamequest.data.model

import com.example.gamequest.data.local.entity.UsuarioEntity

data class RankingEntry(val remoteUserId: Int?, val localUserId: Int?, val nombres: String,
    val puntajeAcumulado: Int, val nivel: Int, val carrera: String = "") {
    fun isCurrent(localId: Int, remoteId: Int?) = if (remoteUserId != null)
        remoteId != null && remoteUserId == remoteId else localUserId == localId
    val stableKey: String get() = remoteUserId?.let { "remote-$it" } ?: "local-$localUserId"
    companion object {
        fun local(user: UsuarioEntity) = RankingEntry(user.remoteId, user.id, user.nombres, user.puntajeAcumulado, user.nivel, user.carrera)
    }
}
