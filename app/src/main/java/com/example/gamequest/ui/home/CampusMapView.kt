package com.example.gamequest.ui.home

import androidx.compose.foundation.Canvas
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
import com.example.gamequest.util.CharacterSpecies
import com.example.gamequest.util.SpriteColorEngine.CharacterColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Size
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import kotlin.math.hypot

/**
 * Mapa 2.5D offline del campus. Las coordenadas de Room siguen siendo
 * normalizadas (posX/posY); la profundidad es únicamente visual y no se
 * persiste. Esto permite evolucionar a un renderer 3D sin cambiar el dominio.
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

private data class Edificio(
    val x: Float,
    val y: Float,
    val ancho: Float,
    val alto: Float,
    val color: Color
)

private val edificios = listOf(
    Edificio(0.12f, 0.26f, 0.23f, 0.13f, Color(0xFFD8A85F)),
    Edificio(0.63f, 0.20f, 0.24f, 0.15f, Color(0xFFB97C54)),
    Edificio(0.18f, 0.65f, 0.27f, 0.13f, Color(0xFF9D8BC4)),
    Edificio(0.62f, 0.60f, 0.22f, 0.17f, Color(0xFF5D9EAD))
)

@Composable
fun CampusMapView(
    puntos: List<PuntoInteresEntity>,
    completados: Set<Int>,
    onPuntoClick: (PuntoInteresEntity) -> Unit,
    modifier: Modifier = Modifier,
    characterColor: CharacterColor = CharacterColor.BLUE_ORIGINAL,
    characterSpecies: CharacterSpecies = CharacterSpecies.DUDE
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(6.dp))
            .border(3.dp, AmberAccent, RoundedCornerShape(6.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(puntos) {
                detectTapGestures { tapOffset ->
                    if (size.width == 0) return@detectTapGestures
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
        drawRect(Color(0xFF173D3A))
        drawRoundRect(
            color = Color(0xFF2E7651),
            topLeft = Offset(size.width * 0.035f, size.height * 0.04f),
            size = Size(size.width * 0.93f, size.height * 0.91f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(28f)
        )

        // Líneas de profundidad del terreno para sugerir una vista inclinada.
        for (index in 1..7) {
            val y = size.height * (0.10f + index * 0.105f)
            drawLine(
                color = Color(0xFF4B9968).copy(alpha = 0.55f),
                start = Offset(size.width * 0.08f, y),
                end = Offset(size.width * 0.92f, y + size.height * 0.035f),
                strokeWidth = 2f
            )
        }

        // Caminos con borde y superficie para dar volumen al plano.
        val camino = Color(0xFFD9C487)
        drawLine(
            color = Color(0xFF8C6E49),
            start = Offset(size.width * 0.50f, size.height * 0.05f),
            end = Offset(size.width * 0.50f, size.height * 0.95f),
            strokeWidth = 42f
        )
        drawLine(
            color = camino,
            start = Offset(size.width * 0.50f, size.height * 0.05f),
            end = Offset(size.width * 0.50f, size.height * 0.95f),
            strokeWidth = 34f
        )
        drawLine(
            color = Color(0xFF8C6E49),
            start = Offset(size.width * 0.07f, size.height * 0.46f),
            end = Offset(size.width * 0.93f, size.height * 0.49f),
            strokeWidth = 42f
        )
        drawLine(
            color = camino,
            start = Offset(size.width * 0.07f, size.height * 0.46f),
            end = Offset(size.width * 0.93f, size.height * 0.49f),
            strokeWidth = 34f
        )

        // Edificios estilizados: una cara frontal y una cara lateral extruida.
        edificios.forEach { edificio ->
            val left = size.width * edificio.x
            val top = size.height * edificio.y
            val width = size.width * edificio.ancho
            val height = size.height * edificio.alto
            val profundidad = size.minDimension * 0.035f
            val frente = Path().apply {
                moveTo(left, top)
                lineTo(left + width, top)
                lineTo(left + width, top + height)
                lineTo(left, top + height)
                close()
            }
            val lateral = Path().apply {
                moveTo(left + width, top)
                lineTo(left + width + profundidad, top - profundidad * 0.55f)
                lineTo(left + width + profundidad, top + height - profundidad * 0.55f)
                lineTo(left + width, top + height)
                close()
            }
            drawPath(lateral, Color(0xFF684D43))
            drawPath(frente, edificio.color)
            drawRect(
                color = Color.White.copy(alpha = 0.22f),
                topLeft = Offset(left + width * 0.12f, top + height * 0.18f),
                size = Size(width * 0.16f, height * 0.22f)
            )
            drawRect(
                color = Color.White.copy(alpha = 0.22f),
                topLeft = Offset(left + width * 0.38f, top + height * 0.18f),
                size = Size(width * 0.16f, height * 0.22f)
            )
        }

        // Árboles decorativos sobre el terreno.
        posicionesArboles.forEach { (rx, ry) ->
            val cx = size.width * rx
            val cy = size.height * ry
            val r = size.minDimension * 0.045f
            drawCircle(color = Color(0xFF163A2B), radius = r, center = Offset(cx + r * 0.25f, cy + r * 0.55f))
            drawCircle(color = Color(0xFF276D3E), radius = r * 0.82f, center = Offset(cx, cy))
            drawCircle(color = Color(0xFF55A85B), radius = r * 0.52f, center = Offset(cx - r * 0.18f, cy - r * 0.18f))
            drawRect(
                color = Color(0xFF68432B),
                topLeft = Offset(cx - r * 0.18f, cy + r * 0.58f),
                size = Size(r * 0.36f, r * 0.75f)
            )
        }

        // Ubicación aproximada del usuario en el cruce central.
        drawCircle(
            color = AmberAccent.copy(alpha = 0.25f),
            radius = size.minDimension * 0.11f,
            center = Offset(size.width * 0.5f, size.height * 0.45f)
        )

        // Marcadores elevados: sombra, poste y pin conservan el toque sobre posX/posY.
        puntos.forEach { punto ->
            val center = Offset(punto.posX * size.width, punto.posY * size.height)
            val esCompletado = punto.id in completados
            val color = if (esCompletado) {
                Color(0xFF2E7D32)
            } else {
                colorPorCategoria(punto.categoria, primary, secondary, tertiary)
            }
            val pinCenter = center - Offset(0f, 14f)
            drawOval(
                color = Color.Black.copy(alpha = 0.35f),
                topLeft = Offset(center.x - 24f, center.y - 7f),
                size = Size(48f, 14f)
            )
            drawLine(
                color = Color(0xFF102D2B),
                start = center,
                end = pinCenter,
                strokeWidth = 5f
            )
            drawCircle(color = Color(0xFF102D2B), radius = 25f, center = pinCenter)
            drawCircle(color = color, radius = 20f, center = pinCenter)
            drawCircle(color = if (esCompletado) Color.White else AmberAccent, radius = 6f, center = pinCenter)
        }
    }

    // Avatar pixel art animado sobre el cruce de caminos del campus
    DudeSprite(
        species = characterSpecies,
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
