package com.example.gamequest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import com.example.gamequest.data.preferences.UserPreferences
import com.example.gamequest.ui.navigation.CampusQuestNavHost
import com.example.gamequest.ui.theme.GamequestTheme
import com.example.gamequest.util.LocalSoundManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as CampusQuestApp).container
        setContent {
            val prefs by container.preferencesRepository.preferencias
                .collectAsState(initial = UserPreferences())
            CompositionLocalProvider(LocalSoundManager provides container.soundEffectManager) {
                GamequestTheme(darkTheme = prefs.temaOscuro) {
                    CampusQuestNavHost(container = container)
                }
            }
        }
    }
}
