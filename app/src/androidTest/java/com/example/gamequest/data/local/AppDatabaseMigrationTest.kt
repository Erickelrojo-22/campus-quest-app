package com.example.gamequest.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migration1To2PreservesUserMissionAndProgress() {
        helper.createDatabase(DATABASE_NAME, 1).apply {
            execSQL(
                """
                INSERT INTO usuario
                    (id, nombres, correoInstitucional, carrera, contrasenaHash,
                     puntajeAcumulado, nivel, rol)
                VALUES
                    (7, 'Estudiante legado', 'legado@live.uleam.edu.ec',
                     'Software', 'hash', 80, 2, 'ESTUDIANTE')
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO punto_interes
                    (id, nombre, categoria, descripcion, horarioAtencion,
                     tramites, posX, posY, codigoQr)
                VALUES
                    (8, 'Biblioteca', 'Académico', 'Punto legado', '08:00-17:00',
                     'Préstamos', 0.25, 0.50, 'QR-LEGADO')
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO mision
                    (id, titulo, descripcionPista, puntos, tiempoEstimadoMin,
                     dificultad, puntoInteresId, insigniaNombre, insigniaEmoji)
                VALUES
                    (9, 'Misión legada', 'Busca la biblioteca', 40, 10,
                     'Media', 8, 'Lector', '📚')
                """.trimIndent()
            )
            execSQL(
                """
                INSERT INTO progreso_mision
                    (id, usuarioId, misionId, fechaHora, puntosObtenidos,
                     codigoQrValidado, estado)
                VALUES
                    (10, 7, 9, 123456789, 40, 'QR-LEGADO', 'completada')
                """.trimIndent()
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(
            DATABASE_NAME,
            2,
            true,
            AppDatabase.MIGRATION_1_2
        )

        migrated.query(
            "SELECT nombres, puntajeAcumulado FROM usuario WHERE id = 7"
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals("Estudiante legado", cursor.getString(0))
            assertEquals(80, cursor.getInt(1))
        }
        migrated.query(
            "SELECT titulo, activa FROM mision WHERE id = 9"
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals("Misión legada", cursor.getString(0))
            assertEquals(1, cursor.getInt(1))
        }
        migrated.query(
            "SELECT usuarioId, misionId, puntosObtenidos FROM progreso_mision WHERE id = 10"
        ).use { cursor ->
            cursor.moveToFirst()
            assertEquals(7, cursor.getInt(0))
            assertEquals(9, cursor.getInt(1))
            assertEquals(40, cursor.getInt(2))
        }
        migrated.close()
    }

    private companion object {
        const val DATABASE_NAME = "migration-integrity-test"
    }
}
