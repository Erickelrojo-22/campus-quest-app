package com.example.gamequest.ui.auth

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.InstitutionalRed
import com.example.gamequest.ui.theme.PixelCream
import com.example.gamequest.ui.theme.PixelInkOnCream
import com.example.gamequest.ui.theme.TealPrimaryDark

/**
 * Pantalla de registro de usuario en la base de datos (RF-02).
 * Permite registrar cuentas de estudiante o tutor con correo institucional.
 */
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegistroExitoso: (UsuarioEntity) -> Unit,
    onVolverALogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(containerColor = TealPrimaryDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
            Spacer(Modifier.height(16.dp))

            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(3.dp, AmberAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "NUEVA CUENTA",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AmberAccent
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Únete a la expedición universitaria",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(Modifier.height(16.dp))

                    CampoTextoRetro(
                        etiqueta = "NOMBRES COMPLETOS",
                        valor = uiState.nombres,
                        onValorChange = viewModel::onNombresChange
                    )
                    Spacer(Modifier.height(10.dp))

                    CampoTextoRetro(
                        etiqueta = "CORREO INSTITUCIONAL (@live.uleam.edu.ec)",
                        valor = uiState.correo,
                        onValorChange = viewModel::onCorreoChange,
                        keyboardType = KeyboardType.Email
                    )
                    Spacer(Modifier.height(10.dp))

                    CampoTextoRetro(
                        etiqueta = "CARRERA O FACULTAD",
                        valor = uiState.carrera,
                        onValorChange = viewModel::onCarreraChange
                    )
                    Spacer(Modifier.height(10.dp))

                    CampoTextoRetro(
                        etiqueta = "CONTRASEÑA (MÍN. 6 CARACTERES)",
                        valor = uiState.contrasena,
                        onValorChange = viewModel::onContrasenaChange,
                        esPassword = true,
                        keyboardType = KeyboardType.Password
                    )
                    Spacer(Modifier.height(10.dp))

                    CampoTextoRetro(
                        etiqueta = "CONFIRMAR CONTRASEÑA",
                        valor = uiState.confirmarContrasena,
                        onValorChange = viewModel::onConfirmarContrasenaChange,
                        esPassword = true,
                        keyboardType = KeyboardType.Password
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.esTutor,
                            onCheckedChange = viewModel::onEsTutorChange,
                            colors = CheckboxDefaults.colors(
                                checkedColor = AmberAccent,
                                checkmarkColor = PixelInkOnCream,
                                uncheckedColor = Color.White
                            )
                        )
                        Text(
                            "Registrarme con rol de tutor/guía",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }

                    uiState.error?.let { mensaje ->
                        Spacer(Modifier.height(6.dp))
                        Text(
                            mensaje,
                            color = InstitutionalRed,
                            style = MaterialTheme.typography.bodySmall
                        )
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

@Composable
private fun CampoTextoRetro(
    etiqueta: String,
    valor: String,
    onValorChange: (String) -> Unit,
    esPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Text(
        etiqueta,
        style = MaterialTheme.typography.labelSmall,
        color = AmberAccent,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(4.dp))
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(2.dp),
        visualTransformation = if (esPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = PixelCream,
            unfocusedContainerColor = PixelCream,
            focusedTextColor = PixelInkOnCream,
            unfocusedTextColor = PixelInkOnCream,
            focusedIndicatorColor = PixelInkOnCream,
            unfocusedIndicatorColor = PixelInkOnCream,
            cursorColor = PixelInkOnCream
        )
    )
}
