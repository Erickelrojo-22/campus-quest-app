package com.example.gamequest.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gamequest.data.local.dao.MisionDao
import com.example.gamequest.data.local.dao.ProgresoMisionDao
import com.example.gamequest.data.local.dao.PuntoInteresDao
import com.example.gamequest.data.local.dao.UsuarioDao
import com.example.gamequest.data.local.entity.MisionEntity
import com.example.gamequest.data.local.entity.ProgresoMisionEntity
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import com.example.gamequest.data.local.entity.UsuarioEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UsuarioEntity::class,
        PuntoInteresEntity::class,
        MisionEntity::class,
        ProgresoMisionEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun puntoInteresDao(): PuntoInteresDao
    abstract fun misionDao(): MisionDao
    abstract fun progresoMisionDao(): ProgresoMisionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "campus_quest.db"
                ).addMigrations(MIGRATION_1_2).addCallback(object : Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        scope.launch {
                            INSTANCE?.let { poblarDatosIniciales(it) }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun poblarDatosIniciales(db: AppDatabase) {
            val puntoDao = db.puntoInteresDao()
            val misionDao = db.misionDao()
            SeedData.puntosConMisiones().forEach { semilla ->
                val puntoId = puntoDao.insertar(semilla.punto)
                misionDao.insertar(semilla.mision.copy(puntoInteresId = puntoId.toInt()))
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE mision ADD COLUMN activa INTEGER NOT NULL DEFAULT 1")
                db.execSQL(
                    """
                    DELETE FROM punto_interes
                    WHERE id NOT IN (
                        SELECT MIN(id)
                        FROM punto_interes
                        GROUP BY codigoQr
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_punto_interes_codigoQr ON punto_interes(codigoQr)")
                db.execSQL(
                    """
                    DELETE FROM progreso_mision
                    WHERE id NOT IN (
                        SELECT MIN(id)
                        FROM progreso_mision
                        GROUP BY usuarioId, misionId
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_progreso_mision_usuarioId_misionId " +
                        "ON progreso_mision(usuarioId, misionId)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_usuario_correoInstitucional ON usuario(correoInstitucional)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_usuario_nombres ON usuario(nombres)")
            }
        }
    }
}
