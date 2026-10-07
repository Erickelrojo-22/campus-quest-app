package com.example.gamequest.ui.home

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import com.example.gamequest.ui.components.CharacterAnimation
import com.example.gamequest.ui.components.CharacterSprite
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.util.AvatarColor
import com.example.gamequest.util.CharacterSpecies
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.delay
import java.util.Calendar
import com.example.gamequest.util.LocalSoundManager
import com.example.gamequest.util.SoundEffect

// ── Escala del mundo ────────────────────────────────────────────────────────────
// El tamaño del mapa se deriva de la resolución REAL de la imagen (no del ancho de la pantalla),
// así la proporción mapa ↔ personaje es idéntica en cualquier teléfono o tablet.

/** dp de pantalla por cada píxel de la imagen del mapa (zoom inicial). */
private const val MAP_DP_PER_IMAGE_PX = 2.2f
/** Zoom mínimo y máximo permitido con pinch o con los botones +/−. */
private const val MAP_MIN_DP_PER_IMAGE_PX = 1.4f
private const val MAP_MAX_DP_PER_IMAGE_PX = 3.5f
/** Incremento de zoom por cada pulsación de los botones +/−. */
private const val MAP_ZOOM_STEP = 1.3f

/** Dimensiones de respaldo de la imagen del mapa (si aún no se ha decodificado). */
private const val FALLBACK_MAP_W_PX = 896
private const val FALLBACK_MAP_H_PX = 1200

/** Tamaño del sprite en el zoom inicial; escala junto con el mapa para mantener la proporción. */
private val BASE_SPRITE_SIZE = 48.dp

/** Velocidad de caminata (en dp del mundo, zoom inicial) ≈ 2.5 cuerpos por segundo. */
private const val WALK_SPEED_DP_PER_SEC = 120f
/** Duración máxima de un trayecto: en distancias largas el avatar "trota" para no hacerse tedioso. */
private const val MAX_WALK_DURATION_MS = 4000
/** Velocidad tranquila de paseo de los NPCs (dp del mundo por segundo). */
private const val NPC_SPEED_DP_PER_SEC = 70f

private fun colorPorCategoria(categoria: String, primary: Color, secondary: Color, tertiary: Color): Color =
    when (categoria) {
        "Académico"  -> AmberAccent
        "Trámites"   -> secondary
        "Recreación" -> Color(0xFF6BC96B)
        else         -> tertiary
    }

/**
 * Clase para manejar el estado y animaciones de NPCs en el mapa
 */
class NpcState(
    val id: Int,
    val species: CharacterSpecies,
    val primaryColor: AvatarColor,
    val secondaryColor: AvatarColor,
    initialX: Float,
    initialY: Float
) {
    val x = Animatable(initialX)
    val y = Animatable(initialY)
    var isWalking by mutableStateOf(false)
    var flipX by mutableStateOf(false)
}

/**
 * Entidad renderizable para ordenar correctamente por Y (profundidad)
 */
data class RenderableEntity(
    val isPlayer: Boolean,
    val id: Int,
    val x: Float,
    val y: Float,
    val isWalking: Boolean,
    val species: CharacterSpecies,
    val primary: AvatarColor,
    val secondary: AvatarColor,
    val flipX: Boolean
)

/**
 * Mapa estilo Pokémon GO / RPG interactivo.
 *
 * Características:
 *  - Carga el mapa completo desde assets (`mapAssetPath`). Si en el futuro se cambia la imagen,
 *    todo sigue funcionando automáticamente.
 *  - La cámara sigue al avatar en tiempo real en una vista ampliada (zoom).
 *  - La escala del mundo depende de la resolución de la imagen (dp por píxel), no de la pantalla,
 *    por lo que la proporción mapa ↔ personaje es consistente en todos los dispositivos.
 *  - Zoom con pinch (dos dedos) o con botones +/− accesibles; el personaje escala junto al mapa.
 *  - Pulso de exploración (radar Pokémon GO) alrededor del avatar.
 *  - Tocar cualquier punto del mapa hace que el avatar camine suavemente hacia allá con animación WALK.
 *  - Tocar un Punto de Interés hace que el avatar camine hacia la misión y abra sus detalles.
 *  - Permite arrastrar libremente (pan) con el dedo, con botón flotante para recentrar en el avatar.
 *  - Ciclo de día/noche en base a la hora actual.
 *  - NPCs caminando por el mapa de manera aleatoria.
 */
@Composable
fun CampusMapView(
    puntos: List<PuntoInteresEntity>,
    completados: Set<Int>,
    onPuntoClick: (PuntoInteresEntity) -> Unit,
    modifier: Modifier = Modifier,
    puntoObjetivo: PuntoInteresEntity? = null,
    primaryColor: AvatarColor = AvatarColor.COBALT_BLUE,
    secondaryColor: AvatarColor = AvatarColor.RUBY_RED,
    characterSpecies: CharacterSpecies = CharacterSpecies.DUDE,
    mapAssetPath: String = "maps/campus_map_uleam.png",
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val primary   = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary  = MaterialTheme.colorScheme.tertiary

    val soundManager = LocalSoundManager.current

    // Cargar mapa en memoria desde assets
    val mapImage = remember(mapAssetPath) {
        runCatching {
            context.assets.open(mapAssetPath).use { stream ->
                BitmapFactory.decodeStream(stream)?.asImageBitmap()
            }
        }.getOrNull()
    }

    // Resolución real de la imagen: define el tamaño y la proporción del mundo
    val imageWidthPx  = mapImage?.width  ?: FALLBACK_MAP_W_PX
    val imageHeightPx = mapImage?.height ?: FALLBACK_MAP_H_PX

    // Coordenadas normalizadas del jugador (0.0 a 1.0)
    // Inicia en el centro neurálgico del campus (cerca del estadio y senderos)
    val playerX = remember { Animatable(0.48f) }
    val playerY = remember { Animatable(0.58f) }

    // Estado de la animación del avatar (IDLE cuando está quieto, WALK cuando camina)
    var isWalking by remember { mutableStateOf(false) }
    var playerFlipX by remember { mutableStateOf(false) }

    // Determinar color de overlay basado en la hora (Ciclo Día/Noche)
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val timeOverlayColor = remember(currentHour) {
        when (currentHour) {
            in 0..5, in 19..23 -> Color(0x66000033) // Noche (azul oscuro)
            in 17..18 -> Color(0x33FF6600) // Atardecer (naranja)
            else -> Color.Transparent // Día
        }
    }

    // Instanciar algunos NPCs
    val npcs = remember {
        listOf(
            NpcState(1, CharacterSpecies.DRAKE, AvatarColor.EMERALD, AvatarColor.FIRE_ORANGE, 0.40f, 0.50f),
            NpcState(2, CharacterSpecies.PINK, AvatarColor.LAVENDER, AvatarColor.PASTEL_PINK, 0.55f, 0.65f),
            NpcState(3, CharacterSpecies.OWLET, AvatarColor.ICE_BLUE, AvatarColor.STEEL_GREY, 0.60f, 0.45f)
        )
    }

    // Lógica de movimiento automático para los NPCs
    // Los pasos se miden en dp del mundo (no en % del mapa) para que su recorrido sea proporcional a su tamaño.
    LaunchedEffect(imageWidthPx, imageHeightPx) {
        val worldWidthDp  = imageWidthPx  * MAP_DP_PER_IMAGE_PX
        val worldHeightDp = imageHeightPx * MAP_DP_PER_IMAGE_PX
        npcs.forEach { npc ->
            launch {
                while (true) {
                    // Esperar un tiempo aleatorio antes de moverse
                    delay(Random.nextLong(2000, 7000))
                    val stepDp = 60f + Random.nextFloat() * 100f
                    val angle  = Random.nextFloat() * 2f * PI.toFloat()
                    val newX = (npc.x.value + cos(angle) * stepDp / worldWidthDp).coerceIn(0.08f, 0.92f)
                    val newY = (npc.y.value + sin(angle) * stepDp / worldHeightDp).coerceIn(0.08f, 0.92f)
                    val distDp = hypot((newX - npc.x.value) * worldWidthDp, (newY - npc.y.value) * worldHeightDp)
                    val duration = (distDp / NPC_SPEED_DP_PER_SEC * 1000f).roundToInt().coerceAtLeast(300)

                    npc.flipX = newX < npc.x.value
                    npc.isWalking = true
                    val xAnim = launch { npc.x.animateTo(newX, tween(duration, easing = LinearEasing)) }
                    val yAnim = launch { npc.y.animateTo(newY, tween(duration, easing = LinearEasing)) }
                    xAnim.join()
                    yAnim.join()
                    npc.isWalking = false
                }
            }
        }
    }

    // Nivel de zoom actual (dp por píxel de imagen). Ajustable con pinch o botones +/−.
    var mapScale by remember { mutableFloatStateOf(MAP_DP_PER_IMAGE_PX) }

    // Radar / pulso de alcance estilo Pokémon GO a los pies del avatar
    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse_progress",
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.45f,
        targetValue   = 0.0f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse_alpha",
    )

    // Desplazamiento manual temporal del usuario (drag para mirar alrededor)
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }
    val isDragged by remember { derivedStateOf { abs(panX) > 0.5f || abs(panY) > 0.5f } }
    var recenterJob by remember { mutableStateOf<Job?>(null) }
    var zoomJob by remember { mutableStateOf<Job?>(null) }

    var walkJob by remember { mutableStateOf<Job?>(null) }
    var targetMarkerPos by remember { mutableStateOf<Offset?>(null) }

    // Re-centrar suavemente la cámara sobre el avatar
    val recenterCamera: () -> Unit = {
        recenterJob?.cancel()
        val startPanX = panX
        val startPanY = panY
        recenterJob = scope.launch {
            animate(1f, 0f, animationSpec = tween(350, easing = FastOutSlowInEasing)) { f, _ ->
                panX = startPanX * f
                panY = startPanY * f
            }
        }
    }

    // Cambia el zoom manteniendo el mismo punto del mundo en el centro de la vista
    val applyZoom: (Float) -> Unit = { requestedScale ->
        val oldScale = mapScale
        val newScale = requestedScale.coerceIn(MAP_MIN_DP_PER_IMAGE_PX, MAP_MAX_DP_PER_IMAGE_PX)
        if (newScale != oldScale) {
            val ratio = newScale / oldScale
            mapScale = newScale
            panX *= ratio
            panY *= ratio
        }
    }

    // Zoom animado para los botones +/− (alternativa accesible al pinch de dos dedos)
    val animateZoomTo: (Float) -> Unit = { target ->
        zoomJob?.cancel()
        val from = mapScale
        val to = target.coerceIn(MAP_MIN_DP_PER_IMAGE_PX, MAP_MAX_DP_PER_IMAGE_PX)
        zoomJob = scope.launch {
            animate(from, to, animationSpec = tween(250, easing = FastOutSlowInEasing)) { value, _ ->
                applyZoom(value)
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, AmberAccent, RoundedCornerShape(12.dp))
            .background(Color(0xFF23352B))
    ) {
        val viewW = constraints.maxWidth.toFloat()
        val viewH = constraints.maxHeight.toFloat()

        if (viewW <= 0 || viewH <= 0) return@BoxWithConstraints

        val dpToPx = density.density
        // Relación respecto al zoom inicial: el personaje, el radar y la velocidad escalan con el mundo
        val zoomRatio = mapScale / MAP_DP_PER_IMAGE_PX

        // Dimensiones del mapa escalado (respetando la proporción real de la imagen)
        val mapW = imageWidthPx * mapScale * dpToPx
        val mapH = imageHeightPx * mapScale * dpToPx

        // Posición mundial del jugador
        val curX = playerX.value
        val curY = playerY.value
        val worldPlayerX = curX * mapW
        val worldPlayerY = curY * mapH

        // Cámara para centrar al jugador en el viewport + offset manual de arrastre
        val rawCamX = (viewW / 2f) - worldPlayerX + panX
        val rawCamY = (viewH / 2f) - worldPlayerY + panY

        // Restringir la cámara para que no muestre vacío fuera del mapa
        // (si el mapa fuese más pequeño que la vista, se centra)
        val camX = if (mapW > viewW) rawCamX.coerceIn(viewW - mapW, 0f) else (viewW - mapW) / 2f
        val camY = if (mapH > viewH) rawCamY.coerceIn(viewH - mapH, 0f) else (viewH - mapH) / 2f

        val currentCamX by rememberUpdatedState(camX)
        val currentCamY by rememberUpdatedState(camY)
        val currentMapW by rememberUpdatedState(mapW)
        val currentMapH by rememberUpdatedState(mapH)
        val currentPuntos by rememberUpdatedState(puntos)

        // Medidas de la UI del mapa en dp → px (constantes en pantalla, independientes de la densidad)
        val pinElevationPx   = 12f * dpToPx
        val pinOuterRadiusPx = 11f * dpToPx
        val pinInnerRadiusPx = 9f * dpToPx
        val pinDotRadiusPx   = 3.5f * dpToPx
        // Zona táctil de los pines: 56 dp de diámetro (supera el mínimo accesible de 48 dp)
        val pinTouchRadiusPx = 28f * dpToPx

        val startWalkingTo: (targetNormX: Float, targetNormY: Float, onArrived: (() -> Unit)?) -> Unit = { targetNormX, targetNormY, onArrived ->
            val currentTargetX = targetNormX.coerceIn(0.04f, 0.96f)
            val currentTargetY = targetNormY.coerceIn(0.04f, 0.96f)

            // Velocidad constante y natural de caminata, relativa al tamaño del mundo.
            // Se lee el zoom actual aquí (no se captura) porque el gesto puede conservar una lambda antigua.
            val walkSpeedPxPerSec = WALK_SPEED_DP_PER_SEC * dpToPx * (mapScale / MAP_DP_PER_IMAGE_PX)

            // Guardado en coordenadas normalizadas para que el marcador siga en su sitio al hacer zoom
            targetMarkerPos = Offset(currentTargetX, currentTargetY)

            walkJob?.cancel()
            walkJob = scope.launch {
                try {
                    isWalking = true
                    // Re-centrar suavemente el desplazamiento manual de la cámara si estaba desplazada
                    if (isDragged) recenterCamera()

                    val startX = playerX.value
                    val startY = playerY.value
                    val dxPx = (currentTargetX - startX) * currentMapW
                    val dyPx = (currentTargetY - startY) * currentMapH
                    val distPx = hypot(dxPx, dyPx)

                    // Orientar sprite hacia la dirección en la que camina
                    if (dxPx > 1.5f) {
                        playerFlipX = false
                    } else if (dxPx < -1.5f) {
                        playerFlipX = true
                    }

                    if (distPx > 3f) {
                        // Velocidad constante; en trayectos largos sube (trote) para no superar MAX_WALK_DURATION_MS
                        val durationMs = ((distPx / walkSpeedPxPerSec) * 1000)
                            .roundToInt()
                            .coerceIn(120, MAX_WALK_DURATION_MS)
                        val xJob = launch {
                            playerX.animateTo(currentTargetX, tween(durationMs, easing = LinearEasing))
                        }
                        val yJob = launch {
                            playerY.animateTo(currentTargetY, tween(durationMs, easing = LinearEasing))
                        }
                        xJob.join()
                        yJob.join()
                    }
                } finally {
                    isWalking = false
                    targetMarkerPos = null
                }
                onArrived?.invoke()
            }
        }

        // Canvas: Dibuja mapa, caminos, radar y pines
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downPos = down.position
                        var isTap = true
                        val touchSlop = viewConfiguration.touchSlop

                        do {
                            val event = awaitPointerEvent()
                            val pressedChanges = event.changes.filter { it.pressed }
                            if (pressedChanges.isEmpty()) break

                            if (pressedChanges.size > 1) {
                                isTap = false
                            }

                            if (isTap && pressedChanges.isNotEmpty()) {
                                val current = pressedChanges.first().position
                                val dist = hypot((current.x - downPos.x).toDouble(), (current.y - downPos.y).toDouble())
                                if (dist > touchSlop) {
                                    isTap = false
                                }
                            }

                            if (!isTap) {
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()
                                if (zoom != 1f || pan != Offset.Zero) {
                                    event.changes.forEach { it.consume() }
                                    recenterJob?.cancel()
                                    zoomJob?.cancel()
                                    if (zoom != 1f) applyZoom(mapScale * zoom)
                                    val limitX = currentMapW * 0.45f
                                    val limitY = currentMapH * 0.45f
                                    panX = (panX + pan.x).coerceIn(-limitX, limitX)
                                    panY = (panY + pan.y).coerceIn(-limitY, limitY)
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        if (isTap) {
                            val tapWorldX = downPos.x - currentCamX
                            val tapWorldY = downPos.y - currentCamY

                            soundManager?.play(SoundEffect.CLICK)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                            // 1. Verificar si tocó cerca de un Punto de Interés (se mide contra la cabeza del pin)
                            val clickedPunto = currentPuntos.minByOrNull { punto ->
                                val px = punto.posX * currentMapW
                                val py = punto.posY * currentMapH - pinElevationPx
                                hypot((px - tapWorldX).toDouble(), (py - tapWorldY).toDouble())
                            }

                            if (clickedPunto != null) {
                                val px = clickedPunto.posX * currentMapW
                                val py = clickedPunto.posY * currentMapH - pinElevationPx
                                val dist = hypot((px - tapWorldX).toDouble(), (py - tapWorldY).toDouble())
                                if (dist <= pinTouchRadiusPx) {
                                    // Caminar naturalmente hacia el punto y abrir la misión al llegar
                                    startWalkingTo(clickedPunto.posX, clickedPunto.posY) {
                                        soundManager?.play(SoundEffect.CLICK)
                                        onPuntoClick(clickedPunto)
                                    }
                                    return@awaitEachGesture
                                }
                            }

                            // 2. Tocar en el suelo del mapa: el avatar camina naturalmente a velocidad constante
                            val targetNormX = tapWorldX / currentMapW
                            val targetNormY = tapWorldY / currentMapH
                            startWalkingTo(targetNormX, targetNormY, null)
                        }
                    }
                }
        ) {
            translate(left = camX, top = camY) {
                // 1. Dibujar el mapa completo de fondo
                if (mapImage != null) {
                    drawImage(
                        image = mapImage,
                        dstSize = IntSize(mapW.roundToInt(), mapH.roundToInt()),
                        // Medium: reescalado suave sin dientes de sierra al ampliar la imagen
                        filterQuality = FilterQuality.Medium,
                        colorFilter = if (timeOverlayColor != Color.Transparent) ColorFilter.tint(timeOverlayColor, BlendMode.Darken) else null
                    )
                } else {
                    // Fallback visual si el mapa aún carga
                    drawRect(Color(0xFF2E7651), size = Size(mapW, mapH))
                    if (timeOverlayColor != Color.Transparent) {
                        drawRect(timeOverlayColor, size = Size(mapW, mapH), blendMode = BlendMode.Darken)
                    }
                }

                // 2. Pulso / Radar estilo Pokémon GO a los pies del jugador (escala con el personaje)
                drawCircle(
                    color  = AmberAccent.copy(alpha = pulseAlpha),
                    radius = (16f + 20f * pulseProgress) * dpToPx * zoomRatio,
                    center = Offset(worldPlayerX, worldPlayerY),
                    style  = Stroke(width = 1.5f * dpToPx)
                )
                drawCircle(
                    color  = AmberAccent.copy(alpha = 0.15f),
                    radius = 14f * dpToPx * zoomRatio,
                    center = Offset(worldPlayerX, worldPlayerY),
                )

                // 2.1 Indicador visual de destino al que camina el avatar
                targetMarkerPos?.let { normTarget ->
                    val target = Offset(normTarget.x * mapW, normTarget.y * mapH)
                    drawCircle(
                        color = AmberAccent.copy(alpha = 0.75f),
                        radius = (7f + 3f * pulseProgress) * dpToPx,
                        center = target,
                        style = Stroke(width = 2f * dpToPx)
                    )
                    drawCircle(
                        color = AmberAccent.copy(alpha = 0.4f),
                        radius = 2.5f * dpToPx,
                        center = target
                    )
                }

                // 3. Marcadores elevados para los puntos de interés (tamaño constante en pantalla)
                puntos.forEach { punto ->
                    val pinPos = Offset(punto.posX * mapW, punto.posY * mapH)
                    val esCompletado = punto.id in completados
                    val esObjetivo = punto.id == puntoObjetivo?.id
                    val pinColor = if (esCompletado) Color(0xFF2E7D32) else colorPorCategoria(punto.categoria, primary, secondary, tertiary)
                    val elevatedCenter = pinPos - Offset(0f, pinElevationPx)

                    if (esObjetivo) {
                        // Halo de atención animado alrededor del punto objetivo de la misión
                        drawCircle(
                            color = AmberAccent.copy(alpha = pulseAlpha),
                            radius = (14f + 12f * pulseProgress) * dpToPx,
                            center = elevatedCenter,
                            style = Stroke(width = 2f * dpToPx)
                        )
                        drawCircle(
                            color = AmberAccent.copy(alpha = 0.22f),
                            radius = 14f * dpToPx,
                            center = elevatedCenter
                        )
                    }

                    // Sombra ovalada en el suelo
                    drawOval(
                        color = Color.Black.copy(alpha = 0.35f),
                        topLeft = Offset(pinPos.x - 12f * dpToPx, pinPos.y - 3.5f * dpToPx),
                        size = Size(24f * dpToPx, 7f * dpToPx)
                    )
                    // Poste del pin
                    drawLine(
                        color = Color(0xFF102D2B),
                        start = pinPos,
                        end = elevatedCenter,
                        strokeWidth = 2f * dpToPx
                    )
                    // Cabeza del pin
                    drawCircle(color = Color(0xFF102D2B), radius = pinOuterRadiusPx, center = elevatedCenter)
                    drawCircle(color = pinColor, radius = pinInnerRadiusPx, center = elevatedCenter)
                    drawCircle(color = if (esCompletado) Color.White else if (esObjetivo) AmberAccent else Color.White, radius = pinDotRadiusPx, center = elevatedCenter)
                }
            }
        }

        // 4. Avatares animados sobre el punto exacto de la pantalla (Jugador + NPCs ordenados por Y)
        // El sprite escala con el zoom para conservar siempre la proporción respecto al mapa
        val spriteSizeDp = BASE_SPRITE_SIZE * zoomRatio
        val spriteHalfPx = with(density) { (spriteSizeDp / 2).toPx() }
        val spriteFootOffsetPx = 8f * dpToPx * zoomRatio

        // Entidades a dibujar ordenadas por Y para simular profundidad
        // Calculado dinámicamente para que los NPCs continúen caminando incluso si el jugador está quieto
        val renderables = buildList {
            add(
                RenderableEntity(
                    isPlayer = true, id = 0, x = curX, y = curY, isWalking = isWalking,
                    species = characterSpecies, primary = primaryColor, secondary = secondaryColor, flipX = playerFlipX
                )
            )
            npcs.forEach { npc ->
                add(
                    RenderableEntity(
                        isPlayer = false, id = npc.id, x = npc.x.value, y = npc.y.value, isWalking = npc.isWalking,
                        species = npc.species, primary = npc.primaryColor, secondary = npc.secondaryColor, flipX = npc.flipX
                    )
                )
            }
        }.sortedBy { it.y }

        renderables.forEach { entity ->
            val screenEntityX = entity.x * mapW + camX
            val screenEntityY = entity.y * mapH + camY

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = (screenEntityX - spriteHalfPx).roundToInt(),
                            y = (screenEntityY - spriteHalfPx - spriteFootOffsetPx).roundToInt()
                        )
                    }
                    .size(spriteSizeDp),
                contentAlignment = Alignment.Center
            ) {
                // Para hacer flip visual (mirar izq/der) usamos graphicsLayer scaleX = -1f si lo implementamos,
                // pero por ahora pasamos los colores
                CharacterSprite(
                    species        = entity.species,
                    animation      = if (entity.isWalking) CharacterAnimation.WALK else CharacterAnimation.IDLE,
                    primaryColor   = entity.primary,
                    secondaryColor = entity.secondary,
                    size           = spriteSizeDp,
                    modifier       = if (entity.flipX) Modifier.graphicsLayer { scaleX = -1f } else Modifier
                )
            }
        }

        // 5. Brújula / Indicador hacia el objetivo de la misión activa
        if (puntoObjetivo != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.5.dp, AmberAccent), RoundedCornerShape(20.dp))
                    .clickable {
                        soundManager?.play(SoundEffect.CLICK)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        startWalkingTo(puntoObjetivo.posX, puntoObjetivo.posY) {
                            soundManager?.play(SoundEffect.CLICK)
                            onPuntoClick(puntoObjetivo)
                        }
                    },
                color = Color(0xFF142E24).copy(alpha = 0.95f),
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🧭", fontSize = 18.sp)
                    Column {
                        Text(
                            "MISIÓN ACTIVA",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent
                        )
                        Text(
                            puntoObjetivo.nombre,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 6. Controles de zoom +/− (alternativa de un solo dedo al pinch, WCAG 2.5.1)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    soundManager?.play(SoundEffect.CLICK)
                    animateZoomTo(mapScale * MAP_ZOOM_STEP)
                },
                modifier = Modifier
                    .size(40.dp)
                    .border(BorderStroke(1.5.dp, AmberAccent), CircleShape),
                shape = CircleShape,
                containerColor = Color(0xFF142E24).copy(alpha = 0.92f),
                contentColor = AmberAccent,
                elevation = FloatingActionButtonDefaults.elevation(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Acercar mapa",
                    tint = AmberAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
            FloatingActionButton(
                onClick = {
                    soundManager?.play(SoundEffect.CLICK)
                    animateZoomTo(mapScale / MAP_ZOOM_STEP)
                },
                modifier = Modifier
                    .size(40.dp)
                    .border(BorderStroke(1.5.dp, AmberAccent), CircleShape),
                shape = CircleShape,
                containerColor = Color(0xFF142E24).copy(alpha = 0.92f),
                contentColor = AmberAccent,
                elevation = FloatingActionButtonDefaults.elevation(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = "Alejar mapa",
                    tint = AmberAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // 7. Botón flotante para recentrar en el avatar si el usuario arrastró el mapa
        if (isDragged) {
            FloatingActionButton(
                onClick = {
                    soundManager?.play(SoundEffect.CLICK)
                    recenterCamera()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(40.dp)
                    .border(BorderStroke(1.5.dp, AmberAccent), CircleShape),
                shape = CircleShape,
                containerColor = Color(0xFF142E24).copy(alpha = 0.92f),
                contentColor = AmberAccent,
                elevation = FloatingActionButtonDefaults.elevation(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = "Recentrar en mi personaje",
                    tint = AmberAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
