package com.example.gamequest.ui.common

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gamequest.ui.navigation.Routes
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.ContainerDark

data class BottomTab(val route: String, val label: String, val icon: ImageVector)

/** Orden de las pestañas = orden de las páginas del carrusel (deslizar izquierda/derecha). */
val bottomTabs = listOf(
    BottomTab(Routes.HOME, "Inicio", Icons.Filled.Home),
    BottomTab(Routes.MISSIONS, "Misiones", Icons.Filled.Checklist),
    BottomTab(Routes.SCANNER, "QR", Icons.Filled.QrCodeScanner),
    BottomTab(Routes.BADGES, "Insignias", Icons.Filled.EmojiEvents),
    BottomTab(Routes.PROFILE, "Perfil", Icons.Filled.Person)
)

/** Índice de la pestaña asociada a [route], o -1 si no es una pestaña. */
fun indiceDeTab(route: String?): Int = bottomTabs.indexOfFirst { it.route == route }

@Composable
private fun coloresBarra(): NavigationBarItemColors = NavigationBarItemDefaults.colors(
    selectedIconColor = ContainerDark,
    // Blanco sobre el verde de la barra: 7:1. El ámbar solo daba ~3.5:1 en texto pequeño.
    selectedTextColor = Color.White,
    indicatorColor = AmberAccent,
    unselectedIconColor = Color.White.copy(alpha = 0.8f),
    unselectedTextColor = Color.White.copy(alpha = 0.8f)
)

/**
 * Barra inferior de navegación: muestra solo los íconos y el nombre únicamente
 * de la pestaña seleccionada, para que no se vea amontonada en pantallas
 * angostas. Las pestañas no seleccionadas conservan su nombre como
 * `contentDescription` para TalkBack, y el seleccionado lo expone el texto.
 */
@Composable
fun CampusBottomBar(selectedIndex: Int, onSelect: (Int) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        tonalElevation = 0.dp
    ) {
        bottomTabs.forEachIndexed { indice, tab ->
            val seleccionada = indice == selectedIndex
            NavigationBarItem(
                selected = seleccionada,
                onClick = { onSelect(indice) },
                icon = {
                    Icon(
                        tab.icon,
                        // Seleccionada: el nombre ya está en el texto visible (evita leerlo doble).
                        contentDescription = if (seleccionada) null else tab.label
                    )
                },
                label = {
                    Text(
                        tab.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                },
                alwaysShowLabel = false,
                colors = coloresBarra()
            )
        }
    }
}

/**
 * Riel lateral para pantallas anchas o en horizontal: aprovecha el ancho y no
 * le quita altura al contenido. Mismo comportamiento que la barra inferior.
 */
@Composable
fun CampusNavigationRail(selectedIndex: Int, onSelect: (Int) -> Unit) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        // Desplazable: en teléfonos en horizontal los 5 ítems pueden no caber.
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        bottomTabs.forEachIndexed { indice, tab ->
            val seleccionada = indice == selectedIndex
            NavigationRailItem(
                selected = seleccionada,
                onClick = { onSelect(indice) },
                icon = {
                    Icon(tab.icon, contentDescription = if (seleccionada) null else tab.label)
                },
                label = {
                    Text(tab.label, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                },
                alwaysShowLabel = false,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = ContainerDark,
                    selectedTextColor = Color.White,
                    indicatorColor = AmberAccent,
                    unselectedIconColor = Color.White.copy(alpha = 0.8f),
                    unselectedTextColor = Color.White.copy(alpha = 0.8f)
                )
            )
        }
    }
}
