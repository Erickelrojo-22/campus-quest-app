package com.example.gamequest.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.AmberAccentDark
import com.example.gamequest.ui.theme.ContainerDark
import com.example.gamequest.ui.theme.InstitutionalRed
import com.example.gamequest.util.LocalSoundManager
import com.example.gamequest.util.SoundEffect

private data class OnboardingPaso(
    val icono: String,
    val titulo: String,
    val subtitulo: String,
    val descripcion: String,
    val etiquetaRpg: String
)

private val pasosOnboarding = listOf(
    OnboardingPaso(
        icono = "🏛️🎮",
        titulo = "¡Bienvenido a Campus Quest!",
        subtitulo = "La ULEAM ahora es tu tablero de aventura",
        descripcion = "Explora las facultades, bibliotecas y plazas. Tu avatar pixel art recorrerá los senderos universitarios mientras descubres cada rincón del campus.",
        etiquetaRpg = "PASO 1 · EL MUNDO RPG"
    ),
    OnboardingPaso(
        icono = "📍🔍",
        titulo = "Misiones y Códigos QR",
        subtitulo = "Sigue pistas y escanea los puntos clave",
        descripcion = "Consulta tus misiones activas con la brújula del mapa. Al llegar al lugar físico indicado, escanea el código QR para validar tu visita.",
        etiquetaRpg = "PASO 2 · RETOS Y EXPLORACIÓN"
    ),
    OnboardingPaso(
        icono = "🏅⭐",
        titulo = "Insignias, Nivel y Ranking",
        subtitulo = "Forja tu leyenda como Aventurero",
        descripcion = "Cada misión te otorga experiencia (pts) y una insignia exclusiva. Sube de rango RPG, comparte tu Carné de Aventurero y compite por el 1er lugar del campus.",
        etiquetaRpg = "PASO 3 · GLORIA Y RECOMPENSAS"
    )
)

@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit,
    onCompletar: () -> Unit
) {
    val soundManager = LocalSoundManager.current
    val haptic = LocalHapticFeedback.current
    var pasoActual by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .clip(RoundedCornerShape(16.dp))
                .border(BorderStroke(3.dp, AmberAccent), RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Indicador RPG superior
                Surface(
                    color = InstitutionalRed,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        pasosOnboarding[pasoActual].etiquetaRpg,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        letterSpacing = 1.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Contenido animado del paso
                AnimatedContent(
                    targetState = pasoActual,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "OnboardingTransition"
                ) { paso ->
                    val datos = pasosOnboarding[paso]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Icono circular retro
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .background(
                                    brush = Brush.radialGradient(listOf(AmberAccent, AmberAccentDark)),
                                    shape = CircleShape
                                )
                                .border(BorderStroke(3.dp, Color.White.copy(alpha = 0.8f)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(datos.icono, fontSize = 42.sp)
                        }

                        Spacer(Modifier.height(14.dp))

                        Text(
                            datos.titulo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            datos.subtitulo,
                            style = MaterialTheme.typography.bodySmall,
                            color = AmberAccent,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(10.dp))

                        Text(
                            datos.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Indicador de pasos (puntos)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pasosOnboarding.indices.forEach { indice ->
                        Box(
                            modifier = Modifier
                                .size(if (indice == pasoActual) 10.dp else 8.dp)
                                .background(
                                    if (indice == pasoActual) AmberAccent else MaterialTheme.colorScheme.outlineVariant,
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Botones de navegación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (pasoActual > 0) {
                        OutlinedButton(
                            onClick = {
                                soundManager?.play(SoundEffect.CLICK)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                pasoActual--
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Atrás")
                        }
                    } else {
                        TextButton(
                            onClick = {
                                soundManager?.play(SoundEffect.CLICK)
                                onDismiss()
                            }
                        ) {
                            Text("Omitir", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Button(
                        onClick = {
                            soundManager?.play(SoundEffect.CLICK)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            if (pasoActual < pasosOnboarding.size - 1) {
                                pasoActual++
                            } else {
                                soundManager?.play(SoundEffect.SCAN_SUCCESS)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCompletar()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberAccent,
                            contentColor = ContainerDark
                        )
                    ) {
                        Text(
                            if (pasoActual < pasosOnboarding.size - 1) "Siguiente" else "¡Comenzar! 🚀",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
