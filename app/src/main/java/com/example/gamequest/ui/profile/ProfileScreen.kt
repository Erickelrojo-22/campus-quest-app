package com.example.gamequest.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamequest.data.local.entity.Rol
import com.example.gamequest.ui.common.CampusBottomBar
import com.example.gamequest.ui.common.SessionViewModel
import com.example.gamequest.ui.navigation.Routes
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.InstitutionalRed



@Composable
fun ProfileScreen(
    sessionViewModel: SessionViewModel,
    onNavigateTab: (String) -> Unit,
    onSettings: () -> Unit,
    onMissionManagement: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val usuario by sessionViewModel.usuarioActual.collectAsState()

    Scaffold(bottomBar = { CampusBottomBar(currentRoute = Routes.PROFILE, onNavigate = onNavigateTab) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(18.dp).size(36.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(usuario?.nombres ?: "", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                if (!usuario?.correoInstitucional.isNullOrBlank()) {
                    Text(usuario?.correoInstitucional ?: "", color = AmberAccent, style = MaterialTheme.typography.bodySmall)
                }
                if (!usuario?.carrera.isNullOrBlank()) {
                    Text(usuario?.carrera ?: "", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                }
                if (usuario?.rol == Rol.TUTOR) {
                    Text("Tutor del programa de bienvenida", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(color = Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "⭐ Nivel ${usuario?.nivel ?: 1}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Surface(color = AmberAccent.copy(alpha = 0.3f), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "🏆 ${usuario?.puntajeAcumulado ?: 0} pts",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = AmberAccent,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

            }

            Spacer(Modifier.height(16.dp))

            OpcionPerfil(Icons.Filled.Settings, "Configuración", "Tema, notificaciones, idioma", onSettings)

            if (usuario?.rol == Rol.TUTOR) {
                OpcionPerfil(
                    Icons.Filled.AdminPanelSettings,
                    "Gestión de misiones",
                    "Crear, editar y eliminar misiones (modo tutor)",
                    onMissionManagement
                )
            }

            OpcionPerfil(
                Icons.AutoMirrored.Filled.Logout,
                "Cerrar sesión",
                "Tu progreso se mantiene guardado",
                onCerrarSesion,
                colorTexto = InstitutionalRed
            )
        }
    }
}

@Composable
private fun OpcionPerfil(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    colorTexto: Color = Color.Unspecified
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, tint = if (colorTexto != Color.Unspecified) colorTexto else MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.Bold, color = if (colorTexto != Color.Unspecified) colorTexto else MaterialTheme.colorScheme.onSurface)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
