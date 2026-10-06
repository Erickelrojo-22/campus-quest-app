package com.example.gamequest.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import com.example.gamequest.ui.common.ANCHO_PANTALLA_AMPLIA
import com.example.gamequest.ui.common.CampusBottomBar
import com.example.gamequest.ui.common.CampusNavigationRail
import com.example.gamequest.ui.common.bottomTabs
import com.example.gamequest.ui.common.rememberReducirMovimiento
import com.example.gamequest.util.LocalSoundManager
import com.example.gamequest.util.SoundEffect
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Contenedor de las 5 pestañas principales (Inicio, Misiones, QR, Insignias, Perfil).
 *
 * - Se puede **deslizar** a izquierda/derecha entre pestañas, o tocar el menú.
 * - Teléfono en vertical: barra inferior (solo íconos; nombre en la activa).
 * - Pantalla ancha u horizontal: riel lateral (no resta altura al contenido).
 * - Solo se compone la página visible (+ las vecinas mientras se desliza), así que
 *   cada pestaña carga de forma perezosa. [contenido] recibe `activa = true`
 *   únicamente cuando la página ya se asentó (p. ej. el escáner no abre la
 *   cámara ni pide permisos mientras solo se pasa por encima).
 *
 * @param tabSolicitada índice pedido desde otra pantalla (p. ej. "Escanear" en el
 *   detalle de misión); -1 si no hay petición. Se confirma con [onTabSolicitadaAtendida].
 */
@Composable
fun MainTabsScreen(
    tabSolicitada: Int,
    onTabSolicitadaAtendida: () -> Unit,
    contenido: @Composable (tab: Int, activa: Boolean) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0) { bottomTabs.size }
    val scope = rememberCoroutineScope()
    val soundManager = LocalSoundManager.current
    val reducirMovimiento = rememberReducirMovimiento()
    val vista = LocalView.current

    // Animar solo entre vecinas: saltar de Inicio a Perfil animando compondría
    // (y haría cargar) las páginas intermedias, como la cámara del escáner.
    val desplazarA: suspend (Int) -> Unit = { indice ->
        if (reducirMovimiento || abs(indice - pagerState.currentPage) > 1) {
            pagerState.scrollToPage(indice)
        } else {
            pagerState.animateScrollToPage(indice)
        }
    }

    val irATab: (Int) -> Unit = { indice ->
        if (indice != pagerState.targetPage) {
            soundManager?.play(SoundEffect.CLICK)
            scope.launch { desplazarA(indice) }
        }
    }

    LaunchedEffect(tabSolicitada) {
        if (tabSolicitada in bottomTabs.indices) {
            desplazarA(tabSolicitada)
            onTabSolicitadaAtendida()
        }
    }

    // Atrás desde cualquier pestaña vuelve primero a Inicio y solo después sale.
    BackHandler(enabled = pagerState.currentPage != 0) {
        scope.launch { desplazarA(0) }
    }

    // Anuncia el cambio de sección a TalkBack (al deslizar no hay otro aviso).
    var anuncioInicialOmitido by remember { mutableStateOf(false) }
    LaunchedEffect(pagerState.settledPage) {
        if (!anuncioInicialOmitido) {
            anuncioInicialOmitido = true
        } else {
            @Suppress("DEPRECATION")
            vista.announceForAccessibility("Sección ${bottomTabs[pagerState.settledPage].label}")
        }
    }

    val seleccion = pagerState.targetPage

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val usarRiel = maxWidth >= ANCHO_PANTALLA_AMPLIA
        val paginas: @Composable (Modifier) -> Unit = { modifier ->
            HorizontalPager(
                state = pagerState,
                modifier = modifier,
                key = { it }
            ) { pagina ->
                contenido(pagina, pagerState.settledPage == pagina)
            }
        }

        if (usarRiel) {
            Row(Modifier.fillMaxSize()) {
                CampusNavigationRail(selectedIndex = seleccion, onSelect = irATab)
                // El riel ya respeta el inset lateral: evita que cada pantalla lo duplique.
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))
                ) {
                    paginas(Modifier.fillMaxSize())
                }
            }
        } else {
            Scaffold(
                bottomBar = { CampusBottomBar(selectedIndex = seleccion, onSelect = irATab) },
                // Cada pantalla gestiona su inset superior (algunas pintan su barra bajo la
                // barra de estado); aquí solo reservamos el alto de la barra inferior.
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { padding ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .consumeWindowInsets(padding)
                ) {
                    paginas(Modifier.fillMaxSize())
                }
            }
        }
    }
}
