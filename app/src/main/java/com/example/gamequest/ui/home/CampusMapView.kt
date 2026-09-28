package com.example.gamequest.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import com.example.gamequest.ui.theme.AmberAccent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@Composable
fun CampusMapView(
    puntos: List<PuntoInteresEntity>,
    completados: Set<Int>,
    onPuntoClick: (PuntoInteresEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    var tamano by remember { mutableStateOf(Size.Zero) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF3E9142))
            .border(3.dp, AmberAccent, RoundedCornerShape(6.dp))
            .pointerInput(puntos) {
                detectTapGestures { tapOffset ->
                    if (tamano.width == 0f) return@detectTapGestures
                    val radioToque = 28f
                    val objetivo = puntos.minByOrNull { punto ->
                        val px = punto.posX * tamano.width
                        val py = punto.posY * tamano.height
                        hypot((px - tapOffset.x).toDouble(), (py - tapOffset.y).toDouble())
                    }
                    if (objetivo != null) {
                        val px = objetivo.posX * tamano.width
                        val py = objetivo.posY * tamano.height
                        val distancia = hypot((px - tapOffset.x).toDouble(), (py - tapOffset.y).toDouble())
                        if (distancia <= radioToque) onPuntoClick(objetivo)
                    }
                }
            }
    ) {
        tamano = size

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
        listOf(
            0.10f to 0.12f, 0.88f to 0.10f,
            0.09f to 0.88f, 0.90f to 0.85f, 0.68f to 0.18f
        ).forEach { (rx, ry) ->
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

        // Ubicación aproximada del usuario, en el cruce central de caminos
        drawCircle(
            color = AmberAccent.copy(alpha = 0.25f),
            radius = size.minDimension * 0.11f,
            center = Offset(size.width * 0.5f, size.height * 0.45f)
        )
        drawCircle(
            color = Color(0xFF2F80ED),
            radius = 14f,
            center = Offset(size.width * 0.5f, size.height * 0.45f)
        )
        drawCircle(
            color = Color.White,
            radius = 14f,
            center = Offset(size.width * 0.5f, size.height * 0.45f),
            style = Stroke(width = 3f)
        )

        puntos.forEach { punto ->
            val center = Offset(punto.posX * size.width, punto.posY * size.height)
            val color = if (punto.id in completados) {
                Color(0xFF2E7D32)
            } else {
                colorPorCategoria(punto.categoria, primary, secondary, tertiary)
            }
            drawCircle(color = Color(0xFF0A2E2C), radius = 21f, center = center)
            drawCircle(color = color, radius = 16f, center = center)
        }
    }
}
