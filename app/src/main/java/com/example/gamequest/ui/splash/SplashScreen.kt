package com.example.gamequest.ui.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.gamequest.ui.theme.TealPrimary
import com.example.gamequest.ui.theme.TealPrimaryDark
import kotlinx.coroutines.delay

/**
 * RF-01: splash screen — muestra el logotipo mientras se carga la
 * configuración inicial (DataStore) y se verifica si existe una sesión activa.
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1100)
        onTimeout()
    }

    val transition = rememberInfiniteTransition(label = "splash-progress")
    val progreso by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label = "progreso"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(TealPrimary, TealPrimaryDark))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(TealPrimaryDark, CircleShape)
                    .border(4.dp, AmberAccent, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Explore,
                    contentDescription = null,
                    tint = AmberAccent,
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "CAMPUS QUEST",
                color = AmberAccent,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Descubre tu universidad\nmisión a misión",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "LOADING...",
                color = AmberAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.6f)),
                color = AmberAccent,
                trackColor = TealPrimaryDark
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "ULEAM · Carrera de Software",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }
    }
}
