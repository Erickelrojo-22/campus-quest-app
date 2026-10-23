package com.example.gamequest

import androidx.activity.ComponentActivity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import com.example.gamequest.data.session.*
import com.example.gamequest.ui.navigation.*
import com.example.gamequest.ui.theme.GamequestTheme
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*

class SessionNavigationTest {
    @get:Rule val compose=createAndroidComposeRule<ComponentActivity>()
    private lateinit var dependencies: FakeCampusQuestDependencies
    private lateinit var nav: NavHostController
    private val state=mutableStateOf<SessionState>(SessionState.Restoring)
    private var retries=0
    @Before fun setup() = runBlocking { dependencies=FakeCampusQuestDependencies(compose.activity); dependencies.seed() }
    @After fun cleanup() { dependencies.close() }
    private fun launch(initial: SessionState,restoration: StateRestorationTester?=null) {
        state.value=initial
        dependencies.preferences.value=dependencies.preferences.value.copy(usuarioActivoId=(initial as? SessionState.Authenticated)?.localUserId ?: -1)
        val content: @Composable () -> Unit = {
            val context=LocalContext.current
            val controller=if(restoration != null) rememberNavController() else remember {
                TestNavHostController(context).apply { navigatorProvider.addNavigator(ComposeNavigator()) }
            }
            nav=controller
            GamequestTheme {
                CampusQuestNavGraph(controller,state.value,dependencies,
                    onLogin={ state.value=SessionState.Authenticated(it,SessionSource.REMOTE,false) },
                    onLogout={ state.value=SessionState.SignedOut() },onRetryRestore={ retries++ })
            }
        }
        if(restoration != null) restoration.setContent(content) else compose.setContent(content)
        if (initial is SessionState.Authenticated) compose.mainClock.advanceTimeBy(1500)
    }
    private fun route(expected: String) { compose.waitUntil(10000) { nav.currentDestination?.route==expected }; compose.runOnIdle { assertEquals(expected,nav.currentDestination?.route) } }
    private fun auth(id: Int=8)=SessionState.Authenticated(id,SessionSource.REMOTE,false)
    @Test fun restoring_masDe1100ms_noAbreMainNiLogin() {
        launch(SessionState.Restoring); compose.mainClock.advanceTimeBy(2000); compose.waitForIdle()
        assertEquals(Routes.SPLASH,nav.currentDestination?.route)
    }
    @Test fun authenticated_abreMainUnaVez() {
        launch(auth()); route(Routes.MAIN)
        compose.runOnIdle { state.value=auth().copy(offline=true) }
        compose.waitForIdle(); assertNull(nav.previousBackStackEntry)
    }
    @Test fun signedOut_abreLogin() { launch(SessionState.SignedOut()); route(Routes.LOGIN) }
    @Test fun recoveryRequired_muestraReintento() {
        launch(SessionState.RecoveryRequired("No hay copia local"))
        compose.onNodeWithText("Reintentar").performClick(); compose.runOnIdle { assertEquals(1,retries) }
    }
    @Test fun errorSinCache_puedeVolverAlLogin() {
        launch(SessionState.RecoveryRequired("Sin cache"))
        compose.onNodeWithText("Volver al inicio de sesión").performClick(); route(Routes.LOGIN)
    }
    @Test fun http401EnMain_limpiaBackStackPrivado() {
        launch(auth()); route(Routes.MAIN)
        compose.runOnIdle { state.value=SessionState.SignedOut("expired") }; route(Routes.LOGIN)
        compose.runOnIdle { assertNull(nav.previousBackStackEntry) }
    }
    @Test fun http401EnDetalle_limpiaBackStackPrivado() {
        launch(auth()); route(Routes.MAIN)
        compose.runOnIdle { nav.navigate(Routes.missionDetail(1)) }; route(Routes.MISSION_DETAIL)
        compose.runOnIdle { state.value=SessionState.SignedOut("expired") }; route(Routes.LOGIN)
        compose.runOnIdle { assertNull(nav.previousBackStackEntry) }
    }
    @Test fun mismaCuenta_enDetalle_noVuelveAMain() {
        launch(auth()); route(Routes.MAIN)
        compose.runOnIdle { nav.navigate(Routes.missionDetail(1)) }; route(Routes.MISSION_DETAIL)
        compose.runOnIdle { state.value=auth().copy(offline=true) }; compose.waitForIdle()
        assertEquals(Routes.MISSION_DETAIL,nav.currentDestination?.route)
    }
    @Test fun loginNuevo_noRecuperaPantallasDeLaCuentaAnterior() {
        launch(auth()); route(Routes.MAIN)
        compose.runOnIdle { nav.navigate(Routes.missionDetail(1)) }; route(Routes.MISSION_DETAIL)
        compose.runOnIdle { state.value=auth(9) }; route(Routes.MAIN)
        compose.runOnIdle { assertNull(nav.previousBackStackEntry) }
    }
    @Test fun rotacion_noDuplicaNavegacion() {
        val restoration=StateRestorationTester(compose)
        launch(auth(),restoration); route(Routes.MAIN)
        compose.runOnIdle { nav.navigate(Routes.missionDetail(1)) }; route(Routes.MISSION_DETAIL)
        restoration.emulateSavedInstanceStateRestore(); route(Routes.MISSION_DETAIL)
        compose.runOnIdle { assertEquals(Routes.MAIN,nav.previousBackStackEntry?.destination?.route) }
    }
}
