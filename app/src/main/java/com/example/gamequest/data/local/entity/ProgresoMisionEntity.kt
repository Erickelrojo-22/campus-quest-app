package com.example.gamequest.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object EstadoMision {
    const val COMPLETADA = "completada"
}

/**
 * Tabla transaccional: registro de cada misión completada por un usuario.
 */
@Entity(
    tableName = "progreso_mision",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MisionEntity::class,
            parentColumns = ["id"],
            childColumns = ["misionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("usuarioId"),
        Index("misionId"),
        Index(value = ["usuarioId", "misionId"], unique = true)
    ]
)
data class ProgresoMisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val misionId: Int,
    val fechaHora: Long,
    val puntosObtenidos: Int,
    val codigoQrValidado: String,
    val estado: String = EstadoMision.COMPLETADA
)
