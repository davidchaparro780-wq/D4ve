package com.auraplayer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.data.model.MediaModel
import kotlin.math.cos
import kotlin.math.sin

/**
 * 📻 Skin Vintage Hi-Fi: Vúmetros Analógicos de Aguja Mecánica.
 * Réplica de los amplificadores de bulbos de alta fidelidad de los años 70 (McIntosh / Marantz):
 * - Dos cuadrantes analógicos estéreo (Canal L y Canal R) iluminados en ámbar cálido.
 * - Agujas balísticas con amortiguación física de resorte que reaccionan a la energía del audio.
 * - Escala en dB (-20 dB a +3 dB) y diodo LED indicador de sobrecarga PEAK.
 * - Placa conmemorativa de aluminio cepillado con tipografía vintage.
 */
@Composable
fun AnalogVuMetersSkin(
    song: MediaModel?,
    isPlaying: Boolean,
    bassEnergy: Float,
    onTap: () -> Unit,
    onDoubleTap: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    // Simulación de energía balística independiente L y R con resortes físicos
    val targetLevelL = if (isPlaying) (0.20f + bassEnergy * 0.75f).coerceIn(0.05f, 1.0f) else 0.05f
    val targetLevelR = if (isPlaying) (0.18f + bassEnergy * 0.72f).coerceIn(0.05f, 1.0f) else 0.05f

    val needleLevelL by animateFloatAsState(
        targetValue = targetLevelL,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 280f),
        label = "needle_left"
    )
    val needleLevelR by animateFloatAsState(
        targetValue = targetLevelR,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = 260f),
        label = "needle_right"
    )

    // Chasis vintage con borde de aluminio y panel oscuro
    Box(
        modifier = modifier
            .shadow(18.dp, RoundedCornerShape(26.dp), spotColor = Color(0xFFD97706).copy(alpha = 0.35f))
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E2430),
                        Color(0xFF0F131C),
                        Color(0xFF070A0F)
                    )
                )
            )
            .border(2.dp, Brush.verticalGradient(listOf(Color(0xFF475569), Color(0xFF1E293B))), RoundedCornerShape(26.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        AuraHaptic.click(view)
                        onTap()
                    },
                    onDoubleTap = { offset ->
                        onDoubleTap(offset)
                    }
                )
            }
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placa superior de marca audiófila
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DaVE HI-FI LABORATORY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFF59E0B),
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )

                // Indicador LED PEAK de saturación
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isPeaking = isPlaying && bassEnergy > 0.80f
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .shadow(if (isPeaking) 6.dp else 0.dp, CircleShape, spotColor = Color(0xFFEF4444))
                            .background(if (isPeaking) Color(0xFFEF4444) else Color(0xFF451A1A), CircleShape)
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "PEAK",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPeaking) Color(0xFFEF4444) else Color(0xFF6B7280),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // DOS VÚMETROS ANALÓGICOS (L & R)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SingleVuMeterDial(
                    label = "LEFT CHANNEL",
                    level = needleLevelL,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                )
                SingleVuMeterDial(
                    label = "RIGHT CHANNEL",
                    level = needleLevelR,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                )
            }

            // Información inferior: Título + Especificación Hi-Fi
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = song?.title ?: "Sin reproducir",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song?.artist ?: "DaVE Player"} • DUAL MONO VU DECIBEL MONITOR",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8),
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Cuadrante individual de Vúmetro Analógico retro con arco y aguja mecánica.
 */
@Composable
private fun SingleVuMeterDial(
    label: String,
    level: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = Color(0xFFF59E0B).copy(alpha = 0.25f))
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.radialGradient(
                    listOf(
                        Color(0xFFFFFBEB), // Resplandor ámbar cálido
                        Color(0xFFFEF3C7),
                        Color(0xFFFDE68A),
                        Color(0xFFD97706).copy(alpha = 0.2f)
                    )
                )
            )
            .border(2.dp, Color(0xFF292524), RoundedCornerShape(14.dp))
            .padding(6.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@Canvas

            val pivotX = w / 2f
            val pivotY = h * 0.95f
            val radius = h * 0.80f

            // Arco normal (-20 a 0 dB en negro)
            val startAngle = 210f
            val sweepTotal = 120f
            val safeSweep = sweepTotal * 0.75f // Primeros 75% es zona segura
            val redSweep = sweepTotal * 0.25f  // Último 25% es zona roja (+1 a +3 dB)

            // Arco seguro
            drawArc(
                color = Color(0xFF44403C),
                startAngle = startAngle,
                sweepAngle = safeSweep,
                useCenter = false,
                topLeft = Offset(pivotX - radius, pivotY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Arco de sobrecarga rojo
            drawArc(
                color = Color(0xFFDC2626),
                startAngle = startAngle + safeSweep,
                sweepAngle = redSweep,
                useCenter = false,
                topLeft = Offset(pivotX - radius, pivotY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Marcas de división principales
            for (step in 0..6) {
                val frac = step / 6f
                val angleDeg = startAngle + (sweepTotal * frac)
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val innerR = radius - 8.dp.toPx()
                val outerR = radius + 2.dp.toPx()

                val p1 = Offset(
                    (pivotX + cos(angleRad) * innerR).toFloat(),
                    (pivotY + sin(angleRad) * innerR).toFloat()
                )
                val p2 = Offset(
                    (pivotX + cos(angleRad) * outerR).toFloat(),
                    (pivotY + sin(angleRad) * outerR).toFloat()
                )

                val markColor = if (frac > 0.75f) Color(0xFFDC2626) else Color(0xFF292524)
                drawLine(
                    color = markColor,
                    start = p1,
                    end = p2,
                    strokeWidth = if (step == 0 || step == 4 || step == 6) 2.5.dp.toPx() else 1.5.dp.toPx()
                )
            }

            // AGUJA MECÁNICA PIVOTANTE
            // level va de 0.0f a 1.0f
            val needleAngleDeg = startAngle + (sweepTotal * level.coerceIn(0f, 1f))
            val needleAngleRad = Math.toRadians(needleAngleDeg.toDouble())
            val needleLength = radius * 0.96f

            val needleTip = Offset(
                (pivotX + cos(needleAngleRad) * needleLength).toFloat(),
                (pivotY + sin(needleAngleRad) * needleLength).toFloat()
            )

            // Sombra tenue de la aguja
            drawLine(
                color = Color.Black.copy(alpha = 0.20f),
                start = Offset(pivotX + 2.dp.toPx(), pivotY + 2.dp.toPx()),
                end = Offset(needleTip.x + 2.dp.toPx(), needleTip.y + 2.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Aguja principal roja/negra de precisión
            drawLine(
                color = Color(0xFF1C1917),
                start = Offset(pivotX, pivotY),
                end = needleTip,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Punta roja de la aguja
            val redTipStart = Offset(
                (pivotX + cos(needleAngleRad) * (needleLength * 0.70f)).toFloat(),
                (pivotY + sin(needleAngleRad) * (needleLength * 0.70f)).toFloat()
            )
            drawLine(
                color = Color(0xFFDC2626),
                start = redTipStart,
                end = needleTip,
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Tornillo de pivote central
            drawCircle(
                color = Color(0xFF292524),
                radius = 6.dp.toPx(),
                center = Offset(pivotX, pivotY)
            )
            drawCircle(
                color = Color(0xFFD6D3D1),
                radius = 3.dp.toPx(),
                center = Offset(pivotX, pivotY)
            )
        }

        // Etiqueta del canal en la parte inferior
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
        ) {
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF44403C),
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.8.sp
            )
        }
    }
}
