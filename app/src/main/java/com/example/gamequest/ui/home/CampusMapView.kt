package com.example.gamequest.ui.home

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
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
    primaryColor: AvatarColor = AvatarColor.COBALT_BLUE,
    secondaryColor: AvatarColor = AvatarColor.RUBY_RED,
    characterSpecies: CharacterSpecies = CharacterSpecies.DUDE,
    mapAssetPath: String = "maps/campus_map_uleam.png",
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

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
                    val duration = (dist * 3500).roundToInt().coerceIn(800, 3000)

                    npc.flipX = newX < npc.x.value
                    npc.isWalking = true
                    launch { npc.x.animateTo(newX, tween(duration, easing = LinearOutSlowInEasing)) }
                    npc.y.animateTo(newY, tween(duration, easing = LinearOutSlowInEasing))
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
    var dragPanX by remember { mutableFloatStateOf(0f) }
    var dragPanY by remember { mutableFloatStateOf(0f) }
    val isDragged = remember(dragPanX, dragPanY) { dragPanX != 0f || dragPanY != 0f }

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
        val rawCamX = (viewW / 2f) - worldPlayerX + dragPanX
        val rawCamY = (viewH / 2f) - worldPlayerY + dragPanY

        // Restringir la cámara para que no muestre vacío fuera del mapa
        val camX = rawCamX.coerceIn(viewW - mapW, 0f)
        val camY = rawCamY.coerceIn(viewH - mapH, 0f)

        // Posición real del avatar en pantalla (píxeles de pantalla)
        val screenPlayerX = worldPlayerX + camX
        val screenPlayerY = worldPlayerY + camY

        // Canvas: Dibuja mapa, caminos, radar y pines
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    // Arrastrar con el dedo para explorar el mapa
                    detectDragGestures { _, dragAmount ->
                        dragPanX += dragAmount.x
                        dragPanY += dragAmount.y
                    }
                }
                .pointerInput(puntos, camX, camY) {
                    // Tocar para caminar o interactuar
                    detectTapGestures { tapOffset ->
                        val tapWorldX = tapOffset.x - camX
                        val tapWorldY = tapOffset.y - camY
                        
                        soundManager?.play(SoundEffect.CLICK)

                        // 1. Verificar si tocó cerca de un Punto de Interés
                        val radioPin = 60f
                        val clickedPunto = puntos.minByOrNull { punto ->
                            val px = punto.posX * mapW
                            val py = punto.posY * mapH
                            hypot((px - tapWorldX).toDouble(), (py - tapWorldY).toDouble())
                        }

                        if (clickedPunto != null) {
                            val px = clickedPunto.posX * mapW
                            val py = clickedPunto.posY * mapH
                            val dist = hypot((px - tapWorldX).toDouble(), (py - tapWorldY).toDouble())
                            if (dist <= radioPin) {
                                // Caminar hacia el punto y abrir misión
                                scope.launch {
                                    playerFlipX = clickedPunto.posX < curX
                                    isWalking = true
                                    dragPanX = 0f; dragPanY = 0f
                                    launch { playerX.animateTo(clickedPunto.posX, tween(1100, easing = LinearOutSlowInEasing)) }
                                    playerY.animateTo(clickedPunto.posY, tween(1100, easing = LinearOutSlowInEasing))
                                    isWalking = false
                                    onPuntoClick(clickedPunto)
                                }
                                return@detectTapGestures
                            }
                        }

                        // 2. Tocar en el suelo del mapa: el avatar camina hacia allá
                        val targetNormX = (tapWorldX / mapW).coerceIn(0.05f, 0.95f)
                        val targetNormY = (tapWorldY / mapH).coerceIn(0.05f, 0.95f)

                        val distNorm = hypot((targetNormX - curX).toDouble(), (targetNormY - curY).toDouble()).toFloat()
                        val duration = (distNorm * 3500).roundToInt().coerceIn(600, 2200)

                        scope.launch {
                            playerFlipX = targetNormX < curX
                            isWalking = true
                            dragPanX = 0f; dragPanY = 0f
                            launch { playerX.animateTo(targetNormX, tween(duration, easing = LinearOutSlowInEasing)) }
                            playerY.animateTo(targetNormY, tween(duration, easing = LinearOutSlowInEasing))
                            isWalking = false
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

                // 3. Marcadores elevados para los puntos de interés
                puntos.forEach { punto ->
                    val pinPos = Offset(punto.posX * mapW, punto.posY * mapH)
                    val esCompletado = punto.id in completados
                    val pinColor = if (esCompletado) Color(0xFF2E7D32) else colorPorCategoria(punto.categoria, primary, secondary, tertiary)
                    val elevatedCenter = pinPos - Offset(0f, 18f)

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
                    drawCircle(color = if (esCompletado) Color.White else AmberAccent, radius = 6f, center = elevatedCenter)
                }
            }
        }

        // 4. Avatares animados sobre el punto exacto de la pantalla (Jugador + NPCs ordenados por Y)
        val spriteSizeDp = 52.dp
        val spriteHalfPx = with(density) { (spriteSizeDp / 2).toPx() }

        val renderables = remember(curX, curY, isWalking, playerFlipX, characterSpecies, primaryColor, secondaryColor, npcs) {
            val list = mutableListOf<RenderableEntity>()
            list.add(
                RenderableEntity(
                    isPlayer = true, id = 0, x = curX, y = curY, isWalking = isWalking,
                    species = characterSpecies, primary = primaryColor, secondary = secondaryColor, flipX = playerFlipX
                )
            )
            npcs.forEach { npc ->
                list.add(
                    RenderableEntity(
                        isPlayer = false, id = npc.id, x = npc.x.value, y = npc.y.value, isWalking = npc.isWalking,
                        species = npc.species, primary = npc.primaryColor, secondary = npc.secondaryColor, flipX = npc.flipX
                    )
                )
            }
            list.sortedBy { it.y } // Ordenar por Y para simular profundidad
        }

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

        // 5. Botón flotante para recentrar en el avatar si el usuario arrastró el mapa
        if (isDragged) {
            FloatingActionButton(
                onClick = {
                    dragPanX = 0f
                    dragPanY = 0f
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
