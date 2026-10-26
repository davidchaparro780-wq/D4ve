package com.auraplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auraplayer.audio.EqualizerManager
import com.auraplayer.data.repository.PlaylistManager

@Composable
fun EqualizerScreen(
    playlistManager: PlaylistManager,
    currentAccent: String,
    onAccentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val view = androidx.compose.ui.platform.LocalView.current
    val eqManager = remember { EqualizerManager.instance }
    var isEqEnabled by remember { mutableStateOf(eqManager.isEnabled) }
    var selectedPreset by remember { mutableIntStateOf(0) }
    var selectedProfile by remember { mutableIntStateOf(0) }
    val presets = listOf(
        "Aura Flat", "Cyber Bass", "Vocal Glow", "Neon Pop",
        "Rock Velvet", "Lo-Fi Lounge", "Electronic", "Audiófilo Hi-Fi"
    )

    val deviceProfiles = listOf(
        "🎧 In-Ear Earbuds",
        "🦻 Over-Ear Bass",
        "🚗 Car Audio",
        "🎙️ Studio Flat",
        "🔊 Altavoz Móvil"
    )

    val bandFrequencies = EqualizerManager.TEN_BAND_FREQUENCIES
    val bandLevels = remember {
        mutableStateListOf<Float>().apply {
            for (i in 0 until 10) add(eqManager.bandLevels[i])
        }
    }

    var bassBoost by remember { mutableFloatStateOf(eqManager.bassStrength / 10f) }
    var virtualizer by remember { mutableFloatStateOf(eqManager.virtualizerStrength / 10f) }
    var loudness by remember { mutableFloatStateOf(eqManager.loudnessGain / 10f) }
    var crossfadeSeconds by remember { mutableIntStateOf(playlistManager.getCrossfadeSeconds()) }
    var isKaraokeEnabled by remember { mutableStateOf(eqManager.isKaraokeMode) }
    var isSpatial8D by remember { mutableStateOf(eqManager.isSpatial8DEnabled) }
    var selectedEqTab by remember { mutableIntStateOf(0) }

    val themePrimary = MaterialTheme.colorScheme.primary
    val themeSecondary = MaterialTheme.colorScheme.secondary
    val themeBackground = MaterialTheme.colorScheme.background
    val atmosphericGradient = remember(themePrimary, themeSecondary, themeBackground) {
        Brush.verticalGradient(
            listOf(
                themePrimary.copy(alpha = 0.20f),
                themeSecondary.copy(alpha = 0.10f),
                themeBackground,
                themeBackground
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(atmosphericGradient)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Master Toggle Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    6.dp,
                    RoundedCornerShape(20.dp),
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E2842), Color(0xFF101626))
                    )
                ),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Aura Studio Effects",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = if (isEqEnabled) "DSP 32-bit Float • 10 Bandas" else "Desactivado",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isEqEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isEqEnabled,
                    onCheckedChange = {
                        isEqEnabled = it
                        eqManager.setEnabled(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dual-Tab Console Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0F1524))
                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(14.dp))
                .padding(4.dp)
        ) {
            val tabs = listOf("🎚️ Ecualizador & Presets", "🌌 Efectos & Inmersión")
            tabs.forEachIndexed { index, title ->
                val tabModifier = if (isSelected) {
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
                                )
                            )
                        )
                } else {
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                }
                Box(
                    modifier = tabModifier
                        .clickable {
                            com.auraplayer.audio.AuraHaptic.click(view)
                            selectedEqTab = index
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedEqTab == 0) {
            // TAB 0: Ecualizador, Perfiles Acústicos, Presets & Temas

            // Headphone & Output Acoustic Device Profiles
            Text(
                text = "PERFILES ACÚSTICOS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(deviceProfiles) { index, profile ->
                    FilterChip(
                        selected = selectedProfile == index,
                        onClick = {
                            selectedProfile = index
                            when (index) {
                                0 -> { // In-Ear
                                    eqManager.applyPreset(3) // Neon Pop
                                    virtualizer = 60f
                                    bassBoost = 45f
                                }
                                1 -> { // Over-Ear Bass
                                    eqManager.applyPreset(1) // Cyber Bass
                                    bassBoost = 85f
                                    virtualizer = 50f
                                }
                                2 -> { // Car Audio
                                    eqManager.applyPreset(4) // Rock
                                    loudness = 40f
                                    virtualizer = 30f
                                }
                                3 -> { // Studio Flat
                                    eqManager.applyPreset(0) // Flat
                                    bassBoost = 0f
                                    virtualizer = 0f
                                    loudness = 0f
                                }
                                4 -> { // Phone Speaker
                                    eqManager.applyPreset(2) // Vocal
                                    loudness = 80f
                                    bassBoost = 20f
                                }
                            }
                            for (i in 0 until 10) bandLevels[i] = eqManager.bandLevels[i]
                            eqManager.setBassBoostStrength((bassBoost * 10).toInt().toShort())
                            eqManager.setVirtualizerStrength((virtualizer * 10).toInt().toShort())
                            eqManager.setLoudnessGain((loudness * 10).toInt())
                        },
                        label = { Text(profile, fontSize = 12.sp) },
                        enabled = isEqEnabled,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Presets Chips
            Text(
                text = "PRESETS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                itemsIndexed(presets) { index, preset ->
                    FilterChip(
                        selected = selectedPreset == index,
                        onClick = {
                            selectedPreset = index
                            eqManager.applyPreset(index)
                            for (i in 0 until 10) {
                                bandLevels[i] = eqManager.bandLevels[i]
                            }
                            bassBoost = eqManager.bassStrength / 10f
                            virtualizer = eqManager.virtualizerStrength / 10f
                            loudness = eqManager.loudnessGain / 10f
                        },
                        label = { Text(preset) },
                        enabled = isEqEnabled,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 10-Band Graphic Equalizer
            Text(
                text = "Ecualizador de 10 Bandas",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        6.dp,
                        RoundedCornerShape(20.dp),
                        spotColor = themePrimary.copy(alpha = 0.25f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E2842), Color(0xFF101626))
                        )
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    bandFrequencies.forEachIndexed { index, freq ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = freq,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(62.dp)
                            )
                            Slider(
                                value = bandLevels[index],
                                onValueChange = { newVal ->
                                    bandLevels[index] = newVal
                                    eqManager.setBandLevel(index, newVal)
                                },
                                valueRange = -12f..12f,
                                enabled = isEqEnabled,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .semantics {
                                        contentDescription = "Ecualizador, banda de $freq"
                                        stateDescription = "${if (bandLevels[index] > 0) "+" else ""}${bandLevels[index].toInt()} decibelios"
                                    }
                            )
                            Text(
                                text = "${if (bandLevels[index] > 0) "+" else ""}${bandLevels[index].toInt()} dB",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.width(48.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cyber Theme Accent Picker
            Text(
                text = "TEMAS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E2842), Color(0xFF101626))
                        )
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Color de Acento", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    val accents = listOf(
                        Triple("MYSTIC_OBSIDIAN", "Misterio 🔮", Color(0xFFB388FF)),
                        Triple("MIDNIGHT_ECLIPSE", "Eclipse 🌑", Color(0xFFFF2A55)),
                        Triple("PHANTOM_SPECTER", "Espectro 👁️", Color(0xFF00FF9D)),
                        Triple("NEBULA_ENIGMA", "Nebulosa 🌌", Color(0xFFE040FB)),
                        Triple("AMOLED", "AMOLED 🖤", Color(0xFF38BDF8)),
                        Triple("CYBERPUNK", "Cyberpunk ⚡", Color(0xFF00F0FF)),
                        Triple("SUNSET_GOLD", "Sunset 👑", Color(0xFFFFD700)),
                        Triple("PURPLE", "Velvet 💜", Color(0xFFA855F7)),
                        Triple("CYAN", "Matrix 💎", Color(0xFF06B6D4)),
                        Triple("MAGENTA", "Magenta 🌸", Color(0xFFF43F5E)),
                        Triple("GREEN", "Emerald 🌲", Color(0xFF10B981))
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(accents) { _, (key, label, color) ->
                            val isSelected = currentAccent.equals(key, ignoreCase = true)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        com.auraplayer.audio.AuraHaptic.click(view)
                                        playlistManager.setThemeAccent(key)
                                        onAccentChange(key)
                                        android.widget.Toast.makeText(context, "Tema activado: $label", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key == "AMOLED") {
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 1: Efectos de Estudio & Inmersión

            // Sound Enhancement Rack (Bass Boost, 3D Virtualizer, Loudness)
            Text(
                text = "MEJORA ACÚSTICA",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E2842), Color(0xFF101626))
                        )
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Bass Boost
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Graves (Bass)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(text = "${bassBoost.toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Slider(
                            value = bassBoost,
                            onValueChange = {
                                bassBoost = it
                                eqManager.setBassBoostStrength((it * 10).toInt().toShort())
                            },
                            valueRange = 0f..100f,
                            enabled = isEqEnabled,
                            modifier = Modifier.semantics {
                                contentDescription = "Intensidad de graves"
                                stateDescription = "${bassBoost.toInt()} por ciento"
                            },
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.secondary, activeTrackColor = MaterialTheme.colorScheme.secondary)
                        )
                    }

                    // 3D Virtualizer
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.SurroundSound, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Virtualizador 3D", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Text(text = "${virtualizer.toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        }
                        Slider(
                            value = virtualizer,
                            onValueChange = {
                                virtualizer = it
                                eqManager.setVirtualizerStrength((it * 10).toInt().toShort())
                            },
                            valueRange = 0f..100f,
                            enabled = isEqEnabled,
                            modifier = Modifier.semantics {
                                contentDescription = "Virtualizador"
                                stateDescription = "${virtualizer.toInt()} por ciento"
                            },
                            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.tertiary, activeTrackColor = MaterialTheme.colorScheme.tertiary)
                        )
                    }

                    // Loudness Boost
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Sonoridad (Loudness)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                            Text(text = "${loudness.toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                        }
                        Slider(
                            value = loudness,
                            onValueChange = {
                                loudness = it
                                eqManager.setLoudnessGain((it * 10).toInt())
                            },
                            valueRange = 0f..100f,
                            enabled = isEqEnabled,
                            modifier = Modifier.semantics {
                                contentDescription = "Sonoridad"
                                stateDescription = "${loudness.toInt()} por ciento"
                            },
                            colors = SliderDefaults.colors(thumbColor = Color(0xFFF59E0B), activeTrackColor = Color(0xFFF59E0B))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Spatial 8D Audio Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        5.dp,
                        RoundedCornerShape(18.dp),
                        spotColor = if (isSpatial8D) Color(0xFF38BDF8).copy(alpha = 0.35f) else Color.Black
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            if (isSpatial8D) listOf(Color(0xFF16273C), Color(0xFF0C1622))
                            else listOf(Color(0xFF1A2234), Color(0xFF0F1420))
                        )
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(3.dp, CircleShape)
                                .clip(CircleShape)
                                .background(if (isSpatial8D) Color(0xFF38BDF8) else Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SurroundSound,
                                contentDescription = null,
                                tint = if (isSpatial8D) Color.Black else Color(0xFF94A3B8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Audio Espacial 8D",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSpatial8D) Color(0xFF38BDF8) else Color.White
                            )
                            Text(
                                text = if (isSpatial8D) "Envolvente 360°" else "Inmersión para audífonos",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isSpatial8D,
                        onCheckedChange = {
                            isSpatial8D = it
                            eqManager.setSpatial8DEnabled(it)
                            virtualizer = eqManager.virtualizerStrength / 10f
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF38BDF8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Karaoke Mode Card (Vocal Attenuation)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        5.dp,
                        RoundedCornerShape(18.dp),
                        spotColor = if (isKaraokeEnabled) Color(0xFFF43F5E).copy(alpha = 0.35f) else Color.Black
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.verticalGradient(
                            if (isKaraokeEnabled) listOf(Color(0xFF2A1522), Color(0xFF180D16))
                            else listOf(Color(0xFF1A2234), Color(0xFF0F1420))
                        )
                    ),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .shadow(3.dp, CircleShape)
                                .clip(CircleShape)
                                .background(if (isKaraokeEnabled) Color(0xFFF43F5E) else Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (isKaraokeEnabled) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Modo Karaoke",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isKaraokeEnabled) Color(0xFFF43F5E) else Color.White
                            )
                            Text(
                                text = if (isKaraokeEnabled) "Voz atenuada" else "Atenúa la voz principal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Switch(
                        checked = isKaraokeEnabled,
                        onCheckedChange = {
                            isKaraokeEnabled = it
                            eqManager.setKaraokeMode(it)
                            for (i in 0 until 10) bandLevels[i] = eqManager.bandLevels[i]
                            virtualizer = eqManager.virtualizerStrength / 10f
                            bassBoost = eqManager.bassStrength / 10f
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFF43F5E)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // DJ Crossfade Engine
            Text(
                text = "CROSSFADE & GAPLESS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E2842), Color(0xFF101626))
                        )
                    ),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Fundido Cruzado", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = if (crossfadeSeconds == 0) "Desactivado" else "${crossfadeSeconds}s entre pistas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (crossfadeSeconds == 0) "OFF" else "${crossfadeSeconds}s",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = crossfadeSeconds.toFloat(),
                        onValueChange = {
                            val sec = it.toInt()
                            crossfadeSeconds = sec
                            playlistManager.setCrossfadeSeconds(sec)
                        },
                        valueRange = 0f..12f,
                        steps = 11,
                        modifier = Modifier.semantics {
                            contentDescription = "Duración del fundido cruzado"
                            stateDescription = if (crossfadeSeconds == 0) "Desactivado" else "${crossfadeSeconds} segundos"
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ReplayGain Normalization
                    var isReplayGain by remember { mutableStateOf(playlistManager.isReplayGainEnabled()) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "ReplayGain", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Volumen uniforme sin saturación",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isReplayGain,
                            onCheckedChange = {
                                isReplayGain = it
                                playlistManager.setReplayGainEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Autoplay / Radio Mode
                    var isAutoplay by remember { mutableStateOf(playlistManager.isAutoplayEnabled()) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Autoplay", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Continúa temas similares al terminar",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAutoplay,
                            onCheckedChange = {
                                isAutoplay = it
                                playlistManager.setAutoplayEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
