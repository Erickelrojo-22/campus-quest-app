package com.example.gamequest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla maestra: lugares del campus.
 * `posX`/`posY` ubican el punto dentro del mapa estilizado del campus (0f..1f),
 * equivalente conceptual a latitud/longitud dentro de un plano cerrado sin GPS real.
 */
@Entity(tableName = "punto_interes")
data class PuntoInteresEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val horarioAtencion: String,
    val tramites: String,
    val posX: Float,
    val posY: Float,
    val codigoQr: String
)
