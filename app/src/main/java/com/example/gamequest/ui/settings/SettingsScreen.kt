package com.example.gamequest.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamequest.ui.theme.InstitutionalRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val prefs by viewModel.preferencias.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            SeccionTitulo("APARIENCIA")
            FilaSwitch(Icons.Filled.DarkMode, "Tema oscuro", prefs.temaOscuro, viewModel::setTemaOscuro)
            HorizontalDivider()

            Spacer()
            SeccionTitulo("NOTIFICACIONES")
            FilaSwitch(Icons.Filled.Notifications, "Recordatorios de misión", prefs.recordatoriosMision, viewModel::setRecordatoriosMision)
            FilaSwitch(Icons.Filled.Notifications, "Avisos del campus", prefs.avisosCampus, viewModel::setAvisosCampus)
            FilaSwitch(Icons.Filled.Vibration, "Sonido y vibración", prefs.sonidoVibracion, viewModel::setSonidoVibracion)
            HorizontalDivider()

            Spacer()
            SeccionTitulo("PREFERENCIAS")
            FilaInfo(Icons.Filled.Language, "Idioma", prefs.idioma)
            FilaInfo(Icons.Filled.LocationCity, "Campus por defecto", prefs.campusPorDefecto)
            FilaSwitch(Icons.Filled.CloudOff, "Descargar mapa sin conexión", prefs.descargarMapaSinConexion, viewModel::setDescargarMapaSinConexion)

            Spacer()
            Text(
                "Preferencias guardadas con DataStore",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer()
            OutlinedButton(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth(),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = InstitutionalRed)
            ) {
                Text("Cerrar sesión", fontWeight = FontWeight.Bold)
            }
            Spacer()
        }
    }
}

@Composable
private fun Spacer() = androidx.compose.foundation.layout.Spacer(Modifier.padding(vertical = 6.dp))

@Composable
private fun SeccionTitulo(texto: String) {
    Text(
        texto,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 10.dp)
    )
}

@Composable
private fun FilaSwitch(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    valor: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(titulo, modifier = Modifier.padding(start = 14.dp).weight(1f))
        Switch(checked = valor, onCheckedChange = onChange)
    }
}

@Composable
private fun FilaInfo(icono: androidx.compose.ui.graphics.vector.ImageVector, titulo: String, valor: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(titulo, modifier = Modifier.padding(start = 14.dp).weight(1f))
        Text(valor, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
    }
}
