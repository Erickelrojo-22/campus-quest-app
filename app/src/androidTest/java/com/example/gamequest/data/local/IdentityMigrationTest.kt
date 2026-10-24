package com.example.gamequest.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import androidx.room.Room

class IdentityMigrationTest {
    @get:Rule val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), AppDatabase::class.java)
    @Test fun version2_conservaIdsYRelaciones() { migrate(2) }
    @Test fun version1_pasaPorVersion2() { migrate(1) }
    private fun migrate(version: Int) {
        val name = "identity-migration-$version"
        helper.createDatabase(name, version).apply {
            execSQL("INSERT INTO usuario (id,nombres,correoInstitucional,carrera,contrasenaHash,puntajeAcumulado,nivel,rol) VALUES (51,'Visitor','','','',20,1,'estudiante')")
            execSQL("INSERT INTO punto_interes (id,nombre,categoria,descripcion,horarioAtencion,tramites,posX,posY,codigoQr) VALUES (1,'Point','test','','','',0,0,'QR')")
            val active = if (version == 2) ",activa" else ""
            val activeValue = if (version == 2) ",1" else ""
            execSQL("INSERT INTO mision (id,titulo,descripcionPista,puntos,tiempoEstimadoMin,dificultad,puntoInteresId,insigniaNombre,insigniaEmoji$active) VALUES (1,'Mission','',20,1,'test',1,'',''$activeValue)")
            execSQL("INSERT INTO progreso_mision (id,usuarioId,misionId,fechaHora,puntosObtenidos,codigoQrValidado,estado) VALUES (1,51,1,10,20,'QR','completada')")
            close()
        }
        helper.runMigrationsAndValidate(name, 3, true, AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3).apply {
            query("SELECT id,remoteId FROM usuario").use { assertTrue(it.moveToFirst()); assertEquals(51,it.getInt(0)); assertTrue(it.isNull(1)) }
            query("SELECT id,usuarioId,remoteId FROM progreso_mision").use { assertTrue(it.moveToFirst()); assertEquals(1,it.getInt(0)); assertEquals(51,it.getInt(1)); assertTrue(it.isNull(2)) }
            query("PRAGMA foreign_key_check").use { assertFalse(it.moveToFirst()) }
            close()
        }
    }
    @Test fun version3_creacionLimpia() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, AppDatabase::class.java).build()
        try { assertNull(db.usuarioDao().buscarPorRemoteId(51)) } finally { db.close() }
    }
}
