package com.example.gamequest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import com.example.gamequest.data.preferences.UserPreferences
import com.example.gamequest.ui.common.ProveedorPerfilVentanaCampus
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
                .collectAsStateWithLifecycle(initialValue = UserPreferences())
            CompositionLocalProvider(LocalSoundManager provides container.soundEffectManager) {
                GamequestTheme(darkTheme = prefs.temaOscuro) {
                    ProveedorPerfilVentanaCampus {
                        CampusQuestNavHost(container = container)
                    }
                }
            }
        }
    }
}
