package com.example.gamequest

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import com.example.gamequest.data.local.AppDatabase
import com.example.gamequest.data.remote.*
import com.example.gamequest.data.repository.AuthResult
import com.example.gamequest.data.session.*
import com.example.gamequest.ui.navigation.*
import com.example.gamequest.ui.theme.GamequestTheme
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import java.io.File

class IntegratedSessionTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    @get:Rule val migration = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),AppDatabase::class.java)
    private lateinit var deps: FakeCampusQuestDependencies
    private lateinit var nav: NavHostController
    @After fun cleanup() { if(::deps.isInitialized) deps.close() }
    private fun route(expected: String) = compose.waitUntil(15000) { nav.currentDestination?.route == expected }

    @Test fun migrar_restaurar_cambiarCuenta_rechazar403_expirar401() = runBlocking {
        val name="integrated-session"
        migration.createDatabase(name,2).apply {
            execSQL("INSERT INTO usuario (id,nombres,correoInstitucional,carrera,contrasenaHash,puntajeAcumulado,nivel,rol) VALUES (51,'Visitor','','','',20,1,'estudiante')")
            execSQL("INSERT INTO punto_interes (id,nombre,categoria,descripcion,horarioAtencion,tramites,posX,posY,codigoQr) VALUES (1,'Point','test','','','',0,0,'QR')")
            execSQL("INSERT INTO mision (id,titulo,descripcionPista,puntos,tiempoEstimadoMin,dificultad,puntoInteresId,insigniaNombre,insigniaEmoji,activa) VALUES (1,'Mission','',20,1,'test',1,'','',1)")
            execSQL("INSERT INTO progreso_mision (id,usuarioId,misionId,fechaHora,puntosObtenidos,codigoQrValidado,estado) VALUES (1,51,1,10,20,'LOCAL','completada')")
            close()
        }
        val db=Room.databaseBuilder(compose.activity.applicationContext,AppDatabase::class.java,name)
            .addMigrations(AppDatabase.MIGRATION_1_2,AppDatabase.MIGRATION_2_3).build()
        deps=FakeCampusQuestDependencies(compose.activity,db)
        deps.tokens.save("offline-visitor-51")
        deps.sessionCoordinator.restore()
        assertEquals(SessionState.Authenticated(51,SessionSource.LOCAL_VISITOR,true),deps.sessionCoordinator.state.value)
        assertNull(db.usuarioDao().buscarPorId(51)?.remoteId)
        compose.setContent {
            val state by deps.sessionCoordinator.state.collectAsState()
            nav=rememberNavController()
            GamequestTheme { CampusQuestNavGraph(nav,state,deps,{},{},{}) }
        }
        route(Routes.MAIN)
        deps.remoteProgress=listOf(RemoteProgressDto(1,51,1,20,30,"REMOTE","completada"))
        val login=deps.authRepository.entrarConNombre("Remote",false) as AuthResult.Exito
        val localId=login.usuario.id
        assertNotEquals(51,localId)
        deps.campusRepository.syncCatalogAndProgress(localId)
        assertEquals("LOCAL",db.progresoMisionDao().buscar(51,1)?.codigoQrValidado)
        assertEquals("REMOTE",db.progresoMisionDao().buscar(localId,1)?.codigoQrValidado)
        assertNotEquals(1,db.progresoMisionDao().buscar(localId,1)?.id)
        deps.sessionCoordinator.restore()
        assertEquals(localId,(deps.sessionCoordinator.state.value as SessionState.Authenticated).localUserId)
        assertEquals(2,db.usuarioDao().observarRanking().first().size)
        route(Routes.MAIN)
        deps.sessionCoordinator.logout(); route(Routes.LOGIN)
        deps.authFailure=403
        assertTrue(deps.authRepository.entrarConNombre("Rejected",false) is AuthResult.Error)
        assertNull(deps.tokens.token)
        assertEquals(2,db.usuarioDao().observarRanking().first().size)
        deps.authFailure=null
        assertTrue(deps.authRepository.entrarConNombre("Remote",false) is AuthResult.Exito)
        route(Routes.MAIN)
        deps.meFailure=401
        try { deps.gateway.me(); fail("Expected HTTP401") } catch(e: ApiException) { assertEquals(401,e.status) }
        route(Routes.LOGIN)
        compose.runOnIdle { assertNull(nav.previousBackStackEntry) }
        assertNull(deps.tokens.token)
        assertEquals("LOCAL",db.progresoMisionDao().buscar(51,1)?.codigoQrValidado)
        val screenshot=compose.onRoot().captureToImage().asAndroidBitmap()
        File(compose.activity.filesDir,"session-expired.png").outputStream().use { screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) }
        Unit
    }
}
