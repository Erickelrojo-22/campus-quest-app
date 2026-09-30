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
import com.example.gamequest.util.AvatarColor
import com.example.gamequest.util.CharacterSpecies
import com.example.gamequest.util.SpriteColorEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Animaciones necesarias disponibles para todas las especies de personajes.
 */
enum class CharacterAnimation(
    val filename: String,
    val frameCount: Int,
    val fps: Int = 8,
) {
    IDLE("idle.png", frameCount = 4, fps = 6),
    WALK("walk.png", frameCount = 6, fps = 8),
    RUN ("run.png",  frameCount = 6, fps = 10),
    JUMP("jump.png", frameCount = 8, fps = 10),
}

// Alias para retrocompatibilidad
typealias DudeAnimation = CharacterAnimation

/**
 * Composable universal para renderizar cualquier especie de personaje (Dude, Pink, Owlet)
 * con animación continua y palette swap dual en tiempo real.
 */
@Composable
fun CharacterSprite(
    species: CharacterSpecies       = CharacterSpecies.DUDE,
    animation: CharacterAnimation   = CharacterAnimation.IDLE,
    primaryColor: AvatarColor       = AvatarColor.COBALT_BLUE,
    secondaryColor: AvatarColor     = AvatarColor.RUBY_RED,
    size: Dp                        = 64.dp,
    loop: Boolean                   = true,
    modifier: Modifier              = Modifier,
) {
    val context = LocalContext.current
    val frameDurationMs = (1000f / animation.fps).toInt()

    var frames by remember(species, animation, primaryColor, secondaryColor) {
        mutableStateOf<List<ImageBitmap>>(emptyList())
    }
    LaunchedEffect(species, animation, primaryColor, secondaryColor) {
        frames = withContext(Dispatchers.Default) {
            (0 until animation.frameCount).map { i ->
                SpriteColorEngine.getFrame(
                    context, species, animation.filename,
                    primaryColor, secondaryColor, i
                )
            }
        }
    }

    if (frames.isEmpty()) return

    val frameIndex: Int = if (loop) {
        val transition = rememberInfiniteTransition(label = "char_anim_${species.name}_${animation.name}")
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
        LaunchedEffect(species, animation) {
            repeat(animation.frameCount) {
                idx = it
                kotlinx.coroutines.delay(frameDurationMs.toLong())
            }
        }
        idx
    }

    Image(
        bitmap             = frames[frameIndex],
        contentDescription = "avatar_${species.name}",
        filterQuality      = FilterQuality.None,
        contentScale       = ContentScale.FillBounds,
        modifier           = modifier.size(size),
    )
}

/** Alias retrocompatible con llamadas existentes de DudeSprite */
@Composable
fun DudeSprite(
    animation: CharacterAnimation   = CharacterAnimation.IDLE,
    primaryColor: AvatarColor       = AvatarColor.COBALT_BLUE,
    secondaryColor: AvatarColor     = AvatarColor.RUBY_RED,
    species: CharacterSpecies       = CharacterSpecies.DUDE,
    size: Dp                        = 64.dp,
    loop: Boolean                   = true,
    modifier: Modifier              = Modifier,
) {
    CharacterSprite(
        species        = species,
        animation      = animation,
        primaryColor   = primaryColor,
        secondaryColor = secondaryColor,
        size           = size,
        loop           = loop,
        modifier       = modifier,
    )
}
