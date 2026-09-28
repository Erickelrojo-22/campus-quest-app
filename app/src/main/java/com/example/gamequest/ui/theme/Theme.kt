package com.example.gamequest.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Esquinas cuadradas / mínimamente redondeadas, como en los mockups de
// pixel art (paneles y botones con borde grueso, sin curvas suaves).
private val PixelShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(4.dp),
    extraLarge = RoundedCornerShape(6.dp)
)

private val DarkColorScheme = darkColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealPrimaryLight,
    onPrimaryContainer = Color.White,
    secondary = AmberAccent,
    onSecondary = PixelInkOnCream,
    secondaryContainer = AmberAccentDark,
    onSecondaryContainer = Color.White,
    tertiary = InstitutionalRed,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = ContainerDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = TealPrimary,
    onSurfaceVariant = OnSurfaceDark,
    outline = NeutralOutline,
    error = InstitutionalRed
)

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = ContainerLight,
    onPrimaryContainer = TealPrimaryDark,
    secondary = AmberAccentDark,
    onSecondary = Color.White,
    secondaryContainer = PixelCream,
    tertiary = InstitutionalRed,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = PixelCream,
    onSurface = OnSurfaceLight,
    surfaceVariant = ContainerLight,
    onSurfaceVariant = OnSurfaceLight,
    outline = NeutralOutlineLight,
    error = InstitutionalRed
)

/**
 * Tema visual de Campus Quest — estilo pixel art / RPG retro (apartado
 * "Mockups Pixel Art - Campus Quest"). [darkTheme] se controla desde las
 * preferencias del usuario (persistidas con DataStore), no desde el tema
 * del sistema, tal como se define en la pantalla de Configuración
 * (RF-18 / RF-19). Por defecto se muestra la variante oscura, que es la
 * que ilustran todos los mockups de la propuesta visual.
 */
@Composable
fun GamequestTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = PixelShapes,
        content = content
    )
}
