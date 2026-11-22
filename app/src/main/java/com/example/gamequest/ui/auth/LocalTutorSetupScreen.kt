package com.example.gamequest.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gamequest.ui.common.ContenidoAdaptable
import com.example.gamequest.ui.common.LocalPerfilVentanaCampus

/** First-run setup for the one tutor allowed by a local install. */
@Composable
fun LocalTutorSetupScreen(viewModel: LocalTutorSetupViewModel, onConfigured: () -> Unit, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val perfilVentana = LocalPerfilVentanaCampus.current
    ContenidoAdaptable(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = perfilVentana.margenHorizontal, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
        Text("Configurar tutor local")
        Text("Esta cuenta administra únicamente el catálogo almacenado en este dispositivo.")
        OutlinedTextField(state.nombres, viewModel::nombres, Modifier.fillMaxWidth(), label = { Text("Nombres") })
        OutlinedTextField(state.correo, viewModel::correo, Modifier.fillMaxWidth(), label = { Text("Correo institucional") })
        OutlinedTextField(state.carrera, viewModel::carrera, Modifier.fillMaxWidth(), label = { Text("Carrera") })
        OutlinedTextField(state.contrasena, viewModel::contrasena, Modifier.fillMaxWidth(), label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation())
        state.error?.let { Text(it) }
        Button(onClick = { viewModel.save { onConfigured() } }, enabled = !state.cargando, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.cargando) "Guardando…" else "Crear tutor local")
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}
