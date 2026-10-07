package com.example.gamequest.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.gamequest.data.local.entity.PuntoInteresEntity
import com.example.gamequest.data.preferences.UserPreferencesRepository
import com.example.gamequest.data.repository.MisionConEstado
import com.example.gamequest.ui.common.encabezado
import com.example.gamequest.ui.components.DudeAnimation
import com.example.gamequest.ui.components.DudeSprite
import com.example.gamequest.ui.components.OnboardingDialog
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.ui.theme.PixelSuccess
import com.example.gamequest.util.AvatarColor
import com.example.gamequest.util.CharacterSpecies
import com.example.gamequest.util.LocalSoundManager
import com.example.gamequest.util.SoundEffect
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onMisionClick: (Int) -> Unit,
    puntoDestinoId: Int? = null,
    onPuntoDestinoAtendido: () -> Unit = {}
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

    Scaffold { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            // Horizontal / poco alto: encabezado en una sola fila y panel lateral para la misión
            // sugerida, así el mapa conserva altura útil en lugar de quedar aplastado.
            val compactoAlto = maxHeight < 480.dp
            val horizontal = maxWidth > maxHeight && maxWidth >= 560.dp

            Column(Modifier.fillMaxSize()) {
                EncabezadoInicio(
                    nombre = usuario?.nombres?.substringBefore(" ") ?: "explorador",
                    puntos = usuario?.puntajeAcumulado ?: 0,
                    busqueda = busqueda,
                    onBusquedaChange = viewModel::onBusquedaChange,
                    compacto = compactoAlto,
                    species = charSpecies,
                    primaryColor = charPrimary,
                    secondaryColor = charSecondary
                )

                if (busqueda.isNotBlank()) {
                    if (resultados.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Sin resultados para \"$busqueda\"",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
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
                    val alAbrirPunto: (PuntoInteresEntity) -> Unit = { punto ->
                        misiones.firstOrNull { it.punto.id == punto.id }?.let { onMisionClick(it.mision.id) }
                    }

                    val puntoObjetivoFinal = remember(puntoDestinoId, misiones, sugerida) {
                        if (puntoDestinoId != null) {
                            misiones.firstOrNull { it.punto.id == puntoDestinoId }?.punto
                                ?: sugerida?.punto
                        } else {
                            sugerida?.punto
                        }
                    }

                    LaunchedEffect(puntoDestinoId) {
                        if (puntoDestinoId != null) {
                            onPuntoDestinoAtendido()
                        }
                    }

                    if (horizontal) {
                        Row(Modifier.weight(1f).fillMaxWidth()) {
                            Box(Modifier.weight(1f).fillMaxHeight()) {
                                MapaCampus(
                                    puntos = puntosMapa,
                                    completados = completadosMapa,
                                    puntoObjetivo = puntoObjetivoFinal,
                                    primaryColor = charPrimary,
                                    secondaryColor = charSecondary,
                                    species = charSpecies,
                                    onPuntoClick = alAbrirPunto
                                )
                            }
                            sugerida?.let { estado ->
                                Box(
                                    Modifier.width(320.dp).fillMaxHeight().padding(end = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MisionSugeridaCard(estado = estado, onClick = { onMisionClick(estado.mision.id) })
                                }
                            }
                        }
                    } else {
                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            MapaCampus(
                                puntos = puntosMapa,
                                completados = completadosMapa,
                                puntoObjetivo = puntoObjetivoFinal,
                                primaryColor = charPrimary,
                                secondaryColor = charSecondary,
                                species = charSpecies,
                                onPuntoClick = alAbrirPunto
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
private fun EncabezadoInicio(
    nombre: String,
    puntos: Int,
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    compacto: Boolean,
    species: CharacterSpecies,
    primaryColor: AvatarColor,
    secondaryColor: AvatarColor
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 20.dp, vertical = if (compacto) 8.dp else 16.dp)
    ) {
        if (compacto) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Saludo(nombre = nombre, puntos = puntos, modifier = Modifier.weight(1f))
                CampoBusqueda(
                    busqueda = busqueda,
                    onBusquedaChange = onBusquedaChange,
                    modifier = Modifier.weight(1.4f)
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Saludo(nombre = nombre, puntos = puntos, modifier = Modifier.weight(1f))
                Spacer(Modifier.width(12.dp))
                // Decorativo: TalkBack no debe detenerse en el sprite.
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                        .clearAndSetSemantics { },
                    contentAlignment = Alignment.Center
                ) {
                    DudeSprite(
                        species = species,
                        animation = DudeAnimation.IDLE,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        size = 56.dp
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            CampoBusqueda(busqueda = busqueda, onBusquedaChange = onBusquedaChange, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Saludo(nombre: String, puntos: Int, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            "Hola, $nombre 👋",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.encabezado()
        )
        Spacer(Modifier.height(4.dp))
        Surface(
            color = Color.Black.copy(alpha = 0.25f),
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(2.dp, AmberAccent),
            // El emoji de estrella no se lee bien: se anuncia como "N puntos".
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "$puntos puntos" }
        ) {
            Text(
                "⭐ $puntos pts",
                color = AmberAccent,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun CampoBusqueda(
    busqueda: String,
    onBusquedaChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val soundManager = LocalSoundManager.current
    TextField(
        value = busqueda,
        onValueChange = onBusquedaChange,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        placeholder = { Text("Buscar lugar o misión", color = Color.White.copy(alpha = 0.8f)) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = AmberAccent) },
        trailingIcon = {
            if (busqueda.isNotEmpty()) {
                IconButton(onClick = {
                    soundManager?.play(SoundEffect.CLICK)
                    onBusquedaChange("")
                }) {
                    Icon(Icons.Filled.Close, contentDescription = "Limpiar búsqueda", tint = Color.White)
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Black.copy(alpha = 0.25f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.25f),
            focusedIndicatorColor = AmberAccent,
            unfocusedIndicatorColor = Color.White.copy(alpha = 0.6f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = AmberAccent
        )
    )
}

/**
 * Mapa del campus centrado y con su proporción: en pantallas bajas o anchas se
 * reduce en lugar de deformarse. Como el mapa es un lienzo táctil sin estructura
 * para lectores de pantalla, se describe y se remite a las alternativas accesibles.
 */
@Composable
private fun MapaCampus(
    puntos: List<PuntoInteresEntity>,
    completados: Set<Int>,
    puntoObjetivo: PuntoInteresEntity?,
    primaryColor: AvatarColor,
    secondaryColor: AvatarColor,
    species: CharacterSpecies,
    onPuntoClick: (PuntoInteresEntity) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CampusMapView(
            modifier = Modifier
                .widthIn(max = maxHeight * 0.85f)
                .semantics {
                    contentDescription = "Mapa interactivo del campus con ${puntos.size} puntos de interés, " +
                        "${completados.size} completados. Para una lista accesible usa el buscador " +
                        "o la pestaña Misiones."
                },
            puntos = puntos,
            completados = completados,
            puntoObjetivo = puntoObjetivo,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            characterSpecies = species,
            onPuntoClick = onPuntoClick
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
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .clearAndSetSemantics { },
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
                            // onPrimaryContainer: el color primario sobre su contenedor daba ~1.5:1.
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
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
            Text(
                estado.mision.insigniaEmoji,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.clearAndSetSemantics { }
            )
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
                // Ícono + texto: el estado no depende solo del color.
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PixelSuccess, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Completada", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
