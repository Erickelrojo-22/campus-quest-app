package com.example.gamequest.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

/**
 * Pantalla de registro de usuario en la base de datos (RF-02).
 * Permite registrar cuentas de estudiante con correo institucional.
 */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegistroExitoso: (UsuarioEntity) -> Unit,
    onVolverALogin: () -> Unit
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
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .border(3.dp, AmberAccent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.PersonAdd,
                        contentDescription = null,
                        tint = AmberAccent,
                        modifier = Modifier.size(40.dp)
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
                            "NUEVA CUENTA",
                            style = MaterialTheme.typography.headlineSmall,
                            color = AmberAccent,
                            modifier = Modifier.encabezado()
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Registro con correo institucional ULEAM",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Spacer(Modifier.height(16.dp))

                        CampoFormulario(
                            etiqueta = "Nombres completos",
                            valor = uiState.nombres,
                            onValorChange = viewModel::onNombresChange,
                            icono = Icons.Filled.Person,
                            imeAction = ImeAction.Next,
                            hayError = uiState.error != null
                        )
                        Spacer(Modifier.height(10.dp))

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
                            etiqueta = "Carrera o facultad",
                            valor = uiState.carrera,
                            onValorChange = viewModel::onCarreraChange,
                            placeholder = "Ej. Ingeniería de Software",
                            icono = Icons.Filled.School,
                            imeAction = ImeAction.Next,
                            hayError = uiState.error != null
                        )
                        Spacer(Modifier.height(10.dp))

                        CampoFormulario(
                            etiqueta = "Contraseña (mín. 6 caracteres)",
                            valor = uiState.contrasena,
                            onValorChange = viewModel::onContrasenaChange,
                            icono = Icons.Filled.Lock,
                            esPassword = true,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next,
                            hayError = uiState.error != null
                        )
                        Spacer(Modifier.height(10.dp))

                        CampoFormulario(
                            etiqueta = "Confirmar contraseña",
                            valor = uiState.confirmarContrasena,
                            onValorChange = viewModel::onConfirmarContrasenaChange,
                            icono = Icons.Filled.Lock,
                            esPassword = true,
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                            keyboardActions = KeyboardActions(
                                onDone = { viewModel.registrar(onRegistroExitoso) }
                            ),
                            hayError = uiState.error != null
                        )
                        Spacer(Modifier.height(10.dp))

                        // Aviso de seguridad
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
                                    "Define una contraseña exclusiva para Campus Quest. Nunca uses ni compartas la contraseña oficial de tu correo.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        uiState.error?.let { mensaje ->
                            Spacer(Modifier.height(10.dp))
                            MensajeError(texto = mensaje)
                        }

                        Spacer(Modifier.height(18.dp))
                        Button(
                            onClick = { viewModel.registrar(onRegistroExitoso) },
                            enabled = !uiState.cargando,
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberAccent,
                                contentColor = PixelInkOnCream
                            ),
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            if (uiState.cargando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.height(20.dp),
                                    strokeWidth = 2.dp,
                                    color = PixelInkOnCream
                                )
                            } else {
                                Text("REGISTRARME", style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        TextButton(onClick = onVolverALogin) {
                            Text(
                                "¿Ya tienes cuenta? Inicia sesión aquí",
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
