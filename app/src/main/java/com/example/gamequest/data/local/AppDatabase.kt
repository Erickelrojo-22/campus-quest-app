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

@Database(
    entities = [
        UsuarioEntity::class,
        PuntoInteresEntity::class,
        MisionEntity::class,
        ProgresoMisionEntity::class
    ],
    version = 1,
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
                ).addCallback(object : Callback() {
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
    }
}
