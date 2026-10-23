package com.example.gamequest.data.remote

import com.example.gamequest.data.local.entity.UsuarioEntity

data class RemoteUserDto(val remoteId: Int, val nombres: String, val correoInstitucional: String,
    val carrera: String, val puntajeAcumulado: Int, val nivel: Int, val rol: String) {
    fun toLocal() = UsuarioEntity(nombres = nombres, correoInstitucional = correoInstitucional,
        carrera = carrera, contrasenaHash = "", puntajeAcumulado = puntajeAcumulado,
        nivel = nivel, rol = rol, remoteId = remoteId)
}
data class RemoteProgressDto(val remoteId: Int, val remoteUserId: Int, val misionId: Int,
    val fechaHora: Long, val puntosObtenidos: Int, val codigoQrValidado: String, val estado: String)
