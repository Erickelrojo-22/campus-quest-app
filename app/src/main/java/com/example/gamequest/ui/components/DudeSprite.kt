package com.example.gamequest.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.gamequest.util.SpriteColorEngine
import com.example.gamequest.util.SpriteColorEngine.CharacterColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Animaciones necesarias del Dude Monster.
 * @param assetPath  ruta del sprite sheet en assets/ (horizontal, 32×32 por frame)
 * @param frameCount número de frames en el sheet
 * @param fps        velocidad de reproducción
 */
enum class DudeAnimation(
    val assetPath: String,
    val frameCount: Int,
    val fps: Int = 8,
) {
    IDLE(assetPath = "sprites/dude/idle.png", frameCount = 4, fps = 6),
    WALK(assetPath = "sprites/dude/walk.png", frameCount = 6, fps = 8),
    RUN (assetPath = "sprites/dude/run.png",  frameCount = 6, fps = 10),
    JUMP(assetPath = "sprites/dude/jump.png", frameCount = 8, fps = 10),
}

/**
 * Sprite animado del Dude Monster con palette swap en tiempo real.
 *
 * Uso:
 * ```kotlin
 * DudeSprite(
 *     animation = DudeAnimation.WALK,
 *     color     = CharacterColor.GREEN,
 *     size      = 96.dp,
 * )
 * ```
 *
 * @param animation  Animación a reproducir (default: IDLE)
 * @param color      Color del personaje, persistido desde DataStore
 * @param size       Tamaño en pantalla — se escala pixel-perfect (NEAREST)
 * @param loop       Si la animación hace loop
 * @param modifier   Modifier de Compose
 */
@Composable
fun DudeSprite(
    animation: DudeAnimation = DudeAnimation.IDLE,
    color: CharacterColor    = CharacterColor.BLUE_ORIGINAL,
    size: Dp                 = 64.dp,
    loop: Boolean            = true,
    modifier: Modifier       = Modifier,
) {
    val context = LocalContext.current
    val frameDurationMs = (1000f / animation.fps).toInt()

    // Cargar + procesar el sheet en background (solo cuando cambia animación o color)
    var frames by remember(animation, color) { mutableStateOf<List<ImageBitmap>>(emptyList()) }
    LaunchedEffect(animation, color) {
        frames = withContext(Dispatchers.Default) {
            (0 until animation.frameCount).map { i ->
                SpriteColorEngine.getFrame(context, animation.assetPath, color, i)
            }
        }
    }

    if (frames.isEmpty()) return  // Aún cargando — no renderizar nada

    // Avanzar frames con InfiniteTransition (o Animatable para one-shot)
    val frameIndex: Int = if (loop) {
        val transition = rememberInfiniteTransition(label = "dude_anim_${animation.name}")
        val raw by transition.animateValue(
            initialValue  = 0,
            targetValue   = animation.frameCount,
            typeConverter = Int.VectorConverter,
            animationSpec = infiniteRepeatable(
                animation  = tween(frameDurationMs * animation.frameCount, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "frame",
        )
        raw % animation.frameCount
    } else {
        var idx by remember { mutableIntStateOf(0) }
        LaunchedEffect(Unit) {
            repeat(animation.frameCount) {
                idx = it
                kotlinx.coroutines.delay(frameDurationMs.toLong())
            }
        }
        idx
    }

    Image(
        bitmap             = frames[frameIndex],
        contentDescription = "avatar",
        filterQuality      = FilterQuality.None,   // NEAREST — pixel art nítido
        contentScale       = ContentScale.FillBounds,
        modifier           = modifier.size(size),
    )
}
