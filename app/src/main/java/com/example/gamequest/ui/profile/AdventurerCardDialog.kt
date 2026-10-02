package com.example.gamequest.ui.profile

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.ui.components.CharacterAnimation
import com.example.gamequest.ui.components.CharacterSprite
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.ContainerDark
import com.example.gamequest.ui.theme.InstitutionalRed
import com.example.gamequest.util.AvatarColor
import com.example.gamequest.util.CharacterSpecies
import com.example.gamequest.util.LocalSoundManager
import com.example.gamequest.util.SoundEffect

private fun obtenerRangoRpg(nivel: Int): String = when {
    nivel >= 7 -> "🏆 Maestro del Campus"
    nivel >= 4 -> "⚔️ Aventurero Veterano"
    nivel >= 2 -> "🧭 Explorador de Campus"
    else       -> "🌱 Explorador Novato"
}

private fun compartirCarne(context: Context, usuario: UsuarioEntity, rango: String, totalInsignias: Int) {
    val idFormateado = "CQ-EST-%04d".format(usuario.id)
    val mensaje = """
        🎮 ¡Mira mi Carné de Aventurero en Campus Quest! 🏛️
        
        👤 Estudiante: ${usuario.nombres}
        🎓 Carrera: ${usuario.carrera.ifBlank { "Estudiante ULEAM" }}
        🏷️ Credencial: $idFormateado
        ⭐ Nivel: ${usuario.nivel} ($rango)
        🏆 Puntos: ${usuario.puntajeAcumulado} pts
        🏅 Insignias: $totalInsignias
        
        Explorando y gamificando la ULEAM con Campus Quest. 🚀
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Carné de Aventurero - ${usuario.nombres}")
        putExtra(Intent.EXTRA_TEXT, mensaje)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir carné de aventurero"))
}

@Composable
fun AdventurerCardDialog(
    usuario: UsuarioEntity?,
    species: CharacterSpecies,
    primaryColor: AvatarColor,
    secondaryColor: AvatarColor,
    totalInsignias: Int,
    onDismiss: () -> Unit
) {
    if (usuario == null) return
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val soundManager = LocalSoundManager.current
    val haptic = LocalHapticFeedback.current
    val rango = obtenerRangoRpg(usuario.nivel)
    val carnetId = "CQ-EST-%04d".format(usuario.id)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .border(BorderStroke(3.dp, AmberAccent), RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ── Encabezado Institucional ─────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InstitutionalRed)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "ULEAM · CAMPUS QUEST",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        "CARNÉ DE AVENTURERO",
                        color = AmberAccent,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // ── Cuerpo de la Credencial ─────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar Pixel Art
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .border(BorderStroke(2.dp, AmberAccent.copy(alpha = 0.5f)), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            CharacterSprite(
                                species = species,
                                animation = CharacterAnimation.IDLE,
                                primaryColor = primaryColor,
                                secondaryColor = secondaryColor,
                                size = 60.dp
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        // Información principal
                        Column(Modifier.weight(1f)) {
                            Text(
                                usuario.nombres,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (usuario.carrera.isNotBlank()) {
                                Text(
                                    usuario.carrera,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (usuario.correoInstitucional.isNotBlank()) {
                                Text(
                                    usuario.correoInstitucional,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.clickable {
                                    soundManager?.play(SoundEffect.CLICK)
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    clipboardManager.setText(AnnotatedString(carnetId))
                                    Toast.makeText(context, "ID copiado: $carnetId", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        carnetId,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text("📋", fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))

                    // Rango RPG
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ContainerDark, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "RANGO:",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            rango,
                            color = AmberAccent,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    // Estadísticas de Aventurero
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DatoItem(
                            etiqueta = "NIVEL",
                            valor = "⭐ ${usuario.nivel}",
                            modifier = Modifier.weight(1f)
                        )
                        DatoItem(
                            etiqueta = "PUNTOS",
                            valor = "🏆 ${usuario.puntajeAcumulado}",
                            modifier = Modifier.weight(1f)
                        )
                        DatoItem(
                            etiqueta = "INSIGNIAS",
                            valor = "🏅 $totalInsignias",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // ── Botones de Acción ───────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                soundManager?.play(SoundEffect.CLICK)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Cerrar")
                        }

                        Button(
                            onClick = {
                                soundManager?.play(SoundEffect.CLICK)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                compartirCarne(context, usuario, rango, totalInsignias)
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberAccent,
                                contentColor = ContainerDark
                            )
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Compartir", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatoItem(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(2.dp))
            Text(
                valor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
