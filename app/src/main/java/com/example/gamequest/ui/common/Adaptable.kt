package com.example.gamequest.ui.common

import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Clasificación por espacio útil de la ventana, nunca por marca, modelo ni
 * píxeles físicos. Una misma resolución puede representar tamaños reales
 * distintos según su densidad; por eso la UI se decide en dp.
 *
 * Perfiles de referencia que se validan en la aplicación:
 * - [COMPACTO_PEQUENO]: ancho menor a 360dp; teléfonos pequeños o ventana
 *   reducida en multitarea.
 * - [COMPACTO]: 360dp a 599dp; la mayoría de teléfonos en vertical, incluso
 *   si tienen notch, perforación o navegación por gestos.
 * - [MEDIO]: 600dp a 839dp; tablets pequeñas/medianas y teléfonos plegables
 *   abiertos. Se usa riel lateral.
 * - [EXPANDIDO]: desde 840dp; tablets grandes, con validación visual hasta
 *   11 pulgadas. Pantallas mayores conservan este diseño fluido.
 *
 * El alto compacto (< 480dp) representa principalmente teléfonos en
 * horizontal. No cambia el perfil de ancho, pero evita diseños altos fijos.
 */
enum class ClaseAnchoVentana {
    COMPACTO_PEQUENO,
    COMPACTO,
    MEDIO,
    EXPANDIDO
}

enum class ClaseAltoVentana { COMPACTO, NORMAL }

data class PerfilVentanaCampus(
    val ancho: ClaseAnchoVentana,
    val alto: ClaseAltoVentana
) {
    /** El riel evita comprimir cinco destinos cuando falta altura o sobra ancho. */
    val usarRielNavegacion: Boolean
        get() = ancho == ClaseAnchoVentana.MEDIO ||
            ancho == ClaseAnchoVentana.EXPANDIDO ||
            alto == ClaseAltoVentana.COMPACTO

    /** Margen base de pantallas; el contenido puede limitar además su ancho máximo. */
    val margenHorizontal: Dp
        get() = when (ancho) {
            ClaseAnchoVentana.COMPACTO_PEQUENO -> 12.dp
            ClaseAnchoVentana.COMPACTO -> 16.dp
            ClaseAnchoVentana.MEDIO -> 24.dp
            ClaseAnchoVentana.EXPANDIDO -> 32.dp
        }
}

fun perfilVentanaCampus(ancho: Dp, alto: Dp): PerfilVentanaCampus = PerfilVentanaCampus(
    ancho = when {
        ancho < 360.dp -> ClaseAnchoVentana.COMPACTO_PEQUENO
        ancho < 600.dp -> ClaseAnchoVentana.COMPACTO
        ancho < 840.dp -> ClaseAnchoVentana.MEDIO
        else -> ClaseAnchoVentana.EXPANDIDO
    },
    alto = if (alto < 480.dp) ClaseAltoVentana.COMPACTO else ClaseAltoVentana.NORMAL
)

val LocalPerfilVentanaCampus = staticCompositionLocalOf {
    perfilVentanaCampus(ancho = 360.dp, alto = 800.dp)
}

/**
 * Provee una única clasificación dinámica a toda la app. Cambia al rotar,
 * redimensionar, dividir pantalla o abrir un plegable, sin asumir un modelo.
 */
@Composable
fun ProveedorPerfilVentanaCampus(content: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val perfil = perfilVentanaCampus(maxWidth, maxHeight)
        CompositionLocalProvider(LocalPerfilVentanaCampus provides perfil, content = content)
    }
}

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
