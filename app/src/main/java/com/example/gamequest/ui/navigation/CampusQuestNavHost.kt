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
import com.example.gamequest.ui.common.bottomTabs
import com.example.gamequest.ui.common.indiceDeTab
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
import com.example.gamequest.ui.profile.ProfileViewModel
import com.example.gamequest.ui.scanner.ScannerScreen
import com.example.gamequest.ui.scanner.ScannerViewModel
import com.example.gamequest.ui.settings.SettingsScreen
import com.example.gamequest.ui.settings.SettingsViewModel
import com.example.gamequest.ui.splash.SplashScreen

/**
 * Vuelve al contenedor de pestañas ([Routes.MAIN]) mostrando la pestaña [route]
 * (una de [Routes.HOME], [Routes.MISSIONS], [Routes.SCANNER], ...). La petición viaja
 * en el `savedStateHandle` de la entrada MAIN y [MainTabsScreen] la atiende.
 */
private fun NavHostController.mostrarTab(route: String) {
    val indice = indiceDeTab(route)
    if (indice >= 0) {
        runCatching {
            getBackStackEntry(Routes.MAIN).savedStateHandle[Routes.KEY_TAB_SOLICITADA] = indice
        }
    }
    popBackStack(Routes.MAIN, inclusive = false)
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
                val destino = if (sessionViewModel.preferencias.value.usuarioActivoId > 0) Routes.MAIN else Routes.LOGIN
                navController.navigate(destino) { popUpTo(Routes.SPLASH) { inclusive = true } }
            })
        }

        composable(Routes.LOGIN) {
            val authViewModel: AuthViewModel = viewModel(factory = GenericViewModelFactory { AuthViewModel(container.authRepository) })
            LoginScreen(
                viewModel = authViewModel,
                onLoginExitoso = { usuarioLogueado ->
                    sessionViewModel.iniciarSesion(usuarioLogueado.id)
                    navController.navigate(Routes.MAIN) { popUpTo(0) }
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
                    navController.navigate(Routes.MAIN) { popUpTo(0) }
                },
                onVolverALogin = {
                    navController.popBackStack()
                }
            )
        }

        // Las 5 pestañas viven en un carrusel (deslizable) dentro de una sola ruta.
        // Cada ViewModel se crea al mostrar su página por primera vez (carga perezosa)
        // y queda asociado a esta entrada, así que sobrevive al cambiar de pestaña.
        composable(Routes.MAIN) { entrada ->
            val usuarioId = usuario?.id
            if (usuarioId == null) {
                CargandoPantallaCompleta()
            } else {
                val tabSolicitada by entrada.savedStateHandle
                    .getStateFlow(Routes.KEY_TAB_SOLICITADA, -1)
                    .collectAsState()

                MainTabsScreen(
                    tabSolicitada = tabSolicitada,
                    onTabSolicitadaAtendida = {
                        entrada.savedStateHandle[Routes.KEY_TAB_SOLICITADA] = -1
                    }
                ) { tab, activa ->
                    when (bottomTabs[tab].route) {
                        Routes.HOME -> {
                            val homeViewModel: HomeViewModel = viewModel(
                                key = "home-$usuarioId",
                                factory = GenericViewModelFactory { HomeViewModel(container.campusRepository, usuarioId) }
                            )
                            HomeScreen(
                                viewModel = homeViewModel,
                                onMisionClick = { misionId ->
                                    container.soundEffectManager.play(com.example.gamequest.util.SoundEffect.CLICK)
                                    navController.navigate(Routes.missionDetail(misionId))
                                }
                            )
                        }

                        Routes.MISSIONS -> {
                            val missionsViewModel: MissionsViewModel = viewModel(
                                key = "missions-$usuarioId",
                                factory = GenericViewModelFactory { MissionsViewModel(container.campusRepository, usuarioId) }
                            )
                            MissionsScreen(
                                viewModel = missionsViewModel,
                                onMisionClick = { misionId ->
                                    container.soundEffectManager.play(com.example.gamequest.util.SoundEffect.CLICK)
                                    navController.navigate(Routes.missionDetail(misionId))
                                }
                            )
                        }

                        Routes.SCANNER -> {
                            val scannerViewModel: ScannerViewModel = viewModel(
                                key = "scanner-$usuarioId",
                                factory = GenericViewModelFactory { ScannerViewModel(container.campusRepository, usuarioId) }
                            )
                            ScannerScreen(
                                viewModel = scannerViewModel,
                                activa = activa,
                                onBack = { navController.mostrarTab(Routes.HOME) },
                                onMisionCompletada = { misionId ->
                                    // Deja el escáner listo: al volver atrás no debe mostrar el resultado anterior.
                                    scannerViewModel.reiniciar()
                                    navController.navigate(Routes.badgeEarned(misionId))
                                }
                            )
                        }

                        Routes.BADGES -> {
                            val badgesViewModel: BadgesViewModel = viewModel(
                                key = "badges-$usuarioId",
                                factory = GenericViewModelFactory { BadgesViewModel(container.campusRepository, usuarioId) }
                            )
                            BadgesScreen(
                                viewModel = badgesViewModel,
                                usuarioActualId = usuarioId
                            )
                        }

                        else -> {
                            val profileViewModel: ProfileViewModel = viewModel(
                                key = "profile-$usuarioId",
                                factory = GenericViewModelFactory {
                                    ProfileViewModel(
                                        campusRepository = container.campusRepository,
                                        preferencesRepository = container.preferencesRepository,
                                        usuarioId = usuarioId
                                    )
                                }
                            )
                            ProfileScreen(
                                viewModel = profileViewModel,
                                onSettings = { navController.navigate(Routes.SETTINGS) },
                                onMissionManagement = { navController.navigate(Routes.MISSION_MANAGEMENT) },
                                onCerrarSesion = {
                                    sessionViewModel.cerrarSesion()
                                    navController.navigate(Routes.LOGIN) { popUpTo(0) }
                                }
                            )
                        }
                    }
                }
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
                    onBack = {
                        container.soundEffectManager.play(com.example.gamequest.util.SoundEffect.CLICK)
                        navController.popBackStack()
                    },
                    onEscanear = {
                        container.soundEffectManager.play(com.example.gamequest.util.SoundEffect.CLICK)
                        navController.mostrarTab(Routes.SCANNER)
                    },
                    onVerRuta = {
                        container.soundEffectManager.play(com.example.gamequest.util.SoundEffect.CLICK)
                        navController.mostrarTab(Routes.HOME)
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
                        navController.mostrarTab(Routes.MISSIONS)
                    }
                )
            }
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
