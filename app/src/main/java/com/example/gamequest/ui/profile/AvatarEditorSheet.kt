package com.example.gamequest.ui.profile

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gamequest.ui.components.CharacterAnimation
import com.example.gamequest.ui.components.CharacterSprite
import com.example.gamequest.ui.theme.AmberAccent
import com.example.gamequest.util.AvatarColor
import com.example.gamequest.util.CharacterSpecies
import com.example.gamequest.util.LocalSoundManager
import com.example.gamequest.util.SoundEffect

/**
 * Bottom sheet modal para editar el avatar del usuario.
 *
 * Permite elegir especie, color primario (cuerpo / capucha / pelaje)
 * y color secundario (pañuelo / rostro / vientre) con vista previa en vivo.
 * El estado se gestiona de forma centralizada en [ProfileViewModel].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarEditorSheet(
    species: CharacterSpecies,
    primary: AvatarColor,
    secondary: AvatarColor,
    activeZone: AvatarColorZone,
    previewAnim: CharacterAnimation,
    onSpeciesChange: (CharacterSpecies) -> Unit,
    onActiveZoneChange: (AvatarColorZone) -> Unit,
    onColorSelected: (AvatarColor) -> Unit,
    onTogglePreviewAnim: () -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val soundManager = LocalSoundManager.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Título ──────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Filled.Palette, contentDescription = null, tint = AmberAccent)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Editar Avatar",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Vista previa animada ────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        soundManager?.play(SoundEffect.CLICK)
                        onTogglePreviewAnim()
                    },
                contentAlignment = Alignment.Center,
            ) {
                CharacterSprite(
                    species        = species,
                    animation      = previewAnim,
                    primaryColor   = primary,
                    secondaryColor = secondary,
                    size           = 80.dp,
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Selector de especie ─────────────────────────────────────
            Text(
                "Personaje",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CharacterSpecies.entries.forEach { sp ->
                    val isSelected = sp == species
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) AmberAccent
                                        else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(10.dp),
                            )
                            .clickable {
                                soundManager?.play(SoundEffect.CLICK)
                                onSpeciesChange(sp)
                            },
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            CharacterSprite(
                                species        = sp,
                                animation      = CharacterAnimation.IDLE,
                                primaryColor   = primary,
                                secondaryColor = secondary,
                                size           = 36.dp,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                sp.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) AmberAccent
                                        else MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Zona a pintar (tabs) ────────────────────────────────────
            Text(
                "Zona a pintar",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ZoneTab(
                    label = species.primaryZoneLabel,
                    subtitle = "Principal",
                    selected = activeZone == AvatarColorZone.PRIMARY,
                    color = Color(primary.preview),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        soundManager?.play(SoundEffect.CLICK)
                        onActiveZoneChange(AvatarColorZone.PRIMARY)
                    },
                )
                ZoneTab(
                    label = species.secondaryZoneLabel,
                    subtitle = "Secundario",
                    selected = activeZone == AvatarColorZone.SECONDARY,
                    color = Color(secondary.preview),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        soundManager?.play(SoundEffect.CLICK)
                        onActiveZoneChange(AvatarColorZone.SECONDARY)
                    },
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Grid de 16 colores (4×4) ────────────────────────────────
            val activeColor = if (activeZone == AvatarColorZone.PRIMARY) primary else secondary
            val allColors = AvatarColor.entries

            for (row in 0 until 4) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (col in 0 until 4) {
                        val idx = row * 4 + col
                        if (idx >= allColors.size) break
                        val c = allColors[idx]
                        ColorDot(
                            color = Color(c.preview),
                            label = c.label,
                            selected = c == activeColor,
                            onClick = {
                                soundManager?.play(SoundEffect.CLICK)
                                onColorSelected(c)
                            },
                        )
                    }
                }
                if (row < 3) Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(20.dp))

            // ── Botones de acción ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Restablecer
                OutlinedButton(
                    onClick = {
                        soundManager?.play(SoundEffect.CLICK)
                        onReset()
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reiniciar")
                }

                // Guardar
                Button(
                    onClick = {
                        soundManager?.play(SoundEffect.CLICK)
                        onSave()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Guardar", color = Color.Black)
                }
            }
        }
    }
}

// ── Componentes internos ────────────────────────────────────────────────

@Composable
private fun ZoneTab(
    label: String,
    subtitle: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bgColor by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        label = "zone_bg",
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) AmberAccent else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(10.dp),
            )
            .clickable(onClick = onClick),
        color = bgColor,
        shape = RoundedCornerShape(10.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (selected) Modifier.border(2.dp, Color.White, CircleShape)
                        else Modifier
                    ),
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) AmberAccent
                            else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected) Color.White.copy(alpha = 0.85f)
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ColorDot(
    color: Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (selected) Modifier.border(3.dp, Color.White, CircleShape)
                    else Modifier.border(1.dp, Color.Gray.copy(alpha = 0.3f), CircleShape)
                )
                .clickable(onClick = onClick),
        )
        Spacer(Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
