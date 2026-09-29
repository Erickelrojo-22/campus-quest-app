package com.example.gamequest.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.gamequest.R
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
        soundMap[SoundEffect.CLICK] = soundPool.load(context, R.raw.sfx_click, 1)
        soundMap[SoundEffect.SCAN_SUCCESS] = soundPool.load(context, R.raw.sfx_scan_success, 1)
        soundMap[SoundEffect.BADGE_EARNED] = soundPool.load(context, R.raw.sfx_badge_earned, 1)
        soundMap[SoundEffect.ERROR] = soundPool.load(context, R.raw.sfx_error, 1)
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
