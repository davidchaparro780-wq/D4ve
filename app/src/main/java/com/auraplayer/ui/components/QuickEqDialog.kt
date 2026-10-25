package com.auraplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.PlaybackParameters
import com.auraplayer.audio.EqualizerManager
import com.auraplayer.audio.LoudnessNormalizerManager
import com.auraplayer.audio.SilenceTrimmerManager
import com.auraplayer.data.repository.PlaylistManager

@Composable
fun QuickEqDialog(
    onDismiss: () -> Unit
) {
    val eq = remember { EqualizerManager.instance }

    val context = LocalContext.current
    val playlistManager = remember { PlaylistManager(context) }
    val loudnessManager = remember { LoudnessNormalizerManager.getInstance(context) }
    val silenceManager = remember { SilenceTrimmerManager.getInstance(context) }

    val initialSpeed = com.auraplayer.service.PlaybackService.activePlayer?.playbackParameters?.speed ?: 1.0f
    var activeDjEffect by remember {
        mutableIntStateOf(
            when {
                initialSpeed < 0.90f -> 1 // Slowed
                initialSpeed > 1.10f -> 2 // Nightcore
                else -> 0 // Original
            }
        )
    }

    var crossfadeSec by remember { mutableIntStateOf(playlistManager.getCrossfadeSeconds()) }
    var is8D by remember { mutableStateOf(eq.isSpatial8DEnabled) }
    var isKaraoke by remember { mutableStateOf(eq.isKaraokeMode) }
    var isLoudnessNormalized by remember { mutableStateOf(loudnessManager.isEnabled) }
    var isSilenceTrimmed by remember { mutableStateOf(silenceManager.isEnabled) }

    // Read current values
    var bassBoost by remember { mutableFloatStateOf(eq.bassStrength.toFloat()) }
    var virtualizer by remember { mutableFloatStateOf(eq.virtualizerStrength.toFloat()) }
    var loudness by remember { mutableFloatStateOf(eq.loudnessGain.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0C101D),
        shape = RoundedCornerShape(26.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Equalizer, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Consola DJ & Ecualizador 🎚️", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1, 2, 3: Sliders Console Tile
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1B2338), Color(0xFF101524))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EqSliderRow(
                            label = "Graves & Sub-Bajos 🔊",
                            value = bassBoost,
                            valueRange = 0f..1000f,
                            color = Color(0xFF8B5CF6),
                            onValueChange = {
                                bassBoost = it
                                eq.setBassBoostStrength(it.toInt().toShort())
                            }
                        )
                        EqSliderRow(
                            label = "Espacio Sonoro 3D 🌌",
                            value = virtualizer,
                            valueRange = 0f..1000f,
                            color = Color(0xFF00F0FF),
                            onValueChange = {
                                virtualizer = it
                                eq.setVirtualizerStrength(it.toInt().toShort())
                            }
                        )
                        EqSliderRow(
                            label = "Potencia / Brillo ⚡",
                            value = loudness,
                            valueRange = 0f..1000f,
                            color = Color(0xFFFFD700),
                            onValueChange = {
                                loudness = it
                                eq.setLoudnessGain(it.toInt())
                            }
                        )
                    }
                }

                // 3.5. Efectos Virales 1-Tap DJ (Slowed + Reverb / Nightcore / Hi-Fi)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1B2338), Color(0xFF101524))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Efectos Virales & Acústica DJ 🎛️", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                text = when (activeDjEffect) {
                                    1 -> "🐢 SLOWED"
                                    2 -> "⚡ NIGHTCORE"
                                    3 -> "🗣️ VOZ CLARA"
                                    4 -> "🏛️ CONCIERTO"
                                    else -> "🎵 ORIGINAL"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (activeDjEffect) {
                                    1 -> Color(0xFF8B5CF6)
                                    2 -> Color(0xFFEC4899)
                                    3 -> Color(0xFF10B981)
                                    4 -> Color(0xFF00F0FF)
                                    else -> Color(0xFF38BDF8)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val effects = listOf(
                                Triple(0, "🎵 Original", "1.0x Hi-Fi"),
                                Triple(1, "🐢 Slowed", "0.85x + Reverb"),
                                Triple(2, "⚡ Nightcore", "1.25x Sped Up"),
                                Triple(3, "🗣️ Voz Clara", "Vocal Boost"),
                                Triple(4, "🏛️ Concierto", "3D Space")
                            )
                            items(effects) { (modeIdx, label, sub) ->
                                val isSelected = activeDjEffect == modeIdx
                                Box(
                                    modifier = Modifier
                                        .shadow(if (isSelected) 4.dp else 2.dp, RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) {
                                                when (modeIdx) {
                                                    1 -> Brush.verticalGradient(listOf(Color(0xFF7C3AED), Color(0xFF6D28D9)))
                                                    2 -> Brush.verticalGradient(listOf(Color(0xFFDB2777), Color(0xFFBE185D)))
                                                    3 -> Brush.verticalGradient(listOf(Color(0xFF059669), Color(0xFF047857)))
                                                    4 -> Brush.verticalGradient(listOf(Color(0xFF0891B2), Color(0xFF0E7490)))
                                                    else -> Brush.verticalGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1)))
                                                }
                                            } else {
                                                Brush.verticalGradient(listOf(Color(0xFF222B42), Color(0xFF141926)))
                                            }
                                        )
                                        .clickable {
                                            activeDjEffect = modeIdx
                                            when (modeIdx) {
                                                1 -> {
                                                    // Slowed + Reverb
                                                    com.auraplayer.service.PlaybackService.activePlayer?.playbackParameters =
                                                        PlaybackParameters(0.85f, 0.85f)
                                                    eq.setBassBoostStrength(450)
                                                    eq.setVirtualizerStrength(550)
                                                    bassBoost = 450f
                                                    virtualizer = 550f
                                                }
                                                2 -> {
                                                    // Nightcore
                                                    com.auraplayer.service.PlaybackService.activePlayer?.playbackParameters =
                                                        PlaybackParameters(1.25f, 1.25f)
                                                    eq.setBassBoostStrength(200)
                                                    eq.setLoudnessGain(350)
                                                    bassBoost = 200f
                                                    loudness = 350f
                                                }
                                                3 -> {
                                                    // Voz Clara (Vocal Boost)
                                                    com.auraplayer.service.PlaybackService.activePlayer?.playbackParameters =
                                                        PlaybackParameters(1.0f, 1.0f)
                                                    eq.setBassBoostStrength(60)
                                                    eq.setVirtualizerStrength(180)
                                                    eq.setLoudnessGain(420)
                                                    bassBoost = 60f
                                                    virtualizer = 180f
                                                    loudness = 420f
                                                }
                                                4 -> {
                                                    // Concierto 3D Reverb Space
                                                    com.auraplayer.service.PlaybackService.activePlayer?.playbackParameters =
                                                        PlaybackParameters(1.0f, 1.0f)
                                                    eq.setBassBoostStrength(320)
                                                    eq.setVirtualizerStrength(850)
                                                    eq.setLoudnessGain(150)
                                                    bassBoost = 320f
                                                    virtualizer = 850f
                                                    loudness = 150f
                                                }
                                                else -> {
                                                    // Original
                                                    com.auraplayer.service.PlaybackService.activePlayer?.playbackParameters =
                                                        PlaybackParameters(1.0f, 1.0f)
                                                    eq.setBassBoostStrength(0)
                                                    eq.setVirtualizerStrength(0)
                                                    eq.setLoudnessGain(0)
                                                    bassBoost = 0f
                                                    virtualizer = 0f
                                                    loudness = 0f
                                                }
                                            }
                                        }
                                        .padding(vertical = 7.dp, horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = label,
                                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                            fontSize = 10.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = sub,
                                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFF64748B),
                                            fontSize = 8.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Fundido Cruzado DJ (Crossfade) in 3D Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1B2338), Color(0xFF101524))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mezcla DJ / Crossfade 🎧", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                text = if (crossfadeSec == 0) "OFF" else "${crossfadeSec}s",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(0 to "OFF", 3 to "3s", 5 to "5s", 8 to "8s", 12 to "12s").forEach { (sec, label) ->
                                val isSelected = crossfadeSec == sec
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(if (isSelected) 4.dp else 2.dp, RoundedCornerShape(8.dp))
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isSelected) Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
                                            else Brush.verticalGradient(listOf(Color(0xFF222B42), Color(0xFF141926)))
                                        )
                                        .clickable {
                                            crossfadeSec = sec
                                            playlistManager.setCrossfadeSeconds(sec)
                                        }
                                        .padding(vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Audio Espacial 8D in 3D Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1B2338), Color(0xFF101524))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Audio Espacial 8D 🌀", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Switch(
                            checked = is8D,
                            onCheckedChange = {
                                is8D = it
                                eq.setSpatial8DEnabled(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF38BDF8)
                            )
                        )
                    }
                }

                // 6. Modo Karaoke (Vocal Remover DSP) in 3D Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1B2338), Color(0xFF101524))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Modo Karaoke (Atenuar Voz) 🎤", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Switch(
                            checked = isKaraoke,
                            onCheckedChange = {
                                isKaraoke = it
                                eq.setKaraokeMode(it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFEC4899)
                            )
                        )
                    }
                }

                // 7. Normalizador de Volumen Inteligente (-14 LUFS)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1B2338), Color(0xFF101524))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Normalizador de Volumen 🔊", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Nivela a -14 LUFS para evitar saltos de volumen", fontSize = 10.5.sp, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = isLoudnessNormalized,
                            onCheckedChange = {
                                isLoudnessNormalized = it
                                loudnessManager.isEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF10B981)
                            )
                        )
                    }
                }

                // 8. Recorte Inteligente de Silencios
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color.Black.copy(alpha = 0.5f))
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF1B2338), Color(0xFF101524))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                            Text("Recorte de Silencios ✂️", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Salta intros y outros inaudibles", fontSize = 10.5.sp, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = isSilenceTrimmed,
                            onCheckedChange = {
                                isSilenceTrimmed = it
                                silenceManager.isEnabled = it
                                com.auraplayer.service.PlaybackService.activePlayer?.skipSilenceEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFFF59E0B)
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        bassBoost = 0f
                        virtualizer = 0f
                        loudness = 0f
                        crossfadeSec = 0
                        is8D = false
                        isKaraoke = false
                        isLoudnessNormalized = false
                        isSilenceTrimmed = false
                        activeDjEffect = 0
                        eq.setBassBoostStrength(0)
                        eq.setVirtualizerStrength(0)
                        eq.setLoudnessGain(0)
                        playlistManager.setCrossfadeSeconds(0)
                        eq.setSpatial8DEnabled(false)
                        eq.setKaraokeMode(false)
                        loudnessManager.isEnabled = false
                        silenceManager.isEnabled = false
                        com.auraplayer.service.PlaybackService.activePlayer?.skipSilenceEnabled = false
                        com.auraplayer.service.PlaybackService.activePlayer?.playbackParameters = PlaybackParameters(1.0f, 1.0f)
                    }
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reiniciar", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    Text("Listo", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    )
}

@Composable
private fun EqSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    color: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            val percent = ((value / valueRange.endInclusive) * 100).toInt()
            Text("$percent%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = Color(0xFF1E293B)
            )
        )
    }
}
