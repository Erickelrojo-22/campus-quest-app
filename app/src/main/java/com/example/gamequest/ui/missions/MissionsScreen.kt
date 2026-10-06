package com.example.gamequest.ui.missions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamequest.data.local.entity.Dificultad
import com.example.gamequest.data.repository.MisionConEstado
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.InstitutionalRed
import com.example.gamequest.ui.theme.PixelSuccess
import com.example.gamequest.util.LocalSoundManager
import com.example.gamequest.util.SoundEffect

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MissionsScreen(
    viewModel: MissionsViewModel,
    onMisionClick: (Int) -> Unit
) {
    val soundManager = LocalSoundManager.current
    val filtro by viewModel.filtro.collectAsState()
    val busqueda by viewModel.busqueda.collectAsState()
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
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Cabecera superior con buscador y filtros
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TextField(
                    value = busqueda,
                    onValueChange = viewModel::onBusquedaChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    placeholder = { Text("Buscar misión, lugar o dificultad...", color = Color.White.copy(alpha = 0.8f)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = AmberAccent) },
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) {
                            IconButton(onClick = {
                                soundManager?.play(SoundEffect.CLICK)
                                viewModel.limpiarBusqueda()
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda", tint = Color.White)
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Black.copy(alpha = 0.25f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.25f),
                        focusedIndicatorColor = AmberAccent,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.6f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AmberAccent
                    )
                )

                Spacer(Modifier.height(8.dp))

                // FlowRow: con fuente grande o pantalla angosta los filtros pasan a la
                // línea siguiente en vez de cortarse o salirse de la pantalla.
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
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
            }

            if (misiones.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (busqueda.isNotBlank()) {
                            "No se encontraron misiones para \"$busqueda\"."
                        } else {
                            when (filtro) {
                                FiltroMisiones.ACTIVAS -> "No tienes misiones activas. ¡Completa todas!"
                                FiltroMisiones.COMPLETADAS -> "Aún no completas ninguna misión."
                                FiltroMisiones.TODAS -> "Todavía no hay misiones cargadas."
                            }
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                // Rejilla adaptable: 1 columna en teléfono vertical, 2 o más en horizontal / tablet.
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 320.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(misiones, key = { it.mision.id }) { estado ->
                        MisionCard(estado, onClick = {
                            soundManager?.play(SoundEffect.CLICK)
                            onMisionClick(estado.mision.id)
                        })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MisionCard(estado: MisionConEstado, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            // TalkBack: "Misión X, Completada/Pendiente, botón" en un solo foco.
            .semantics { stateDescription = if (estado.completada) "Completada" else "Pendiente" }
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        // El emoji es decorativo; el nombre de la insignia ya está en el texto.
                        .clearAndSetSemantics { },
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
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PixelSuccess, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Completada",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Barra decorativa (siempre vacía): se oculta a TalkBack para no leer "0%" sin sentido.
                LinearProgressIndicator(
                    progress = { 0f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clearAndSetSemantics { }
                )
                Spacer(Modifier.height(4.dp))
                Text("Sin iniciar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Etiqueta de dato. El texto usa el color de contenido del tema (contraste alto) y el
 * color de categoría se muestra como fondo tenue + borde, de modo que se distinga
 * sin que el texto dependa de un color de baja luminosidad sobre fondo oscuro.
 */
@Composable
private fun Etiqueta(texto: String, color: androidx.compose.ui.graphics.Color) {
    Surface(
        color = color.copy(alpha = 0.22f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.9f))
    ) {
        Text(
            texto,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
