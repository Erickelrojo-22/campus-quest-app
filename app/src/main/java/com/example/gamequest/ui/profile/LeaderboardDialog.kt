package com.example.gamequest.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.ui.common.encabezado
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.AmberAccentDark
import com.example.gamequest.ui.theme.ContainerDark
import com.example.gamequest.ui.theme.PixelCream
import com.example.gamequest.ui.theme.PixelInkOnCream
import com.example.gamequest.ui.theme.TealPrimaryDark
import com.example.gamequest.util.LocalSoundManager
import com.example.gamequest.util.SoundEffect

@Composable
fun LeaderboardDialog(
    ranking: List<UsuarioEntity>,
    usuarioActualId: Int,
    onDismiss: () -> Unit
) {
    val soundManager = LocalSoundManager.current

    Dialog(
        onDismissRequest = {
            soundManager?.play(SoundEffect.CLICK)
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .widthIn(max = 460.dp),
            shape = RoundedCornerShape(12.dp),
            color = TealPrimaryDark,
            border = BorderStroke(2.5.dp, AmberAccent),
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header del ranking
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(AmberAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = PixelInkOnCream,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                "TABLA DE CLASIFICACIÓN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AmberAccent,
                                modifier = Modifier.encabezado()
                            )
                            Text(
                                "Top aventureros del Campus ULEAM",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }

                    IconButton(onClick = {
                        soundManager?.play(SoundEffect.CLICK)
                        onDismiss()
                    }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Cerrar ranking",
                            tint = Color.White
                        )
                    }
                }

                HorizontalDivider(color = AmberAccent.copy(alpha = 0.5f), thickness = 1.dp)

                if (ranking.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Aún no hay puntuaciones registradas en el campus.",
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    // Podio superior para el Top 3 (si hay al menos 1)
                    val top3 = ranking.take(3)
                    val indexActual = ranking.indexOfFirst { it.id == usuarioActualId }
                    val usuarioActual = if (indexActual >= 0) ranking[indexActual] else null

                    Column(modifier = Modifier.fillMaxSize()) {
                        PodioTop3(top3 = top3, usuarioActualId = usuarioActualId)

                        // Resumen de la posición del usuario actual
                        if (usuarioActual != null) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = AmberAccent.copy(alpha = 0.2f),
                                border = BorderStroke(1.5.dp, AmberAccent)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            "Tu Posición: #${indexActual + 1}",
                                            fontWeight = FontWeight.Bold,
                                            color = AmberAccent,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                        Text(
                                            "· Nivel ${usuarioActual.nivel}",
                                            color = Color.White.copy(alpha = 0.9f),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                    Text(
                                        "⭐ ${usuarioActual.puntajeAcumulado} pts",
                                        fontWeight = FontWeight.Bold,
                                        color = AmberAccent,
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }

                        // Lista completa de clasificación
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(ranking, key = { _, user -> user.id }) { index, user ->
                                FilaRankingItem(
                                    posicion = index + 1,
                                    usuario = user,
                                    esUsuarioActual = user.id == usuarioActualId
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PodioTop3(top3: List<UsuarioEntity>, usuarioActualId: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // 2do lugar (Plata)
        if (top3.size >= 2) {
            TarjetaPodio(
                posicion = 2,
                emoji = "🥈",
                usuario = top3[1],
                colorMedalla = Color(0xFFC0C0C0),
                altura = 105.dp,
                esUsuarioActual = top3[1].id == usuarioActualId,
                modifier = Modifier.weight(1f)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 1er lugar (Oro - Centro más alto)
        if (top3.isNotEmpty()) {
            TarjetaPodio(
                posicion = 1,
                emoji = "👑 🥇",
                usuario = top3[0],
                colorMedalla = AmberAccent,
                altura = 125.dp,
                esUsuarioActual = top3[0].id == usuarioActualId,
                modifier = Modifier.weight(1.15f)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 3er lugar (Bronce)
        if (top3.size >= 3) {
            TarjetaPodio(
                posicion = 3,
                emoji = "🥉",
                usuario = top3[2],
                colorMedalla = Color(0xFFCD7F32),
                altura = 95.dp,
                esUsuarioActual = top3[2].id == usuarioActualId,
                modifier = Modifier.weight(1f)
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TarjetaPodio(
    posicion: Int,
    emoji: String,
    usuario: UsuarioEntity,
    colorMedalla: Color,
    altura: androidx.compose.ui.unit.Dp,
    esUsuarioActual: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(altura),
        shape = RoundedCornerShape(8.dp),
        color = if (esUsuarioActual) AmberAccent.copy(alpha = 0.28f) else Color.Black.copy(alpha = 0.35f),
        border = BorderStroke(
            if (posicion == 1 || esUsuarioActual) 2.dp else 1.dp,
            if (posicion == 1) AmberAccent else colorMedalla.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(emoji, fontSize = if (posicion == 1) 20.sp else 16.sp)
            Text(
                usuario.nombres.substringBefore(" "),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Surface(
                color = colorMedalla.copy(alpha = 0.25f),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(0.5.dp, colorMedalla)
            ) {
                Text(
                    "${usuario.puntajeAcumulado} pts",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorMedalla,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun FilaRankingItem(
    posicion: Int,
    usuario: UsuarioEntity,
    esUsuarioActual: Boolean
) {
    val fondo = if (esUsuarioActual) AmberAccent.copy(alpha = 0.22f) else Color.Black.copy(alpha = 0.25f)
    val borde = if (esUsuarioActual) BorderStroke(1.5.dp, AmberAccent) else BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        color = fondo,
        border = borde
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posición con medalla para top 3 o número
            Box(
                modifier = Modifier.width(32.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                when (posicion) {
                    1 -> Text("🥇", fontSize = 16.sp)
                    2 -> Text("🥈", fontSize = 16.sp)
                    3 -> Text("🥉", fontSize = 16.sp)
                    else -> Text(
                        "#$posicion",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            // Inicial o Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (esUsuarioActual) AmberAccent else MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    usuario.nombres.take(1).uppercase(),
                    color = if (esUsuarioActual) PixelInkOnCream else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.width(10.dp))

            // Nombre y Nivel
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        usuario.nombres,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (esUsuarioActual) FontWeight.Bold else FontWeight.Medium,
                        color = if (esUsuarioActual) AmberAccent else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (esUsuarioActual) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "(Tú)",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberAccent
                        )
                    }
                }
                Text(
                    "Nivel ${usuario.nivel} · ${if (usuario.carrera.isNotBlank()) usuario.carrera else "Explorador"}",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(8.dp))

            // Puntos
            Surface(
                color = AmberAccent.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.7f))
            ) {
                Text(
                    "${usuario.puntajeAcumulado} pts",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AmberAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
