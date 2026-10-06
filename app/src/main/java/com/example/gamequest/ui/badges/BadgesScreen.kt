package com.example.gamequest.ui.badges

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.data.repository.MisionConEstado
import com.example.gamequest.ui.common.ContenidoAdaptable
import com.example.gamequest.ui.common.encabezado
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.AmberAccentDark
import com.example.gamequest.ui.theme.ContainerDark
import com.example.gamequest.ui.theme.InstitutionalRed
import com.example.gamequest.util.LocalSoundManager
import com.example.gamequest.util.SoundEffect

@Composable
fun BadgesScreen(
    viewModel: BadgesViewModel,
    usuarioActualId: Int
) {
    val soundManager = LocalSoundManager.current
    val haptic = LocalHapticFeedback.current
    val estado by viewModel.estado.collectAsState()

    ContenidoAdaptable(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color.White.copy(alpha = 0.15f), shape = CircleShape) {
                            Icon(
                                Icons.Filled.MilitaryTech,
                                contentDescription = null,
                                tint = AmberAccent,
                                modifier = Modifier.padding(10.dp).size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                estado.usuario?.nombres ?: "",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Nivel ${estado.usuario?.nivel ?: 1} · ${estado.usuario?.puntajeAcumulado ?: 0} pts",
                                color = Color.White.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    val total = (estado.obtenidas.size + estado.pendientes.size).coerceAtLeast(1)
                    LinearProgressIndicator(
                        progress = { estado.obtenidas.size / total.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = AmberAccent,
                        trackColor = Color.White.copy(alpha = 0.25f)
                    )
                }
            }

            item {
                SeccionTitulo("OBTENIDAS (${estado.obtenidas.size}) · Toca para inspeccionar")
            }
            item {
                InsigniasGrid(
                    misiones = estado.obtenidas,
                    bloqueadas = false,
                    onInsigniaClick = { item ->
                        soundManager?.play(SoundEffect.BADGE_EARNED)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.seleccionarInsignia(item)
                    }
                )
            }

            item {
                SeccionTitulo("POR DESBLOQUEAR (${estado.pendientes.size}) · Pistas secretas")
            }
            item {
                InsigniasGrid(
                    misiones = estado.pendientes,
                    bloqueadas = true,
                    onInsigniaClick = { item ->
                        soundManager?.play(SoundEffect.CLICK)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.seleccionarInsignia(item)
                    }
                )
            }

            item {
                SeccionTitulo("RANKING DEL CAMPUS")
            }
            itemsIndexed(estado.ranking) { indice, usuario ->
                RankingRow(usuario, esUsuarioActual = usuario.id == usuarioActualId, posicion = indice + 1)
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    // Modal de Vitrina de Insignia
    estado.insigniaSeleccionada?.let { seleccionada ->
        InsigniaDetalleDialog(
            item = seleccionada,
            onDismiss = {
                soundManager?.play(SoundEffect.CLICK)
                viewModel.cerrarDetalleInsignia()
            }
        )
    }
}

@Composable
private fun SeccionTitulo(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp).encabezado()
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InsigniasGrid(
    misiones: List<MisionConEstado>,
    bloqueadas: Boolean,
    onInsigniaClick: (MisionConEstado) -> Unit
) {
    if (misiones.isEmpty()) {
        Text(
            if (bloqueadas) "¡Ya desbloqueaste todas las insignias disponibles!" else "Completa tu primera misión para ganar una insignia.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        return
    }
    // FlowRow en vez de filas fijas de 4: se adapta a pantallas angostas, horizontales y tablets.
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        misiones.forEach { item ->
            val descripcion = if (bloqueadas) {
                "Insignia bloqueada. Toca para ver la pista."
            } else {
                "Insignia ${item.mision.insigniaNombre}, obtenida. Toca para ver el detalle."
            }
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button) { onInsigniaClick(item) }
                    .semantics { contentDescription = descripcion },
                shape = CircleShape,
                color = if (bloqueadas) MaterialTheme.colorScheme.surfaceVariant else AmberAccent.copy(alpha = 0.25f),
                border = if (!bloqueadas) BorderStroke(2.dp, AmberAccent) else null
            ) {
                // El contenido visual (emoji / candado) es decorativo: la descripción está arriba.
                Box(Modifier.fillMaxSize().clearAndSetSemantics { }, contentAlignment = Alignment.Center) {
                    if (bloqueadas) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text(item.mision.insigniaEmoji, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun InsigniaDetalleDialog(
    item: MisionConEstado,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val soundManager = LocalSoundManager.current
    val haptic = LocalHapticFeedback.current
    val esDesbloqueada = item.completada

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(BorderStroke(3.dp, if (esDesbloqueada) AmberAccent else MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Insignia en grande con resplandor
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            brush = if (esDesbloqueada) {
                                Brush.radialGradient(listOf(AmberAccent, AmberAccentDark))
                            } else {
                                Brush.radialGradient(listOf(Color.Gray.copy(alpha = 0.4f), Color.DarkGray.copy(alpha = 0.6f)))
                            },
                            shape = CircleShape
                        )
                        .border(
                            BorderStroke(3.dp, if (esDesbloqueada) Color.White.copy(alpha = 0.8f) else Color.Transparent),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (esDesbloqueada) {
                        Text(item.mision.insigniaEmoji, fontSize = 48.sp)
                    } else {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Estado de desbloqueo
                Surface(
                    color = if (esDesbloqueada) AmberAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        if (esDesbloqueada) "🏅 INSIGNIA DESBLOQUEADA" else "🔒 INSIGNIA BLOQUEADA",
                        color = if (esDesbloqueada) AmberAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Nombre de la insignia
                Text(
                    item.mision.insigniaNombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                // Misión y lugar asociado
                Text(
                    "Misión: ${item.mision.titulo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                Text(
                    "📍 ${item.punto.nombre} (${item.punto.categoria})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(12.dp))

                // Puntos otorgados
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "⭐ Recompensa: ${item.mision.puntos} puntos de experiencia",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Pista / Descripción
                Text(
                    if (esDesbloqueada) item.mision.descripcionPista else "🔍 Pista para hallarla:\n${item.mision.descripcionPista}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(18.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Cerrar")
                    }

                    if (esDesbloqueada) {
                        Button(
                            onClick = {
                                soundManager?.play(SoundEffect.CLICK)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val mensaje = "🏆 ¡He desbloqueado la insignia \"${item.mision.insigniaNombre}\" ${item.mision.insigniaEmoji} en Campus Quest ULEAM! (+${item.mision.puntos} pts) 🏛️🎮"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Logro Campus Quest")
                                    putExtra(Intent.EXTRA_TEXT, mensaje)
                                }
                                context.startActivity(Intent.createChooser(intent, "Compartir insignia"))
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
private fun RankingRow(usuario: UsuarioEntity, esUsuarioActual: Boolean, posicion: Int) {
    val descripcion = "Puesto $posicion, ${usuario.nombres}${if (esUsuarioActual) ", eres tú" else ""}, " +
        "${usuario.puntajeAcumulado} puntos"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            // Una sola frase por fila en vez de "medalla", nombre y número por separado.
            .clearAndSetSemantics { contentDescription = descripcion },
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (esUsuarioActual) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                when (posicion) { 1 -> "🥇"; 2 -> "🥈"; 3 -> "🥉"; else -> "$posicion" },
                modifier = Modifier.width(32.dp)
            )
            Text(
                usuario.nombres + if (esUsuarioActual) " (tú)" else "",
                modifier = Modifier.weight(1f),
                fontWeight = if (esUsuarioActual) FontWeight.Bold else FontWeight.Normal
            )
            // onSurface: el color primario sobre la superficie oscura daba ~1.5:1.
            Text("${usuario.puntajeAcumulado}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
