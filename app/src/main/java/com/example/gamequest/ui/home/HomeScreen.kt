package com.example.gamequest.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamequest.data.preferences.UserPreferencesRepository
import com.example.gamequest.data.repository.MisionConEstado
import com.example.gamequest.ui.common.CampusBottomBar
import com.example.gamequest.ui.components.DudeAnimation
import com.example.gamequest.ui.components.DudeSprite
import com.example.gamequest.ui.components.OnboardingDialog
import com.example.gamequest.ui.navigation.Routes
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.AmberAccentDark
import com.example.gamequest.util.AvatarColor
import com.example.gamequest.util.CharacterSpecies
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMisionClick: (Int) -> Unit,
    onNavigateTab: (String) -> Unit
) {
    val usuario by viewModel.usuario.collectAsState()
    val misiones by viewModel.misiones.collectAsState()
    val busqueda by viewModel.busqueda.collectAsState()
    val resultados by viewModel.resultadosBusqueda.collectAsState()
    val sugerida by viewModel.misionSugerida.collectAsState()

    val context = LocalContext.current
    val prefsRepo = remember { UserPreferencesRepository(context) }
    val prefs by prefsRepo.preferencias.collectAsState(initial = null)
    val charPrimary = prefs?.avatarPrimaryColor ?: AvatarColor.COBALT_BLUE
    val charSecondary = prefs?.avatarSecondaryColor ?: AvatarColor.RUBY_RED
    val charSpecies = prefs?.characterSpecies ?: CharacterSpecies.DUDE

    Scaffold(
        bottomBar = { CampusBottomBar(currentRoute = Routes.HOME, onNavigate = onNavigateTab) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Hola, ${usuario?.nombres?.substringBefore(" ") ?: "explorador"} 👋",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            color = Color.Black.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, AmberAccent)
                        ) {
                            Text(
                                "⭐ ${usuario?.puntajeAcumulado ?: 0} pts",
                                color = AmberAccent,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Color.White.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        DudeSprite(
                            species = charSpecies,
                            animation = DudeAnimation.IDLE,
                            primaryColor = charPrimary,
                            secondaryColor = charSecondary,
                            size = 56.dp
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                TextField(
                    value = busqueda,
                    onValueChange = viewModel::onBusquedaChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    placeholder = { Text("Buscar lugar o misión", color = Color.White.copy(alpha = 0.7f)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = AmberAccent) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Black.copy(alpha = 0.25f),
                        unfocusedContainerColor = Color.Black.copy(alpha = 0.25f),
                        focusedIndicatorColor = AmberAccent,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AmberAccent
                    )
                )
            }

            if (busqueda.isNotBlank()) {
                if (resultados.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Sin resultados para \"$busqueda\"", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(resultados, key = { it.mision.id }) { estado ->
                            ResultadoBusquedaItem(estado, onClick = { onMisionClick(estado.mision.id) })
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            } else {
                // Se recalculan solo cuando cambia la lista de misiones, no en
                // cada recomposición (p. ej. al teclear en el buscador o al
                // actualizarse el usuario/puntaje).
                val puntosMapa = remember(misiones) { misiones.map { it.punto }.distinctBy { it.id } }
                val completadosMapa = remember(misiones) {
                    misiones.filter { it.completada }.map { it.punto.id }.toSet()
                }
                Box(modifier = Modifier.weight(1f).padding(16.dp)) {
                    CampusMapView(
                        puntos = puntosMapa,
                        completados = completadosMapa,
                        puntoObjetivo = sugerida?.punto,
                        primaryColor = charPrimary,
                        secondaryColor = charSecondary,
                        characterSpecies = charSpecies,
                        onPuntoClick = { punto ->
                            misiones.firstOrNull { it.punto.id == punto.id }?.let { onMisionClick(it.mision.id) }
                        }
                    )
                }
                sugerida?.let { estado ->
                    MisionSugeridaCard(
                        estado = estado,
                        onClick = { onMisionClick(estado.mision.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }

    val scope = rememberCoroutineScope()
    if (prefs != null && !prefs!!.tutorialVisto) {
        OnboardingDialog(
            onDismiss = {
                scope.launch { prefsRepo.setTutorialVisto(true) }
            },
            onCompletar = {
                scope.launch { prefsRepo.setTutorialVisto(true) }
            }
        )
    }
}

@Composable
private fun MisionSugeridaCard(estado: MisionConEstado, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(estado.mision.insigniaEmoji, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            "MISIÓN SUGERIDA",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(estado.mision.titulo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "${estado.punto.nombre} · ⭐ ${estado.mision.puntos} pts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ResultadoBusquedaItem(estado: MisionConEstado, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(estado.mision.insigniaEmoji, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(estado.punto.nombre, fontWeight = FontWeight.Bold)
                Text(
                    "${estado.punto.categoria} · ${estado.mision.titulo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (estado.completada) {
                Text("Completada", style = MaterialTheme.typography.labelSmall, color = AmberAccentDark)
            }
        }
    }
}
