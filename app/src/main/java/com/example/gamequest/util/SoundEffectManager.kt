package com.example.gamequest.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.gamequest.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

enum class SoundEffect {
    CLICK,
    SCAN_SUCCESS,
    BADGE_EARNED,
    ERROR
}

val LocalSoundManager = staticCompositionLocalOf<SoundEffectManager?> { null }

class SoundEffectManager(
    context: Context,
    preferencesRepository: UserPreferencesRepository,
    scope: CoroutineScope
) {
    private val preferencias = preferencesRepository.preferencias
        .stateIn(scope, SharingStarted.Eagerly, null)

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(audioAttributes)
        .build()

    private val soundMap = mutableMapOf<SoundEffect, Int>()

    init {
        loadAssetSound(context, SoundEffect.CLICK, "audio/sfx/click.ogg")
        loadAssetSound(context, SoundEffect.SCAN_SUCCESS, "audio/sfx/scan_success.ogg")
        loadAssetSound(context, SoundEffect.BADGE_EARNED, "audio/sfx/badge_earned.ogg")
        loadAssetSound(context, SoundEffect.ERROR, "audio/sfx/error.ogg")
    }

    private fun loadAssetSound(context: Context, effect: SoundEffect, assetPath: String) {
        runCatching {
            val afd = context.assets.openFd(assetPath)
            soundMap[effect] = soundPool.load(afd, 1)
        }
    }

    fun play(effect: SoundEffect) {
        val soundId = soundMap[effect] ?: return
        val sonidoActivado = preferencias.value?.sonidoVibracion ?: true
        if (sonidoActivado) {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}
