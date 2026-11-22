package com.example.gamequest

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.gamequest.ui.common.CampusBottomBar
import com.example.gamequest.ui.theme.GamequestTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CampusBottomBarTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun compactNavigationKeepsSelectedLabelVisible() {
        compose.setContent {
            val selected = remember { mutableIntStateOf(3) }
            GamequestTheme {
                CampusBottomBar(selectedIndex = selected.intValue, onSelect = { selected.intValue = it })
            }
        }

        // Todos los destinos conservan su nombre para TalkBack.
        compose.onNodeWithContentDescription("Inicio").assertIsDisplayed()
        compose.onNodeWithContentDescription("Misiones").assertIsDisplayed()
        compose.onNodeWithContentDescription("QR").assertIsDisplayed()
        compose.onNodeWithContentDescription("Insignias").assertIsDisplayed()
        compose.onNodeWithContentDescription("Perfil").assertIsDisplayed()

        // Solo el destino activo muestra su rótulo visible.
        compose.onNodeWithText("Insignias").assertIsDisplayed()
        assertTrue(compose.onAllNodesWithText("Inicio").fetchSemanticsNodes().isEmpty())

        // Al cambiar de destino, el rótulo visible acompaña a la selección.
        compose.onNodeWithContentDescription("Inicio").performClick()
        compose.onNodeWithText("Inicio").assertIsDisplayed()
        assertTrue(compose.onAllNodesWithText("Insignias").fetchSemanticsNodes().isEmpty())
        assertTrue(compose.onAllNodesWithText("Misiones").fetchSemanticsNodes().isEmpty())
        assertTrue(compose.onAllNodesWithText("Perfil").fetchSemanticsNodes().isEmpty())
    }
}
