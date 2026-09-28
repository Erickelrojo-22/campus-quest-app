package com.example.gamequest.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/** Rol del usuario dentro de la aplicación. */
object Rol {
    const val ESTUDIANTE = "estudiante"
    const val TUTOR = "tutor"
}

/**
 * Tabla maestra (de apoyo): cuentas de la aplicación.
 * Ver apartado 6 del documento — Modelo de datos previsto (Room).
 */
@Entity(
    tableName = "usuario",
    indices = [Index("correoInstitucional"), Index("nombres")]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombres: String,
    val correoInstitucional: String,
    val carrera: String,
    val contrasenaHash: String,
    val puntajeAcumulado: Int = 0,
    val nivel: Int = 1,
    val rol: String = Rol.ESTUDIANTE
)
