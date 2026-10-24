package com.auraplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.RestartAlt
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.auraplayer.audio.EqualizerManager
import com.auraplayer.data.repository.PlaylistManager

@Composable
fun QuickEqDialog(
    onDismiss: () -> Unit
) {
    val eq = remember { EqualizerManager.instance }

    val context = LocalContext.current
    val playlistManager = remember { PlaylistManager(context) }
    var crossfadeSec by remember { mutableIntStateOf(playlistManager.getCrossfadeSeconds()) }
    var is8D by remember { mutableStateOf(eq.isSpatial8DEnabled) }
    var isKaraoke by remember { mutableStateOf(eq.isKaraokeMode) }

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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Bass Boost
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

                // 2. Virtualizer / Soundstage
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

                // 3. Loudness Boost
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

                HorizontalDivider(color = Color(0xFF1E293B), thickness = 1.dp)

                // 4. Fundido Cruzado DJ (Crossfade)
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
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0 to "OFF", 3 to "3s", 5 to "5s", 8 to "8s", 12 to "12s").forEach { (sec, label) ->
                            val isSelected = crossfadeSec == sec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Color(0xFF10B981) else Color(0xFF161E36))
                                    .border(1.dp, if (isSelected) Color(0xFF10B981) else Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .clickable {
                                        crossfadeSec = sec
                                        playlistManager.setCrossfadeSeconds(sec)
                                    }
                                    .padding(vertical = 6.dp),
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

                // 5. Audio Espacial 8D
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Audio Espacial 8D 🌀", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Efecto envolvente 360°", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
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

                // 6. Modo Karaoke (Vocal Remover DSP)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Modo Karaoke (Atenuar Voz) 🎤", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Atenúa la voz central para cantar", fontSize = 10.sp, color = Color(0xFF64748B))
                    }
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
                        eq.setBassBoostStrength(0)
                        eq.setVirtualizerStrength(0)
                        eq.setLoudnessGain(0)
                        playlistManager.setCrossfadeSeconds(0)
                        eq.setSpatial8DEnabled(false)
                        eq.setKaraokeMode(false)
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
