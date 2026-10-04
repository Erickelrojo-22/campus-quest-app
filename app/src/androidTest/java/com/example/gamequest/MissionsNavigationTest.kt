package com.example.gamequest

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gamequest.data.local.SeedData
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.repository.CampusRepository
import com.example.gamequest.data.repository.MisionConEstado
import com.example.gamequest.ui.badges.BadgeEarnedScreen
import com.example.gamequest.ui.badges.BadgeEarnedViewModel
import com.example.gamequest.ui.common.CampusBottomBar
import com.example.gamequest.ui.missions.MissionsScreen
import com.example.gamequest.ui.missions.MissionsViewModel
import com.example.gamequest.ui.navigation.Routes
import com.example.gamequest.ui.theme.GamequestTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

/** Exercises rendered screens and navigation with fixtures; never writes to the live API. */
class MissionsNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    private val seed = SeedData.puntosConMisiones().first()
    private val mission = seed.mision.copy(id = 1, puntoInteresId = 1)
    private val point = seed.punto.copy(id = 1)

    private fun launch(items: List<MisionConEstado>, fromBadge: Boolean = false) {
        val repository = object : CampusRepository() {
            override fun observarMisionesConEstado(usuarioId: Int) = flowOf(items)
            override suspend fun buscarMisionPorId(id: Int) = mission
            override fun observarTotalInsignias(usuarioId: Int) = flowOf(1)
            override fun observarUsuario(id: Int) = flowOf(
                UsuarioEntity(id = 1, nombres = "Prueba de navegación", correoInstitucional = "",
                    carrera = "", contrasenaHash = "", puntajeAcumulado = 50)
            )
        }
        lateinit var missionsViewModel: MissionsViewModel
        lateinit var badgeViewModel: BadgeEarnedViewModel
        compose.runOnUiThread {
            missionsViewModel = MissionsViewModel(repository, 1)
            badgeViewModel = BadgeEarnedViewModel(repository, 1, 1)
        }
        compose.setContent {
            GamequestTheme {
                val nav = rememberNavController()
                NavHost(nav, startDestination = if (fromBadge) "earned" else Routes.HOME) {
                    composable(Routes.HOME) {
                        Scaffold(bottomBar = {
                            CampusBottomBar(currentRoute = Routes.HOME, onNavigate = {
                                nav.navigate(it) {
                                    popUpTo(Routes.HOME) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            })
                        }) { padding -> Text("Inicio de prueba", Modifier.padding(padding)) }
                    }
                    composable("earned") {
                        BadgeEarnedScreen(badgeViewModel, onSiguienteMision = {
                            nav.navigate(Routes.MISSIONS) { popUpTo(Routes.HOME) }
                        })
                    }
                    composable(Routes.MISSIONS) {
                        MissionsScreen(missionsViewModel, onMisionClick = {}, onNavigateTab = {
                            nav.navigate(it) { launchSingleTop = true }
                        })
                    }
                }
            }
        }
        if (fromBadge) {
            compose.onNodeWithText("SIGUIENTE MISIÓN").performClick()
        } else {
            compose.onNodeWithText("Misiones").performClick()
        }
        compose.onNodeWithText("Mis misiones").assertIsDisplayed()
    }

    private fun waitForText(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText(text).assertIsDisplayed()
    }

    @Test fun emptyCatalogOpensAndShowsEmptyState() {
        launch(emptyList())
        compose.onNodeWithText("Todas").performClick()
        waitForText("Todavía no hay misiones cargadas.")
    }

    @Test fun activeAndCompletedMissionsRenderAndFilterAfterNavigation() {
        launch(listOf(MisionConEstado(mission, point, false),
            MisionConEstado(mission.copy(id = 2, titulo = "Misión ya completada"), point, true)))
        waitForText(mission.titulo)
        compose.onNodeWithText("Completadas · 1").performClick()
        waitForText("Misión ya completada")
        compose.onNodeWithText("Activas · 1").performClick()
        waitForText(mission.titulo)
    }

    @Test fun nextMissionFromEarnedBadgeOpensCompletedCatalog() {
        launch(listOf(MisionConEstado(mission, point, true)), fromBadge = true)
        compose.onNodeWithText("Completadas · 1").performClick()
        waitForText(mission.titulo)
    }
}
