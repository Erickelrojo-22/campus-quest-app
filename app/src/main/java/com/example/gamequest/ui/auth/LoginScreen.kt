package com.example.gamequest.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.ui.common.CampoFormulario
import com.example.gamequest.ui.common.ContenidoAdaptable
import com.example.gamequest.ui.common.MensajeError
import com.example.gamequest.ui.common.encabezado
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.PixelCream
import com.example.gamequest.ui.theme.PixelInkOnCream
import com.example.gamequest.ui.theme.TealPrimaryDark

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginExitoso: (UsuarioEntity) -> Unit,
    onIrARegistro: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = TealPrimaryDark) { padding ->
        ContenidoAdaptable(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(3.dp, AmberAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Explore,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(46.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))

                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(3.dp, AmberAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "CAMPUS QUEST",
                            style = MaterialTheme.typography.headlineSmall,
                            color = AmberAccent,
                            modifier = Modifier.encabezado()
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (uiState.modoInstitucional) {
                                "Acceso con correo estudiantil ULEAM"
                            } else {
                                "Modo de prueba para evaluación de la app"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(Modifier.height(16.dp))

                    // Selector de Modo: Estudiantil (Oficial) vs Modo Prueba (Testing)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.modoInstitucional,
                            onClick = { viewModel.onModoInstitucionalChange(true) },
                            label = { Text("Estudiantil") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberAccent,
                                selectedLabelColor = PixelInkOnCream,
                                selectedLeadingIconColor = PixelInkOnCream
                            )
                        )
                        FilterChip(
                            selected = !uiState.modoInstitucional,
                            onClick = { viewModel.onModoInstitucionalChange(false) },
                            label = { Text("Modo Prueba") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.BugReport,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberAccent,
                                selectedLabelColor = PixelInkOnCream,
                                selectedLeadingIconColor = PixelInkOnCream
                            )
                        )
                    }
                    Spacer(Modifier.height(16.dp))

                    if (uiState.modoInstitucional) {
                        CampoFormulario(
                            etiqueta = "Correo institucional (@live.uleam.edu.ec)",
                            valor = uiState.correo,
                            onValorChange = viewModel::onCorreoChange,
                            placeholder = "usuario@live.uleam.edu.ec",
                            icono = Icons.Filled.Email,
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next,
                            hayError = uiState.error != null
                        )
                        Spacer(Modifier.height(10.dp))

                        CampoFormulario(
                            etiqueta = "Contraseña",
                            valor = uiState.contrasena,
                            onValorChange = viewModel::onContrasenaChange,
                            icono = Icons.Filled.Lock,
                            esPassword = true,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                            keyboardActions = KeyboardActions(
                                onDone = { viewModel.iniciarSesion(onLoginExitoso) }
                            ),
                            hayError = uiState.error != null
                        )
                        Spacer(Modifier.height(10.dp))

                        // Tarjeta de aviso de seguridad
                        Surface(
                            color = PixelCream.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Security,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    "Por tu seguridad, usa la contraseña creada para Campus Quest (no uses tu contraseña de correo universitario).",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                    } else {
                        // MODO DE PRUEBA (TESTING)
                        Surface(
                            color = PixelCream.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.BugReport,
                                    contentDescription = null,
                                    tint = AmberAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    "Modo de prueba rápido: escribe cualquier nombre para explorar la app sin registrar correo.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))

                        CampoFormulario(
                            etiqueta = "Nombre del tester",
                            valor = uiState.nombre,
                            onValorChange = viewModel::onNombreChange,
                            placeholder = "Ej. Aventurero Test",
                            icono = Icons.Filled.Person,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done,
                            keyboardActions = KeyboardActions(
                                onDone = { viewModel.iniciarSesion(onLoginExitoso) }
                            ),
                            hayError = uiState.error != null
                        )
                        Spacer(Modifier.height(8.dp))

                        // Atajos rápidos para testing
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AssistChip(
                                onClick = { viewModel.onSeleccionarUsuarioPrueba("Tester Estudiante") },
                                label = { Text("Tester Estudiante", fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    labelColor = AmberAccent
                                ),
                                border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f))
                            )
                            AssistChip(
                                onClick = { viewModel.onSeleccionarUsuarioPrueba("Aventurero Demo") },
                                label = { Text("Aventurero Demo", fontSize = 11.sp) },
                                colors = AssistChipDefaults.assistChipColors(
                                    labelColor = AmberAccent
                                ),
                                border = BorderStroke(1.dp, AmberAccent.copy(alpha = 0.5f))
                            )
                        }
                    }

                    // Mensaje de Error
                    uiState.error?.let { mensaje ->
                        Spacer(Modifier.height(10.dp))
                        MensajeError(texto = mensaje)
                    }

                    Spacer(Modifier.height(18.dp))

                    // Botón de acción principal
                    Button(
                        onClick = { viewModel.iniciarSesion(onLoginExitoso) },
                        enabled = !uiState.cargando,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberAccent,
                            contentColor = PixelInkOnCream
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (uiState.cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(20.dp),
                                strokeWidth = 2.dp,
                                color = PixelInkOnCream
                            )
                        } else {
                            Text(
                                if (uiState.modoInstitucional) "INICIAR SESIÓN" else "ENTRAR EN MODO PRUEBA",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    // Enlace a Registro (visible prominentemente en modo institucional)
                    if (uiState.modoInstitucional) {
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = onIrARegistro) {
                            Text(
                                "¿Eres estudiante nuevo? Regístrate aquí",
                                style = MaterialTheme.typography.bodySmall,
                                color = AmberAccent
                            )
                        }
                    }
                }
            }
        }
    }
}
}


