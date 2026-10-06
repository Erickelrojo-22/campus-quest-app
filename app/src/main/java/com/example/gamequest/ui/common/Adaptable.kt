package com.example.gamequest.ui.common

import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Ancho mínimo (dp) a partir del cual se usa riel lateral en lugar de barra inferior. */
val ANCHO_PANTALLA_AMPLIA = 600.dp

/**
 * Marca un título como encabezado para lectores de pantalla (TalkBack permite
 * saltar entre encabezados, lo que acelera mucho la navegación).
 */
fun Modifier.encabezado(): Modifier = semantics { heading() }

/**
 * Centra el contenido y limita su ancho en pantallas grandes / horizontales,
 * para que formularios y listas no se estiren a todo el ancho de una tablet.
 */
@Composable
fun ContenidoAdaptable(
    modifier: Modifier = Modifier,
    anchoMaximo: Dp = 560.dp,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.TopCenter) {
        Box(Modifier.widthIn(max = anchoMaximo).fillMaxWidth()) { content() }
    }
}

/**
 * `true` si el usuario desactivó las animaciones del sistema (Opciones de
 * desarrollador / "Eliminar animaciones"). Se usa para saltar transiciones.
 */
@Composable
fun rememberReducirMovimiento(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
}
