package com.example.gamequest.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamequest.data.local.entity.UsuarioEntity
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.PixelCream
import com.example.gamequest.ui.theme.PixelInkOnCream
import com.example.gamequest.ui.theme.TealPrimaryDark

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginExitoso: (UsuarioEntity) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var esTutor by remember { mutableStateOf(false) }

    Scaffold(containerColor = TealPrimaryDark) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(3.dp, AmberAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Explore,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(20.dp))

            // Panel tipo "consola retro" — borde grueso dorado sobre fondo
            // verde azulado, como en el mockup "Campus Login".
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
                        "CAMPUS QUEST",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AmberAccent
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Escribe tu nombre para empezar tu aventura",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(Modifier.height(20.dp))

                    Text(
                        "TU NOMBRE",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberAccent,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(2.dp),
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
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = esTutor,
                            onCheckedChange = { esTutor = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = AmberAccent,
                                checkmarkColor = PixelInkOnCream,
                                uncheckedColor = Color.White
                            )
                        )
                        Text(
                            "Soy tutor del programa de bienvenida",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }

                    uiState.error?.let { mensaje ->
                        Spacer(Modifier.height(4.dp))
                        Text(mensaje, color = AmberAccent, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(18.dp))
                    Button(
                        onClick = { viewModel.entrar(nombre, esTutor, onLoginExitoso) },
                        enabled = !uiState.cargando,
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmberAccent,
                            contentColor = PixelInkOnCream
                        ),
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        if (uiState.cargando) {
                            CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp, color = PixelInkOnCream)
                        } else {
                            Text("ENTRAR", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
