package com.example.gamequest.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.data.preferences.UserPreferencesRepository
import com.example.gamequest.ui.common.CampusBottomBar
import com.example.gamequest.ui.common.SessionViewModel
import com.example.gamequest.util.LocalSoundManager
import com.example.gamequest.util.SoundEffect
import com.example.gamequest.ui.components.CharacterAnimation
import com.example.gamequest.ui.components.CharacterSprite
import com.example.gamequest.ui.navigation.Routes
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.InstitutionalRed
import com.example.gamequest.util.CharacterSpecies
import com.example.gamequest.util.SpriteColorEngine.CharacterColor
import kotlinx.coroutines.launch

/** Convierte el nombre del color a un Color de Compose para la UI del selector. */
private fun CharacterColor.toComposeColor(): Color = when (this) {
    CharacterColor.BLUE_ORIGINAL -> Color(0xFF0696DB)
    CharacterColor.GREEN         -> Color(0xFF23B437)
    CharacterColor.RED           -> Color(0xFFBE2323)
    CharacterColor.PURPLE        -> Color(0xFF7828C8)
    CharacterColor.ORANGE        -> Color(0xFFD76414)
    CharacterColor.PINK          -> Color(0xFFE650A0)
    CharacterColor.YELLOW        -> Color(0xFFD2B40F)
    CharacterColor.GREY          -> Color(0xFF78788C)
}

@Composable
fun ProfileScreen(
    sessionViewModel: SessionViewModel,
    onNavigateTab: (String) -> Unit,
    onSettings: () -> Unit,
    onMissionManagement: () -> Unit,
    onCerrarSesion: () -> Unit,
) {
    val usuario         by sessionViewModel.usuarioActual.collectAsState()
    val soundManager     = LocalSoundManager.current
    val context          = LocalContext.current
    val scope            = rememberCoroutineScope()
    val prefsRepo        = remember { UserPreferencesRepository(context) }
    val prefs           by prefsRepo.preferencias.collectAsState(initial = null)

    val selectedSpecies = prefs?.characterSpecies ?: CharacterSpecies.DUDE
    val selectedColor   = prefs?.characterColor ?: CharacterColor.BLUE_ORIGINAL
    // Alternar IDLE/WALK al tocar el sprite
    var spriteAnim by remember { mutableStateOf(CharacterAnimation.IDLE) }

    Scaffold(
        bottomBar = { CampusBottomBar(currentRoute = Routes.PROFILE, onNavigate = onNavigateTab) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Header con sprite ────────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Sprite animado — tap para animar Walk
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable {
                            soundManager?.play(SoundEffect.CLICK)
                            spriteAnim = if (spriteAnim == CharacterAnimation.IDLE)
                                CharacterAnimation.WALK else CharacterAnimation.IDLE
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    CharacterSprite(
                        species   = selectedSpecies,
                        animation = spriteAnim,
                        color     = selectedColor,
                        size      = 72.dp,
                    )
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    usuario?.nombres ?: "",
                    color      = Color.White,
                    fontWeight = FontWeight.Bold,
                    style      = MaterialTheme.typography.titleLarge,
                )
                if (!usuario?.correoInstitucional.isNullOrBlank()) {
                    Text(usuario?.correoInstitucional ?: "", color = AmberAccent, style = MaterialTheme.typography.bodySmall)
                }
                if (!usuario?.carrera.isNullOrBlank()) {
                    Text(usuario?.carrera ?: "", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                }
                if (usuario?.rol == Rol.TUTOR) {
                    Text("Tutor del programa de bienvenida", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "⭐ Nivel ${usuario?.nivel ?: 1}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    Surface(color = AmberAccent.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "🏆 ${usuario?.puntajeAcumulado ?: 0} pts",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = AmberAccent,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }

            // ── Personalización de personaje (Especie y Color) ──────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Personaje",
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        "Elige tu héroe y personaliza su color",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))

                    // Selector de especie (Dude, Pink, Owlet)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CharacterSpecies.values().forEach { species ->
                            val isSelected = species == selectedSpecies
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) AmberAccent else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        soundManager?.play(SoundEffect.CLICK)
                                        scope.launch { prefsRepo.setCharacterSpecies(species) }
                                    },
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    CharacterSprite(
                                        species   = species,
                                        animation = CharacterAnimation.IDLE,
                                        color     = selectedColor,
                                        size      = 38.dp,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text       = species.label,
                                        style      = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color      = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Color",
                        fontWeight = FontWeight.Bold,
                        style      = MaterialTheme.typography.labelLarge,
                    )
                    Spacer(Modifier.height(10.dp))

                    // Grid 4×2 de colores
                    val colors = CharacterColor.values()
                    for (row in 0 until 2) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            for (col in 0 until 4) {
                                val idx = row * 4 + col
                                if (idx >= colors.size) break
                                val c = colors[idx]
                                ColorDot(
                                    color     = c.toComposeColor(),
                                    label     = c.label,
                                    selected  = c == selectedColor,
                                    onClick   = {
                                        soundManager?.play(SoundEffect.CLICK)
                                        scope.launch { prefsRepo.setCharacterColor(c) }
                                    },
                                )
                            }
                        }
                        if (row == 0) Spacer(Modifier.height(8.dp))
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // ── Opciones de perfil ───────────────────────────────────────
            OpcionPerfil(Icons.Filled.Settings, "Configuración", "Tema, notificaciones, idioma", onSettings)

            if (usuario?.rol == Rol.TUTOR) {
                OpcionPerfil(
                    Icons.Filled.AdminPanelSettings,
                    "Gestión de misiones",
                    "Crear, editar y eliminar misiones (modo tutor)",
                    onMissionManagement,
                )
            }

            OpcionPerfil(
                Icons.AutoMirrored.Filled.Logout,
                "Cerrar sesión",
                "Tu progreso se mantiene guardado",
                onCerrarSesion,
                colorTexto = InstitutionalRed,
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ColorDot(
    color: Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (selected) Modifier.border(3.dp, Color.White, CircleShape)
                    else Modifier.border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                )
                .clickable(onClick = onClick),
        )
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun OpcionPerfil(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    colorTexto: Color = Color.Unspecified,
) {
    Card(
        onClick    = onClick,
        modifier   = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Row(
            modifier          = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icono, contentDescription = null, tint = if (colorTexto != Color.Unspecified) colorTexto else MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold, color = if (colorTexto != Color.Unspecified) colorTexto else MaterialTheme.colorScheme.onSurface)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
