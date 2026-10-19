package com.example.gamequest.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gamequest.data.local.entity.PuntoInteresEntity
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
}
