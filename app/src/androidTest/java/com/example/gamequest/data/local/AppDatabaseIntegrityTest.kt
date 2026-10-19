package com.example.gamequest.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gamequest.data.local.entity.MisionEntity
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import com.example.gamequest.data.local.entity.ProgresoMisionEntity
import com.example.gamequest.data.local.entity.UsuarioEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseIntegrityTest {
    private lateinit var database: AppDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun duplicateQrCodeIsRejectedByDatabase() = runBlocking {
        database.puntoInteresDao().insertar(point(id = 1, qr = "QR-UNICO"))

        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                database.puntoInteresDao().insertar(point(id = 2, qr = "QR-UNICO"))
            }
        }
        assertEquals(1, database.puntoInteresDao().contar())
    }

    @Test
    fun sameMissionCannotBeCompletedTwiceBySameUser() = runBlocking {
        database.usuarioDao().insertar(user(id = 1))
        database.puntoInteresDao().insertar(point(id = 1, qr = "QR-MISION"))
        database.misionDao().insertar(mission(id = 1, pointId = 1))
        database.progresoMisionDao().insertar(progress(id = 1, userId = 1, missionId = 1))

        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                database.progresoMisionDao().insertar(
                    progress(id = 2, userId = 1, missionId = 1)
                )
            }
        }
        assertEquals(1, database.progresoMisionDao().contarCompletadas(1))
    }

    private fun point(id: Int, qr: String) = PuntoInteresEntity(
        id = id,
        nombre = "Punto $id",
        categoria = "Académico",
        descripcion = "Punto para prueba de integridad",
        horarioAtencion = "08:00-17:00",
        tramites = "Información",
        posX = 0.25f,
        posY = 0.50f,
        codigoQr = qr
    )

    private fun user(id: Int) = UsuarioEntity(
        id = id,
        nombres = "Estudiante $id",
        correoInstitucional = "estudiante$id@live.uleam.edu.ec",
        carrera = "Software",
        contrasenaHash = "hash"
    )

    private fun mission(id: Int, pointId: Int) = MisionEntity(
        id = id,
        titulo = "Misión $id",
        descripcionPista = "Pista de prueba",
        puntos = 40,
        tiempoEstimadoMin = 10,
        dificultad = "Media",
        puntoInteresId = pointId,
        insigniaNombre = "Integridad",
        insigniaEmoji = "🛡️"
    )

    private fun progress(id: Int, userId: Int, missionId: Int) = ProgresoMisionEntity(
        id = id,
        usuarioId = userId,
        misionId = missionId,
        fechaHora = 123456789L + id,
        puntosObtenidos = 40,
        codigoQrValidado = "QR-MISION"
    )
}
