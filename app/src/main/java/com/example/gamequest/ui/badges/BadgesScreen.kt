package com.example.gamequest.ui.badges

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.ui.common.CampusBottomBar
import com.example.gamequest.ui.navigation.Routes
import com.example.gamequest.ui.theme.AmberAccent

@Composable
fun BadgesScreen(
    viewModel: BadgesViewModel,
    usuarioActualId: Int,
    onNavigateTab: (String) -> Unit
) {
    val estado by viewModel.estado.collectAsState()

    Scaffold(bottomBar = { CampusBottomBar(currentRoute = Routes.BADGES, onNavigate = onNavigateTab) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                SeccionTitulo("OBTENIDAS (${estado.obtenidas.size})")
            }
            item {
                InsigniasGrid(estado.obtenidas.map { it.mision.insigniaEmoji }, bloqueadas = false)
            }

            item {
                SeccionTitulo("POR DESBLOQUEAR (${estado.pendientes.size})")
            }
            item {
                InsigniasGrid(estado.pendientes.map { it.mision.insigniaEmoji }, bloqueadas = true)
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
}

@Composable
private fun SeccionTitulo(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@Composable
private fun InsigniasGrid(emojis: List<String>, bloqueadas: Boolean) {
    if (emojis.isEmpty()) {
        Text(
            if (bloqueadas) "¡Ya desbloqueaste todas las insignias disponibles!" else "Completa tu primera misión para ganar una insignia.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxWidth()
            .height((((emojis.size + 3) / 4) * 74).dp)
            .padding(horizontal = 16.dp),
        userScrollEnabled = false
    ) {
        items(emojis) { emoji ->
            Surface(
                modifier = Modifier.padding(6.dp).size(60.dp),
                shape = CircleShape,
                color = if (bloqueadas) MaterialTheme.colorScheme.surfaceVariant else AmberAccent.copy(alpha = 0.25f)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (bloqueadas) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    } else {
                        Text(emoji, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun RankingRow(usuario: UsuarioEntity, esUsuarioActual: Boolean, posicion: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
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
            Text("${usuario.puntajeAcumulado}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
