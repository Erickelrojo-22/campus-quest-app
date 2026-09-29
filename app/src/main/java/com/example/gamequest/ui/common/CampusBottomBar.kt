package com.example.gamequest.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.gamequest.ui.navigation.Routes
import androidx.compose.ui.unit.dp
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.ContainerDark
import com.example.gamequest.util.LocalSoundManager
import com.example.gamequest.util.SoundEffect

data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

val bottomTabs = listOf(
    BottomTab(Routes.HOME, "Inicio", Icons.Filled.Home),
    BottomTab(Routes.MISSIONS, "Misiones", Icons.Filled.Checklist),
    BottomTab(Routes.SCANNER, "QR", Icons.Filled.QrCodeScanner),
    BottomTab(Routes.BADGES, "Insignias", Icons.Filled.EmojiEvents),
    BottomTab(Routes.PROFILE, "Perfil", Icons.Filled.Person)
)

/**
 * Barra inferior de navegación con la estética retro/pixel de Campus
 * Quest: fondo verde azulado oscuro, pestaña activa resaltada en ámbar,
 * como en el HUD inferior de los mockups de pixel art.
 */
@Composable
fun CampusBottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    val soundManager = LocalSoundManager.current
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = AmberAccent,
        tonalElevation = 0.dp
    ) {
        bottomTabs.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = {
                    if (currentRoute != tab.route) {
                        soundManager?.play(SoundEffect.CLICK)
                    }
                    onNavigate(tab.route)
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ContainerDark,
                    selectedTextColor = AmberAccent,
                    indicatorColor = AmberAccent,
                    unselectedIconColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.75f),
                    unselectedTextColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.75f)
                )
            )
        }
    }
}
