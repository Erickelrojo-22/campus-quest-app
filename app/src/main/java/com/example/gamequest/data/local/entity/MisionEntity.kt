package com.example.gamequest.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object Dificultad {
    const val BAJA = "Baja"
    const val MEDIA = "Media"
    const val ALTA = "Alta"
}

/**
 * Tabla maestra: retos asociados a un punto de interés.
 */
@Entity(
    tableName = "mision",
    foreignKeys = [
        ForeignKey(
            entity = PuntoInteresEntity::class,
            parentColumns = ["id"],
            childColumns = ["puntoInteresId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("puntoInteresId")]
)
data class MisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val descripcionPista: String,
    val puntos: Int,
    val tiempoEstimadoMin: Int,
    val dificultad: String,
    val puntoInteresId: Int,
    val insigniaNombre: String,
    val insigniaEmoji: String,
    val activa: Boolean = true
)
