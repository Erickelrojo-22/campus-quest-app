package com.example.gamequest.ui.home

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
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
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.delay
import java.util.Calendar
import com.example.gamequest.util.LocalSoundManager
import com.example.gamequest.util.SoundEffect

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
    LaunchedEffect(Unit) {
        npcs.forEach { npc ->
            launch {
                while (true) {
                    // Esperar un tiempo aleatorio antes de moverse
                    delay(Random.nextLong(2000, 7000))
                    val newX = (npc.x.value + Random.nextFloat() * 0.2f - 0.1f).coerceIn(0.1f, 0.9f)
                    val newY = (npc.y.value + Random.nextFloat() * 0.2f - 0.1f).coerceIn(0.1f, 0.9f)
                    val dist = hypot((newX - npc.x.value).toDouble(), (newY - npc.y.value).toDouble()).toFloat()
                    val duration = (dist * 4500).roundToInt().coerceIn(1000, 3500)

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

    // Nivel de zoom del mapa para ver solo el sector actual (estilo Pokémon GO)
    val zoomFactor = 2.4f

    // Radar / pulso de alcance estilo Pokémon GO a los pies del avatar
    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue  = 30f,
        targetValue   = 70f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse_radius",
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
    val dragPanX = remember { Animatable(0f) }
    val dragPanY = remember { Animatable(0f) }
    val isDragged by remember { derivedStateOf { dragPanX.value != 0f || dragPanY.value != 0f } }

    var walkJob by remember { mutableStateOf<Job?>(null) }
    var targetMarkerPos by remember { mutableStateOf<Offset?>(null) }

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

        // Dimensiones del mapa escalado
        val mapW = viewW * zoomFactor
        val mapH = viewH * zoomFactor

        // Posición mundial del jugador
        val curX = playerX.value
        val curY = playerY.value
        val worldPlayerX = curX * mapW
        val worldPlayerY = curY * mapH

        // Cámara para centrar al jugador en el viewport + offset manual de arrastre
        val rawCamX = (viewW / 2f) - worldPlayerX + dragPanX.value
        val rawCamY = (viewH / 2f) - worldPlayerY + dragPanY.value

        // Restringir la cámara para que no muestre vacío fuera del mapa
        val camX = rawCamX.coerceIn(viewW - mapW, 0f)
        val camY = rawCamY.coerceIn(viewH - mapH, 0f)

        val currentCamX by rememberUpdatedState(camX)
        val currentCamY by rememberUpdatedState(camY)
        val currentMapW by rememberUpdatedState(mapW)
        val currentMapH by rememberUpdatedState(mapH)
        val currentPuntos by rememberUpdatedState(puntos)

        // Velocidad constante y natural de caminata (~150 dp/segundo)
        val walkSpeedPxPerSec = with(density) { 150.dp.toPx() }.coerceAtLeast(130f)

        val startWalkingTo: (targetNormX: Float, targetNormY: Float, onArrived: (() -> Unit)?) -> Unit = { targetNormX, targetNormY, onArrived ->
            val currentTargetX = targetNormX.coerceIn(0.04f, 0.96f)
            val currentTargetY = targetNormY.coerceIn(0.04f, 0.96f)

            targetMarkerPos = Offset(currentTargetX * mapW, currentTargetY * mapH)

            walkJob?.cancel()
            walkJob = scope.launch {
                try {
                    isWalking = true
                    // Re-centrar suavemente el desplazamiento manual de la cámara si estaba desplazada
                    if (dragPanX.value != 0f || dragPanY.value != 0f) {
                        launch { dragPanX.animateTo(0f, tween(350, easing = FastOutSlowInEasing)) }
                        launch { dragPanY.animateTo(0f, tween(350, easing = FastOutSlowInEasing)) }
                    }

                    val startX = playerX.value
                    val startY = playerY.value
                    val dxPx = (currentTargetX - startX) * mapW
                    val dyPx = (currentTargetY - startY) * mapH
                    val distPx = hypot(dxPx.toDouble(), dyPx.toDouble()).toFloat()

                    // Orientar sprite hacia la dirección en la que camina
                    if (dxPx > 1.5f) {
                        playerFlipX = false
                    } else if (dxPx < -1.5f) {
                        playerFlipX = true
                    }

                    if (distPx > 3f) {
                        val durationMs = ((distPx / walkSpeedPxPerSec) * 1000).roundToInt().coerceAtLeast(120)
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
                    detectTapGestures { tapOffset ->
                        val tapWorldX = tapOffset.x - currentCamX
                        val tapWorldY = tapOffset.y - currentCamY

                        soundManager?.play(SoundEffect.CLICK)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                        // 1. Verificar si tocó cerca de un Punto de Interés
                        val radioPin = 60f
                        val clickedPunto = currentPuntos.minByOrNull { punto ->
                            val px = punto.posX * currentMapW
                            val py = punto.posY * currentMapH
                            hypot((px - tapWorldX).toDouble(), (py - tapWorldY).toDouble())
                        }

                        if (clickedPunto != null) {
                            val px = clickedPunto.posX * currentMapW
                            val py = clickedPunto.posY * currentMapH
                            val dist = hypot((px - tapWorldX).toDouble(), (py - tapWorldY).toDouble())
                            if (dist <= radioPin) {
                                // Caminar naturalmente hacia el punto y abrir la misión al llegar
                                startWalkingTo(clickedPunto.posX, clickedPunto.posY) {
                                    soundManager?.play(SoundEffect.CLICK)
                                    onPuntoClick(clickedPunto)
                                }
                                return@detectTapGestures
                            }
                        }

                        // 2. Tocar en el suelo del mapa: el avatar camina naturalmente a velocidad constante
                        val targetNormX = tapWorldX / currentMapW
                        val targetNormY = tapWorldY / currentMapH
                        startWalkingTo(targetNormX, targetNormY, null)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            dragPanX.snapTo((dragPanX.value + dragAmount.x).coerceIn(-currentMapW * 0.45f, currentMapW * 0.45f))
                            dragPanY.snapTo((dragPanY.value + dragAmount.y).coerceIn(-currentMapH * 0.45f, currentMapH * 0.45f))
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
                        filterQuality = FilterQuality.Low,
                        colorFilter = if (timeOverlayColor != Color.Transparent) ColorFilter.tint(timeOverlayColor, BlendMode.Darken) else null
                    )
                } else {
                    // Fallback visual si el mapa aún carga
                    drawRect(Color(0xFF2E7651), size = Size(mapW, mapH))
                    if (timeOverlayColor != Color.Transparent) {
                        drawRect(timeOverlayColor, size = Size(mapW, mapH), blendMode = BlendMode.Darken)
                    }
                }

                // 2. Pulso / Radar estilo Pokémon GO a los pies del jugador
                drawCircle(
                    color  = AmberAccent.copy(alpha = pulseAlpha),
                    radius = pulseRadius,
                    center = Offset(worldPlayerX, worldPlayerY),
                    style  = Stroke(width = 3f)
                )
                drawCircle(
                    color  = AmberAccent.copy(alpha = 0.15f),
                    radius = 28f,
                    center = Offset(worldPlayerX, worldPlayerY),
                )

                // 2.1 Indicador visual de destino al que camina el avatar
                targetMarkerPos?.let { target ->
                    drawCircle(
                        color = AmberAccent.copy(alpha = 0.75f),
                        radius = 14f + (pulseRadius * 0.12f),
                        center = target,
                        style = Stroke(width = 2.5f)
                    )
                    drawCircle(
                        color = AmberAccent.copy(alpha = 0.4f),
                        radius = 5f,
                        center = target
                    )
                }

                // 3. Marcadores elevados para los puntos de interés
                puntos.forEach { punto ->
                    val pinPos = Offset(punto.posX * mapW, punto.posY * mapH)
                    val esCompletado = punto.id in completados
                    val esObjetivo = punto.id == puntoObjetivo?.id
                    val pinColor = if (esCompletado) Color(0xFF2E7D32) else colorPorCategoria(punto.categoria, primary, secondary, tertiary)
                    val elevatedCenter = pinPos - Offset(0f, 18f)

                    if (esObjetivo) {
                        // Halo de atención animado alrededor del punto objetivo de la misión
                        drawCircle(
                            color = AmberAccent.copy(alpha = pulseAlpha),
                            radius = 24f + (pulseRadius * 0.35f),
                            center = elevatedCenter,
                            style = Stroke(width = 3.5f)
                        )
                        drawCircle(
                            color = AmberAccent.copy(alpha = 0.22f),
                            radius = 24f,
                            center = elevatedCenter
                        )
                    }

                    // Sombra ovalada en el suelo
                    drawOval(
                        color = Color.Black.copy(alpha = 0.35f),
                        topLeft = Offset(pinPos.x - 22f, pinPos.y - 6f),
                        size = Size(44f, 12f)
                    )
                    // Poste del pin
                    drawLine(
                        color = Color(0xFF102D2B),
                        start = pinPos,
                        end = elevatedCenter,
                        strokeWidth = 4f
                    )
                    // Cabeza del pin
                    drawCircle(color = Color(0xFF102D2B), radius = 22f, center = elevatedCenter)
                    drawCircle(color = pinColor, radius = 18f, center = elevatedCenter)
                    drawCircle(color = if (esCompletado) Color.White else if (esObjetivo) AmberAccent else Color.White, radius = 6f, center = elevatedCenter)
                }
            }
        }

        // 4. Avatares animados sobre el punto exacto de la pantalla (Jugador + NPCs ordenados por Y)
        val spriteSizeDp = 52.dp
        val spriteHalfPx = with(density) { (spriteSizeDp / 2).toPx() }

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
                            y = (screenEntityY - spriteHalfPx - with(density) { 8.dp.toPx() }).roundToInt()
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
                    .border(BorderStroke(2.dp, AmberAccent), RoundedCornerShape(20.dp))
                    .clickable {
                        soundManager?.play(SoundEffect.CLICK)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        startWalkingTo(puntoObjetivo.posX, puntoObjetivo.posY) {
                            soundManager?.play(SoundEffect.CLICK)
                            onPuntoClick(puntoObjetivo)
                        }
                    },
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
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
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // 6. Botón flotante para recentrar en el avatar si el usuario arrastró el mapa
        if (isDragged) {
            FloatingActionButton(
                onClick = {
                    soundManager?.play(SoundEffect.CLICK)
                    scope.launch {
                        launch { dragPanX.animateTo(0f, tween(350, easing = FastOutSlowInEasing)) }
                        launch { dragPanY.animateTo(0f, tween(350, easing = FastOutSlowInEasing)) }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(40.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                contentColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MyLocation,
                    contentDescription = "Recentrar en mi personaje",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
