package com.example.gamequest.ui.crud

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.gamequest.data.local.entity.Dificultad

private val categorias = listOf("Académico", "Trámites", "Servicios", "Recreación")
private val dificultades = listOf(Dificultad.BAJA, Dificultad.MEDIA, Dificultad.ALTA)
private val emojisSugeridos = listOf("📚", "🏛️", "💻", "💚", "☕", "⚽", "📄", "🖥️", "🎖")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionFormScreen(
    viewModel: MissionFormViewModel,
    onGuardado: () -> Unit,
    onCancelar: () -> Unit
) {
    val estado by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (estado.esEdicion) "Editar misión" else "Nueva misión") },
                navigationIcon = {
                    IconButton(onClick = onCancelar) { Icon(Icons.Filled.Close, contentDescription = "Cerrar") }
                },
                actions = {
                    TextButton(onClick = { viewModel.guardar(onGuardado) }, enabled = !estado.guardando) {
                        Text("GUARDAR", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Etiqueta("Título de la misión")
            OutlinedTextField(estado.titulo, viewModel::onTitulo, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(14.dp))

            Etiqueta("Nombre del punto de interés")
            OutlinedTextField(estado.nombreLugar, viewModel::onNombreLugar, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(14.dp))

            Etiqueta("Categoría")
            var categoriaExpandida by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = categoriaExpandida, onExpandedChange = { categoriaExpandida = it }) {
                OutlinedTextField(
                    value = estado.categoria,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpandida) }
                )
                ExposedDropdownMenu(expanded = categoriaExpandida, onDismissRequest = { categoriaExpandida = false }) {
                    categorias.forEach { opcion ->
                        DropdownMenuItem(text = { Text(opcion) }, onClick = { viewModel.onCategoria(opcion); categoriaExpandida = false })
                    }
                }
            }
            Spacer(Modifier.height(14.dp))

            Etiqueta("Horario de atención")
            OutlinedTextField(estado.horarioAtencion, viewModel::onHorario, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(14.dp))

            Etiqueta("Trámites o servicios que se realizan allí")
            OutlinedTextField(estado.tramites, viewModel::onTramites, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Spacer(Modifier.height(14.dp))

            Etiqueta("Descripción / pista")
            OutlinedTextField(estado.descripcionPista, viewModel::onDescripcionPista, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Spacer(Modifier.height(14.dp))

            Row {
                Column(Modifier.weight(1f)) {
                    Etiqueta("Puntos")
                    OutlinedTextField(
                        estado.puntos, viewModel::onPuntos,
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Etiqueta("Tiempo (min)")
                    OutlinedTextField(
                        estado.tiempoEstimadoMin, viewModel::onTiempo,
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            Etiqueta("Dificultad")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                dificultades.forEach { d ->
                    androidx.compose.material3.FilterChip(
                        selected = estado.dificultad == d,
                        onClick = { viewModel.onDificultad(d) },
                        label = { Text(d) }
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            Etiqueta("Insignia asociada")
            OutlinedTextField(estado.insigniaNombre, viewModel::onInsigniaNombre, modifier = Modifier.fillMaxWidth(), singleLine = true, placeholder = { Text("Nombre de la insignia") })
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                emojisSugeridos.forEach { emoji ->
                    OutlinedButton(onClick = { viewModel.onInsigniaEmoji(emoji) }) { Text(emoji) }
                }
            }
            Spacer(Modifier.height(14.dp))

            Etiqueta("Código QR")
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                OutlinedTextField(
                    estado.codigoQr, viewModel::onCodigoQr,
                    modifier = Modifier.weight(1f), singleLine = true,
                    placeholder = { Text("CQ-LAB-204") }
                )
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = viewModel::generarCodigoQr) { Text("GENERAR") }
            }

            estado.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { viewModel.guardar(onGuardado) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = !estado.guardando,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = com.example.gamequest.ui.theme.AmberAccent,
                    contentColor = com.example.gamequest.ui.theme.PixelInkOnCream
                )
            ) {
                Text(if (estado.esEdicion) "GUARDAR CAMBIOS" else "GUARDAR MISIÓN", style = MaterialTheme.typography.titleSmall)
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = onCancelar,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
            ) {
                Text("Cancelar")
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun Etiqueta(texto: String) {
    Text(
        texto.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(4.dp))
}
