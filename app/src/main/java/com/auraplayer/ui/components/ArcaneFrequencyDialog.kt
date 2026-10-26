package com.auraplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.audio.LunarPhaseManager

data class ArcaneFrequency(
    val hz: Int,
    val name: String,
    val subtitle: String,
    val rune: String,
    val color: Color
)

@Composable
fun ArcaneFrequencyDialog(
    currentHz: Int,
    onApplyFrequency: (hz: Int, pitchRatio: Float) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val lunarPhase = remember { LunarPhaseManager.getCurrentPhase() }

    val frequencies = listOf(
        ArcaneFrequency(
            hz = 432,
            name = "432 Hz: Resonancia Pitagorica",
            subtitle = "Alineacion natural del universo, claridad y calma profunda",
            rune = "ᛟ",
            color = Color(0xFFB388FF)
        ),
        ArcaneFrequency(
            hz = 528,
            name = "528 Hz: Frecuencia Milagrosa",
            subtitle = "Transformacion armonica, reparacion y vitalidad celular",
            rune = "ᚢ",
            color = Color(0xFF00FF9D)
        ),
        ArcaneFrequency(
            hz = 396,
            name = "396 Hz: Vacio Primordial",
            subtitle = "Liberacion de cargas emocionales y arraigo energetico",
            rune = "ᚱ",
            color = Color(0xFFFF2A55)
        ),
        ArcaneFrequency(
            hz = 639,
            name = "639 Hz: Conexion Aurica",
            subtitle = "Apertura del corazon, empatia y sintonia interpersonal",
            rune = "ᛏ",
            color = Color(0xFF00E5FF)
        ),
        ArcaneFrequency(
            hz = 741,
            name = "741 Hz: Despertar Intuitivo",
            subtitle = "Purificacion mental, creatividad y resolucion de enigmas",
            rune = "ᛈ",
            color = Color(0xFFE040FB)
        ),
        ArcaneFrequency(
            hz = 852,
            name = "852 Hz: Orden Espiritual",
            subtitle = "Vision trascendental y retorno al orden cosmico supremo",
            rune = "ᛉ",
            color = Color(0xFF38BDF8)
        ),
        ArcaneFrequency(
            hz = 440,
            name = "440 Hz: Afinacion Estandar",
            subtitle = "Concierto moderno ISO 16 sin alteracion de tono",
            rune = "ᚨ",
            color = Color(0xFF94A3B8)
        )
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 32.dp,
                    shape = RoundedCornerShape(28.dp),
                    spotColor = Color(0xFF7C3AED)
                )
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF130724), Color(0xFF0A0418), Color(0xFF07030C))
                    )
                )
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(Color(0xFF7C3AED), Color(0xFF00E5FF), Color(0xFF3D0B6E))
                    ),
                    RoundedCornerShape(28.dp)
                )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Consola de Resonancia Arcana",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Afinacion Cuantica y Frecuencias Solfeggio",
                            fontSize = 11.sp,
                            color = Color(0xFFB388FF)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Lunar phase card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x22B388FF))
                        .border(
                            0.8.dp,
                            Color(0x44B388FF),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = lunarPhase.phaseEmoji,
                            fontSize = 28.sp
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "${lunarPhase.phaseName} (${lunarPhase.illuminationPercent}%)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2D4FF)
                            )
                            Text(
                                text = "Runa ${lunarPhase.runeSymbol}",
                                fontSize = 11.sp,
                                color = Color(0xFF00E5FF)
                            )
                            Text(
                                text = "${lunarPhase.mysticAuraDescription}. Sugerencia: ${lunarPhase.recommendedFrequencyHz} Hz",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Seleccionar Frecuencia",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Frequency list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(frequencies) { freq ->
                        val isSelected = freq.hz == currentHz
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected)
                                        Brush.horizontalGradient(
                                            listOf(
                                                freq.color.copy(alpha = 0.22f),
                                                Color(0x11FFFFFF)
                                            )
                                        )
                                    else
                                        Brush.horizontalGradient(
                                            listOf(Color(0x0DFFFFFF), Color(0x06FFFFFF))
                                        )
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) freq.color.copy(alpha = 0.8f) else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    AuraHaptic.click(view)
                                    // Ratio real de re-afinación: la música se re-muestrea de 440 Hz a la frecuencia objetivo
                                    onApplyFrequency(freq.hz, freq.hz / 440f)
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Rune badge
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(freq.color.copy(alpha = 0.18f))
                                        .border(1.dp, freq.color.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = freq.rune,
                                        fontSize = 16.sp,
                                        color = freq.color
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = freq.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) freq.color else Color(0xFFE2E8F0)
                                    )
                                    Text(
                                        text = freq.subtitle,
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = freq.color,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3D0B6E)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Aplicar y Cerrar",
                        color = Color(0xFFE2D4FF),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
