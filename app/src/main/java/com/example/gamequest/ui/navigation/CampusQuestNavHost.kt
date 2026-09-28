package com.example.gamequest.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gamequest.AppContainer
import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.ui.auth.AuthViewModel
import com.example.gamequest.ui.auth.LoginScreen
import com.example.gamequest.ui.auth.RegisterScreen
import com.example.gamequest.ui.auth.RegisterViewModel

import com.example.gamequest.ui.badges.BadgeEarnedViewModel
import com.example.gamequest.ui.badges.BadgeEarnedScreen
import com.example.gamequest.ui.badges.BadgesScreen
import com.example.gamequest.ui.badges.BadgesViewModel
import com.example.gamequest.ui.common.GenericViewModelFactory
import com.example.gamequest.ui.common.SessionViewModel
import com.example.gamequest.ui.crud.MissionFormScreen
import com.example.gamequest.ui.crud.MissionFormViewModel
import com.example.gamequest.ui.crud.MissionManagementScreen
import com.example.gamequest.ui.crud.MissionManagementViewModel
import com.example.gamequest.ui.home.HomeScreen
import com.example.gamequest.ui.home.HomeViewModel
import com.example.gamequest.ui.missions.MissionDetailScreen
import com.example.gamequest.ui.missions.MissionDetailViewModel
import com.example.gamequest.ui.missions.MissionsScreen
import com.example.gamequest.ui.missions.MissionsViewModel
import com.example.gamequest.ui.profile.ProfileScreen
import com.example.gamequest.ui.scanner.ScannerScreen
import com.example.gamequest.ui.scanner.ScannerViewModel
import com.example.gamequest.ui.settings.SettingsScreen
import com.example.gamequest.ui.settings.SettingsViewModel
import com.example.gamequest.ui.splash.SplashScreen

private fun NavHostController.navegarATab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun CampusQuestNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val sessionViewModel: SessionViewModel = viewModel(
        factory = GenericViewModelFactory { SessionViewModel(container) }
    )
    val usuario by sessionViewModel.usuarioActual.collectAsState()

    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(onTimeout = {
                val destino = if (sessionViewModel.preferencias.value.usuarioActivoId > 0) Routes.HOME else Routes.LOGIN
                navController.navigate(destino) { popUpTo(Routes.SPLASH) { inclusive = true } }
            })
        }

        composable(Routes.LOGIN) {
            val authViewModel: AuthViewModel = viewModel(factory = GenericViewModelFactory { AuthViewModel(container.authRepository) })
            LoginScreen(
                viewModel = authViewModel,
                onLoginExitoso = { usuarioLogueado ->
                    sessionViewModel.iniciarSesion(usuarioLogueado.id)
                    navController.navigate(Routes.HOME) { popUpTo(0) }
                },
                onIrARegistro = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            val registerViewModel: RegisterViewModel = viewModel(factory = GenericViewModelFactory { RegisterViewModel(container.authRepository) })
            RegisterScreen(
                viewModel = registerViewModel,
                onRegistroExitoso = { usuarioRegistrado ->
                    sessionViewModel.iniciarSesion(usuarioRegistrado.id)
                    navController.navigate(Routes.HOME) { popUpTo(0) }
                },
                onVolverALogin = {
                    navController.popBackStack()
                }
            )
        }


        composable(Routes.HOME) {
            val usuarioId = usuario?.id
            if (usuarioId == null) {
                CargandoPantallaCompleta()
            } else {
                val homeViewModel: HomeViewModel = viewModel(
                    key = "home-$usuarioId",
                    factory = GenericViewModelFactory { HomeViewModel(container.campusRepository, usuarioId) }
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onMisionClick = { misionId -> navController.navigate(Routes.missionDetail(misionId)) },
                    onNavigateTab = { navController.navegarATab(it) }
                )
            }
        }

        composable(Routes.MISSIONS) {
            val usuarioId = usuario?.id
            if (usuarioId == null) {
                CargandoPantallaCompleta()
            } else {
                val missionsViewModel: MissionsViewModel = viewModel(
                    key = "missions-$usuarioId",
                    factory = GenericViewModelFactory { MissionsViewModel(container.campusRepository, usuarioId) }
                )
                MissionsScreen(
                    viewModel = missionsViewModel,
                    onMisionClick = { misionId -> navController.navigate(Routes.missionDetail(misionId)) },
                    onNavigateTab = { navController.navegarATab(it) }
                )
            }
        }

        composable(
            Routes.MISSION_DETAIL,
            arguments = listOf(navArgument("misionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val usuarioId = usuario?.id
            val misionId = backStackEntry.arguments?.getInt("misionId") ?: -1
            if (usuarioId == null) {
                CargandoPantallaCompleta()
            } else {
                val detailViewModel: MissionDetailViewModel = viewModel(
                    key = "detail-$misionId-$usuarioId",
                    factory = GenericViewModelFactory { MissionDetailViewModel(container.campusRepository, misionId, usuarioId) }
                )
                MissionDetailScreen(
                    viewModel = detailViewModel,
                    onBack = { navController.popBackStack() },
                    onEscanear = { navController.navigate(Routes.SCANNER) },
                    onVerRuta = { navController.navigate(Routes.HOME) { popUpTo(Routes.HOME) } }
                )
            }
        }

        composable(Routes.SCANNER) {
            val usuarioId = usuario?.id
            if (usuarioId == null) {
                CargandoPantallaCompleta()
            } else {
                val scannerViewModel: ScannerViewModel = viewModel(
                    key = "scanner-$usuarioId",
                    factory = GenericViewModelFactory { ScannerViewModel(container.campusRepository, usuarioId) }
                )
                ScannerScreen(
                    viewModel = scannerViewModel,
                    onNavigateTab = { navController.navegarATab(it) },
                    onBack = {
                        if (!navController.popBackStack()) navController.navegarATab(Routes.HOME)
                    },
                    onMisionCompletada = { misionId ->
                        navController.navigate(Routes.badgeEarned(misionId)) {
                            popUpTo(Routes.SCANNER) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable(
            Routes.BADGE_EARNED,
            arguments = listOf(navArgument("misionId") { type = NavType.IntType })
        ) { backStackEntry ->
            val usuarioId = usuario?.id
            val misionId = backStackEntry.arguments?.getInt("misionId") ?: -1
            if (usuarioId == null) {
                CargandoPantallaCompleta()
            } else {
                val badgeViewModel: BadgeEarnedViewModel = viewModel(
                    key = "badge-$misionId-$usuarioId",
                    factory = GenericViewModelFactory { BadgeEarnedViewModel(container.campusRepository, misionId, usuarioId) }
                )
                BadgeEarnedScreen(
                    viewModel = badgeViewModel,
                    onSiguienteMision = {
                        navController.navigate(Routes.MISSIONS) { popUpTo(Routes.HOME) }
                    }
                )
            }
        }

        composable(Routes.BADGES) {
            val usuarioId = usuario?.id
            if (usuarioId == null) {
                CargandoPantallaCompleta()
            } else {
                val badgesViewModel: BadgesViewModel = viewModel(
                    key = "badges-$usuarioId",
                    factory = GenericViewModelFactory { BadgesViewModel(container.campusRepository, usuarioId) }
                )
                BadgesScreen(
                    viewModel = badgesViewModel,
                    usuarioActualId = usuarioId,
                    onNavigateTab = { navController.navegarATab(it) }
                )
            }
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                sessionViewModel = sessionViewModel,
                onNavigateTab = { navController.navegarATab(it) },
                onSettings = { navController.navigate(Routes.SETTINGS) },
                onMissionManagement = { navController.navigate(Routes.MISSION_MANAGEMENT) },
                onCerrarSesion = {
                    sessionViewModel.cerrarSesion()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                }
            )
        }

        composable(Routes.SETTINGS) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = GenericViewModelFactory { SettingsViewModel(container.preferencesRepository) }
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
                onCerrarSesion = {
                    sessionViewModel.cerrarSesion()
                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                }
            )
        }

        composable(Routes.MISSION_MANAGEMENT) {
            if (usuario?.rol == Rol.TUTOR) {
                val managementViewModel: MissionManagementViewModel = viewModel(
                    factory = GenericViewModelFactory { MissionManagementViewModel(container.campusRepository) }
                )
                MissionManagementScreen(
                    viewModel = managementViewModel,
                    onBack = { navController.popBackStack() },
                    onNuevaMision = { navController.navigate(Routes.missionFormNuevo()) },
                    onEditarMision = { puntoId -> navController.navigate(Routes.missionFormEditar(puntoId)) }
                )
            } else {
                AccesoNoAutorizado { navController.popBackStack() }
            }
        }

        composable(
            Routes.MISSION_FORM,
            arguments = listOf(navArgument("puntoId") { type = NavType.IntType; defaultValue = -1 })
        ) { backStackEntry ->
            if (usuario?.rol == Rol.TUTOR) {
                val puntoId = backStackEntry.arguments?.getInt("puntoId")?.takeIf { it > 0 }
                val formViewModel: MissionFormViewModel = viewModel(
                    key = "form-$puntoId",
                    factory = GenericViewModelFactory { MissionFormViewModel(container.campusRepository, puntoId) }
                )
                MissionFormScreen(
                    viewModel = formViewModel,
                    onGuardado = { navController.popBackStack() },
                    onCancelar = { navController.popBackStack() }
                )
            } else {
                AccesoNoAutorizado { navController.popBackStack() }
            }
        }
    }
}

@Composable
private fun CargandoPantallaCompleta() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun AccesoNoAutorizado(onBack: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No tienes permisos para acceder a esta sección.")
            TextButton(onClick = onBack) { Text("Volver") }
        }
    }
}
