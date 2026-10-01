package com.example.gamequest.ui.profile

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Palette
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
import com.example.gamequest.util.AvatarColor
import com.example.gamequest.util.LocalSoundManager
import com.example.gamequest.util.SoundEffect
import com.example.gamequest.ui.components.CharacterAnimation
import com.example.gamequest.ui.components.CharacterSprite
import com.example.gamequest.ui.navigation.Routes
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.InstitutionalRed
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.History
import com.example.gamequest.data.repository.CampusRepository
import com.example.gamequest.data.repository.ProgresoDetallado
import com.example.gamequest.util.CharacterSpecies
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    sessionViewModel: SessionViewModel,
    campusRepository: CampusRepository? = null,
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
    val primaryColor    = prefs?.avatarPrimaryColor ?: AvatarColor.COBALT_BLUE
    val secondaryColor  = prefs?.avatarSecondaryColor ?: AvatarColor.RUBY_RED

    // Alternar IDLE/WALK al tocar el sprite
    var spriteAnim by remember { mutableStateOf(CharacterAnimation.IDLE) }

    // Controlar visibilidad del editor de avatar
    var showEditor by remember { mutableStateOf(false) }

    // Controlar visibilidad del historial / timeline de puntos
    var showTimeline by remember { mutableStateOf(false) }

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
                        species        = selectedSpecies,
                        animation      = spriteAnim,
                        primaryColor   = primaryColor,
                        secondaryColor = secondaryColor,
                        size           = 72.dp,
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

            Spacer(Modifier.height(4.dp))

            // ── Opciones de perfil ───────────────────────────────────────

            // Botón para abrir el editor de avatar
            OpcionPerfil(
                icono     = Icons.Filled.Palette,
                titulo    = "Personalizar Avatar",
                subtitulo = "Especie, colores y estilo",
                onClick   = {
                    soundManager?.play(SoundEffect.CLICK)
                    showEditor = true
                },
            )

            // RF-15 / Timeline: Botón para ver el historial y actividades completadas
            OpcionPerfil(
                icono     = Icons.Filled.History,
                titulo    = "Historial de Puntos",
                subtitulo = "Línea de tiempo de misiones completadas",
                onClick   = {
                    soundManager?.play(SoundEffect.CLICK)
                    showTimeline = true
                },
            )

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

    // ── Modal de historial y timeline de puntos ──────────────────────────
    if (showTimeline) {
        val historial by remember(usuario?.id, campusRepository) {
            campusRepository?.observarHistorialProgreso(usuario?.id ?: -1)
                ?: kotlinx.coroutines.flow.flowOf(emptyList())
        }.collectAsState(initial = emptyList())

        ModalBottomSheet(
            onDismissRequest = { showTimeline = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Historial de Puntos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Misiones completadas y recompensas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = AmberAccent.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "⭐ ${usuario?.puntajeAcumulado ?: 0} pts",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = AmberAccent,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (historial.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗺️", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Aún no has completado misiones.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "¡Explora el campus y escanea códigos QR para sumar puntos!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    val dateFormat = remember {
                        java.text.SimpleDateFormat("dd/MM/yyyy · HH:mm", java.util.Locale.getDefault())
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(historial) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            item.mision?.insigniaEmoji ?: "⭐",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            item.mision?.titulo ?: "Misión completada",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            item.punto?.nombre ?: "Punto del campus",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            dateFormat.format(java.util.Date(item.progreso.fechaHora)),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    Surface(
                                        color = AmberAccent.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "+${item.progreso.puntosObtenidos} pts",
                                            color = AmberAccent,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Modal de edición de avatar ───────────────────────────────────────
    if (showEditor) {
        AvatarEditorSheet(
            currentSpecies   = selectedSpecies,
            currentPrimary   = primaryColor,
            currentSecondary = secondaryColor,
            onSave = { newSpecies, newPrimary, newSecondary ->
                scope.launch {
                    prefsRepo.setCharacterSpecies(newSpecies)
                    prefsRepo.setAvatarPrimaryColor(newPrimary)
                    prefsRepo.setAvatarSecondaryColor(newSecondary)
                }
                showEditor = false
            },
            onDismiss = { showEditor = false },
        )
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
