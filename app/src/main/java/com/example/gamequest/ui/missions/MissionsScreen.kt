package com.example.gamequest.ui.missions

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamequest.data.local.entity.Dificultad
import com.example.gamequest.data.repository.MisionConEstado
import com.example.gamequest.ui.common.CampusBottomBar
import com.example.gamequest.ui.navigation.Routes
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.InstitutionalRed
import com.example.gamequest.util.LocalSoundManager
import com.example.gamequest.util.SoundEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionsScreen(
    viewModel: MissionsViewModel,
    onMisionClick: (Int) -> Unit,
    onNavigateTab: (String) -> Unit
) {
    val soundManager = LocalSoundManager.current
    val filtro by viewModel.filtro.collectAsState()
    val misiones by viewModel.misionesFiltradas.collectAsState()
    val activas by viewModel.contadorActivas.collectAsState()
    val completadas by viewModel.contadorCompletadas.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis misiones") },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        },
        bottomBar = { CampusBottomBar(currentRoute = Routes.MISSIONS, onNavigate = onNavigateTab) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filtro == FiltroMisiones.ACTIVAS,
                    onClick = {
                        soundManager?.play(SoundEffect.CLICK)
                        viewModel.onFiltroChange(FiltroMisiones.ACTIVAS)
                    },
                    label = { Text("Activas · $activas") }
                )
                FilterChip(
                    selected = filtro == FiltroMisiones.COMPLETADAS,
                    onClick = {
                        soundManager?.play(SoundEffect.CLICK)
                        viewModel.onFiltroChange(FiltroMisiones.COMPLETADAS)
                    },
                    label = { Text("Completadas · $completadas") }
                )
                FilterChip(
                    selected = filtro == FiltroMisiones.TODAS,
                    onClick = {
                        soundManager?.play(SoundEffect.CLICK)
                        viewModel.onFiltroChange(FiltroMisiones.TODAS)
                    },
                    label = { Text("Todas") }
                )
            }

            if (misiones.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        when (filtro) {
                            FiltroMisiones.ACTIVAS -> "No tienes misiones activas. ¡Completa todas!"
                            FiltroMisiones.COMPLETADAS -> "Aún no completas ninguna misión."
                            FiltroMisiones.TODAS -> "Todavía no hay misiones cargadas."
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    items(misiones, key = { it.mision.id }) { estado ->
                        MisionCard(estado, onClick = {
                            soundManager?.play(SoundEffect.CLICK)
                            onMisionClick(estado.mision.id)
                        })
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun MisionCard(estado: MisionConEstado, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(estado.mision.insigniaEmoji, style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(estado.mision.titulo, fontWeight = FontWeight.Bold)
                    Text(
                        estado.punto.nombre,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Etiqueta("⭐ ${estado.mision.puntos} pts", AmberAccent)
                        Etiqueta("⏱ ${estado.mision.tiempoEstimadoMin} min", MaterialTheme.colorScheme.primary)
                        val colorDificultad = when (estado.mision.dificultad) {
                            Dificultad.ALTA -> InstitutionalRed
                            Dificultad.MEDIA -> AmberAccent
                            else -> MaterialTheme.colorScheme.tertiary
                        }
                        Etiqueta(estado.mision.dificultad, colorDificultad)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            if (estado.completada) {
                Text("Completada", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            } else {
                LinearProgressIndicator(
                    progress = { 0f },
                    modifier = Modifier.fillMaxWidth().height(6.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text("Sin iniciar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun Etiqueta(texto: String, color: androidx.compose.ui.graphics.Color) {
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
        Text(
            texto,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
