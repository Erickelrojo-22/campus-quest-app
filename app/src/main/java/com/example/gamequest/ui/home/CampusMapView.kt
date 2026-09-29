package com.example.gamequest.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import com.example.gamequest.ui.components.DudeAnimation
import com.example.gamequest.ui.components.DudeSprite
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.util.SpriteColorEngine.CharacterColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Size
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import kotlin.math.hypot

/**
 * RF-05: mapa estilizado del campus con los puntos de interés señalizados y
 * la ubicación aproximada del usuario. No usa Google Maps (no requiere clave
 * de API ni conexión), tal como se ilustra en el mockup "Home / Mapa del campus".
 */
private fun colorPorCategoria(categoria: String, primary: Color, secondary: Color, tertiary: Color): Color =
    when (categoria) {
        "Académico" -> AmberAccent
        "Trámites" -> secondary
        "Recreación" -> Color(0xFF6BC96B)
        else -> tertiary
    }

private val posicionesArboles = listOf(
    0.10f to 0.12f, 0.88f to 0.10f,
    0.09f to 0.88f, 0.90f to 0.85f, 0.68f to 0.18f
)

@Composable
fun CampusMapView(
    puntos: List<PuntoInteresEntity>,
    completados: Set<Int>,
    onPuntoClick: (PuntoInteresEntity) -> Unit,
    modifier: Modifier = Modifier,
    characterColor: CharacterColor = CharacterColor.BLUE_ORIGINAL
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF3E9142))
            .border(3.dp, AmberAccent, RoundedCornerShape(6.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(puntos) {
                // `size` (px) lo provee el propio PointerInputScope; así se evita
                // guardar el tamaño en un estado escrito durante el dibujo, que
                // podía provocar recomposiciones innecesarias del Canvas.
                detectTapGestures { tapOffset ->
                    if (size.width == 0) return@detectTapGestures
                    // Aumentado a 65f para facilitar el toque táctil en pantallas de alta densidad (QA issue)
                    val radioToque = 65f
                    val objetivo = puntos.minByOrNull { punto ->
                        val px = punto.posX * size.width
                        val py = punto.posY * size.height
                        hypot((px - tapOffset.x).toDouble(), (py - tapOffset.y).toDouble())
                    }
                    if (objetivo != null) {
                        val px = objetivo.posX * size.width
                        val py = objetivo.posY * size.height
                        val distancia = hypot((px - tapOffset.x).toDouble(), (py - tapOffset.y).toDouble())
                        if (distancia <= radioToque) onPuntoClick(objetivo)
                    }

                }
            }
    ) {
        // Textura de césped a cuadros, como el suelo de un RPG 2D cenital
        val tile = size.minDimension / 14f
        var fila = 0
        var y = 0f
        while (y < size.height) {
            var columna = 0
            var x = 0f
            while (x < size.width) {
                if ((fila + columna) % 2 == 0) {
                    drawRect(
                        color = Color(0xFF3B8A3F),
                        topLeft = Offset(x, y),
                        size = Size(tile, tile)
                    )
                }
                x += tile
                columna++
            }
            y += tile
            fila++
        }

        // Árboles pixelados en las esquinas del campus (fuera de los caminos)
        posicionesArboles.forEach { (rx, ry) ->
            val cx = size.width * rx
            val cy = size.height * ry
            val r = size.minDimension * 0.045f
            drawCircle(color = Color(0xFF1E5A2A), radius = r, center = Offset(cx, cy + r * 0.3f))
            drawCircle(color = Color(0xFF2F8F42), radius = r * 0.8f, center = Offset(cx, cy))
            drawCircle(color = Color(0xFF5E3A1E), radius = r * 0.22f, center = Offset(cx, cy + r * 1.15f))
        }

        // Caminos principales de tierra, como en un mapa RPG cenital
        drawLine(
            color = Color(0xFFD8C58C),
            start = Offset(size.width * 0.5f, 0f),
            end = Offset(size.width * 0.5f, size.height),
            strokeWidth = 30f
        )
        drawLine(
            color = Color(0xFFD8C58C),
            start = Offset(0f, size.height * 0.45f),
            end = Offset(size.width, size.height * 0.45f),
            strokeWidth = 30f
        )

        // Aura suave de ubicación aproximada del usuario en el cruce de caminos
        drawCircle(
            color = AmberAccent.copy(alpha = 0.25f),
            radius = size.minDimension * 0.11f,
            center = Offset(size.width * 0.5f, size.height * 0.45f)
        )

        puntos.forEach { punto ->
            val center = Offset(punto.posX * size.width, punto.posY * size.height)
            val esCompletado = punto.id in completados
            val color = if (esCompletado) {
                Color(0xFF2E7D32)
            } else {
                colorPorCategoria(punto.categoria, primary, secondary, tertiary)
            }
            // Halo exterior translúcido para visibilidad y contraste
            drawCircle(color = Color.Black.copy(alpha = 0.35f), radius = 27f, center = center)
            // Borde retro oscuro
            drawCircle(color = Color(0xFF0A2E2C), radius = 24f, center = center)
            // Círculo temático
            drawCircle(color = color, radius = 19f, center = center)
            // Indicador central brillante
            drawCircle(color = if (esCompletado) Color.White else AmberAccent, radius = 6f, center = center)
        }
    }

    // Avatar pixel art animado sobre el cruce de caminos del campus
    DudeSprite(
        animation = DudeAnimation.IDLE,
        color = characterColor,
        size = 46.dp,
        modifier = Modifier.offset(
            x = maxWidth * 0.5f - 23.dp,
            y = maxHeight * 0.45f - 30.dp
        )
    )
}
}
