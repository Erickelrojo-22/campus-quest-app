package com.example.gamequest.ui.crud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamequest.data.local.entity.MisionEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionManagementScreen(
    viewModel: MissionManagementViewModel,
    onBack: () -> Unit,
    onNuevaMision: () -> Unit,
    onEditarMision: (puntoId: Int) -> Unit
) {
    val misiones by viewModel.misiones.collectAsState()
    val misionAEliminar by viewModel.misionAEliminar.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de misiones") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNuevaMision) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva misión")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Text(
                "Datos locales · Room + MVVM",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
            )
            if (misiones.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay misiones registradas todavía.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 96.dp)) {
                    items(misiones, key = { it.first.id }) { (mision, punto) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(mision.insigniaEmoji, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(mision.titulo, fontWeight = FontWeight.Bold)
                                Text(
                                    "${punto.nombre} · ${mision.puntos} pts",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onEditarMision(punto.id) }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Editar")
                            }
                            IconButton(onClick = { viewModel.solicitarEliminacion(mision) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                            }
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    misionAEliminar?.let { mision ->
        AlertDialog(
            onDismissRequest = viewModel::cancelarEliminacion,
            title = { Text("Eliminar misión") },
            text = { Text("¿Seguro que deseas eliminar \"${mision.titulo}\"? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = viewModel::confirmarEliminacion) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelarEliminacion) { Text("Cancelar") }
            }
        )
    }
}
