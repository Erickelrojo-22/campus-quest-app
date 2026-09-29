package com.example.gamequest.ui.badges

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.AmberAccentDark
import androidx.compose.runtime.LaunchedEffect
import com.example.gamequest.ui.theme.TealPrimary
import com.example.gamequest.ui.theme.TealPrimaryDark
import com.example.gamequest.util.LocalSoundManager
import com.example.gamequest.util.SoundEffect

@Composable
fun BadgeEarnedScreen(
    viewModel: BadgeEarnedViewModel,
    onSiguienteMision: () -> Unit
) {
    val soundManager = LocalSoundManager.current
    val estado by viewModel.estado.collectAsState()
    val mision = estado.mision

    LaunchedEffect(Unit) {
        soundManager?.play(SoundEffect.BADGE_EARNED)
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(TealPrimary, TealPrimaryDark)))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "¡MISIÓN COMPLETADA!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(Brush.radialGradient(listOf(AmberAccent, AmberAccentDark)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(mision?.insigniaEmoji ?: "🎖", fontSize = 64.sp)
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    mision?.insigniaNombre ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    mision?.descripcionPista ?: "",
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    EstadisticaChip("+${mision?.puntos ?: 0}", "PUNTOS")
                    EstadisticaChip("Nv. ${estado.usuario?.nivel ?: 1}", "NIVEL")
                    EstadisticaChip("${estado.totalInsignias}/20", "INSIGNIAS")
                }
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onSiguienteMision,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = AmberAccent,
                        contentColor = Color(0xFF1A2E2A)
                    )
                ) {
                    Text("SIGUIENTE MISIÓN", style = MaterialTheme.typography.titleSmall)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { /* Compartir: fuera del alcance de esta etapa */ },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(4.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Filled.Share, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Compartir mi logro")
                }
            }
        }
    }
}

@Composable
private fun EstadisticaChip(valor: String, etiqueta: String) {
    Surface(color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(valor, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(etiqueta, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
        }
    }
}
