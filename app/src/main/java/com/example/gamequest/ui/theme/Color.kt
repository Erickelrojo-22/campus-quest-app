package com.example.gamequest.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta de marca — Campus Quest, estilo retro / pixel art (8 bits)
// Alineada con los mockups "Mockups Pixel Art - Campus Quest": fondos verde
// azulado profundo, acentos dorados/ámbar y paneles con borde marcado.
val TealPrimary = Color(0xFF12645B)
val TealPrimaryDark = Color(0xFF082220)
val TealPrimaryLight = Color(0xFF1E8377)
val AmberAccent = Color(0xFFF5A623)
val AmberAccentDark = Color(0xFFB97300)
val InstitutionalRed = Color(0xFFD5455A)

val SurfaceLight = Color(0xFFF3EBD8)
val SurfaceDark = Color(0xFF0A2E2C)
val OnSurfaceLight = Color(0xFF14302C)
val OnSurfaceDark = Color(0xFFE7F2EE)
val ContainerDark = Color(0xFF123F3A)
val ContainerLight = Color(0xFFDCEAE6)

val NeutralOutline = Color(0xFF3A6E66)
val NeutralOutlineLight = Color(0xFFB79A5C)

// Cajas de texto tipo "consola retro" (mockup de login)
val PixelInkOnCream = Color(0xFF1D2E2C)
val PixelCream = Color(0xFFF4E9D3)

// Estados del escáner / resultados
val PixelSuccess = Color(0xFF3FA34D)
val PixelSuccessDark = Color(0xFF276B32)

// Bordes / sombra "pixel" (contorno grueso tipo sprite)
val PixelBorder = Color(0xFF04161A)

// Colores de error con contraste AA (>= 4.5:1) sobre los fondos donde se usan:
// InstitutionalRed sobre el verde de las tarjetas solo da ~1.6:1.
val ErrorOnDark = Color(0xFFFFD2CC)   // sobre ContainerDark / TealPrimary (>= 5:1)
val ErrorOnCream = Color(0xFF9B1C2E)  // sobre PixelCream (>= 6:1)

