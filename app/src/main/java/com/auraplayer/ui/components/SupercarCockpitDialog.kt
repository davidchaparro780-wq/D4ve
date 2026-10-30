package com.auraplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.audio.RealtimeVisualizerManager
import com.auraplayer.data.model.MediaModel
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 🏎️ Modo Supercar Cockpit (Dashboard Deportivo GT)
 * Cuadro de instrumentos digital de superdeportivo con:
 * - Tacómetro gigante analógico central de 9,000 RPM con aguja reactiva al subwoofer.
 * - Zona roja (Redline) con indicador shift-light parpadeante en drops de graves.
 * - Marcha engranada digital ("GEAR"), velocímetro digital en km/h y cronómetro de vuelta ("LAP TIME").
 * - Botón de encendido "ENGINE START / STOP" con halo incandescente.
 * - Levas de cambio F1 paddle shifters laterales para saltar de pista.
 */
@Composable
fun SupercarCockpitDialog(
    currentMedia: MediaModel?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current

    // Nivel de RPM reactivo a graves (1,000 RPM en ralentí hasta 9,000 RPM)
    var rpmEnergy by remember { mutableFloatStateOf(0.1f) }
    LaunchedEffect(isPlaying) {
        val visMgr = RealtimeVisualizerManager.instance
        while (true) {
            val target = if (isPlaying) visMgr.getBassEnergy(true) else 0.05f
            rpmEnergy = (rpmEnergy * 0.65f) + (target * 0.35f)
            delay(16)
        }
    }

    val currentRpm = (1000f + (rpmEnergy.coerceIn(0f, 1f) * 8000f)).toInt()
    val isRedline = currentRpm >= 7500 && isPlaying

    // Animación de destello shift light en redline
    val infiniteTransition = rememberInfiniteTransition(label = "shift_light_flash")
    val shiftFlash by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(140, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shiftFlash"
    )

    // Pre-cache de pinceles (Zero-allocation 120 FPS)
    val carbonFiberBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF14171D), Color(0xFF0C0E12), Color(0xFF060709))
        )
    }
    val titaniumRingBrush = remember {
        Brush.sweepGradient(
            listOf(Color(0xFF71717A), Color(0xFFE4E4E7), Color(0xFF52525B), Color(0xFFE4E4E7), Color(0xFF71717A))
        )
    }
    val startButtonBrush = remember(isPlaying) {
        if (isPlaying) {
            Brush.radialGradient(listOf(Color(0xFFEF4444), Color(0xFF991B1B)))
        } else {
            Brush.radialGradient(listOf(Color(0xFF22C55E), Color(0xFF15803D)))
        }
    }

    val lapTimeStr = remember(currentPositionMs, durationMs) {
        val curSec = currentPositionMs / 1000
        val durSec = durationMs / 1000
        String.format("%02d:%02d / %02d:%02d", curSec / 60, curSec % 60, durSec / 60, durSec % 60)
    }

    val speedKmh = remember(currentPositionMs, isPlaying) {
        if (!isPlaying) 0 else ((currentPositionMs / 500) % 240 + 60).toInt()
    }

    val gearStr = if (!isPlaying) "N" else when {
        speedKmh < 50 -> "1"
        speedKmh < 90 -> "2"
        speedKmh < 140 -> "3"
        speedKmh < 190 -> "4"
        speedKmh < 240 -> "5"
        else -> "6"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xEE040507)),
            contentAlignment = Alignment.Center
        ) {
            // PANEL PRINCIPAL DE INSTRUMENTACIÓN
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(carbonFiberBrush)
                    .border(2.dp, Color(0xFF27272A), RoundedCornerShape(26.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // CABECERA: MODULO TELEMÉTRICO & CERRAR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AURA GT CORSE • TELEMETRY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE4E4E7),
                            letterSpacing = 1.2.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            AuraHaptic.click(view)
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // SHIFT LIGHT BAR EN LA PARTE SUPERIOR DEL TACÓMETRO
                Row(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val shiftThresholds = listOf(0.4f, 0.55f, 0.70f, 0.82f, 0.90f)
                    shiftThresholds.forEachIndexed { idx, th ->
                        val isLit = rpmEnergy >= th && isPlaying
                        val dotColor = when {
                            idx >= 3 -> Color(0xFFEF4444) // Rojo
                            idx >= 1 -> Color(0xFFF59E0B) // Ámbar
                            else -> Color(0xFF10B981)     // Verde
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp, 6.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (isLit) dotColor else Color(0xFF27272A))
                                .border(0.5.dp, if (isLit) dotColor else Color(0x33FFFFFF), RoundedCornerShape(2.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // TACÓMETRO CENTRAL GIGANTE DE 9,000 RPM
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF090A0F))
                        .border(3.dp, titaniumRingBrush, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // DIBUJO DE ESCALA Y AGUJA ANALÓGICA
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val r = size.width * 0.44f

                        // Escala circular: de 135° (0 RPM) a 405° / 45° (9000 RPM), recorrido de 270 grados
                        val startAngle = 135f
                        val sweepTotal = 270f

                        // Dibuja marcas de 0 a 9 x 1000 RPM
                        for (tick in 0..9) {
                            val tickFraction = tick / 9f
                            val tickAngleDeg = startAngle + (tickFraction * sweepTotal)
                            val tickRad = tickAngleDeg * (PI / 180f).toFloat()

                            val isRedTick = tick >= 7
                            val tickColor = if (isRedTick) Color(0xFFEF4444) else Color(0xFFE4E4E7)
                            val tickLen = if (tick % 2 == 0) 12.dp.toPx() else 8.dp.toPx()

                            drawLine(
                                color = tickColor,
                                start = Offset(cx + cos(tickRad) * (r - tickLen), cy + sin(tickRad) * (r - tickLen)),
                                end = Offset(cx + cos(tickRad) * r, cy + sin(tickRad) * r),
                                strokeWidth = if (tick % 2 == 0) 2.2.dp.toPx() else 1.4.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }

                        // Zona roja arco (7k a 9k RPM)
                        val redStart = startAngle + (7f / 9f * sweepTotal)
                        val redSweep = (2f / 9f * sweepTotal)
                        drawArc(
                            color = Color(0xFFEF4444).copy(alpha = 0.5f),
                            startAngle = redStart,
                            sweepAngle = redSweep,
                            useCenter = false,
                            style = Stroke(width = 3.dp.toPx()),
                            topLeft = Offset(cx - r, cy - r),
                            size = androidx.compose.ui.geometry.Size(r * 2f, r * 2f)
                        )

                        // Aguja analógica de fibra de carbono con punta roja
                        val needleFraction = (currentRpm / 9000f).coerceIn(0f, 1f)
                        val needleAngleDeg = startAngle + (needleFraction * sweepTotal)
                        val needleRad = needleAngleDeg * (PI / 180f).toFloat()
                        val needleLen = r * 0.82f

                        val needleTipX = cx + cos(needleRad) * needleLen
                        val needleTipY = cy + sin(needleRad) * needleLen

                        drawLine(
                            color = if (isRedline) Color(0xFFEF4444) else Color(0xFFF97316),
                            start = Offset(cx, cy),
                            end = Offset(needleTipX, needleTipY),
                            strokeWidth = 2.8.dp.toPx(),
                            cap = StrokeCap.Round
                        )

                        // Cubre-eje central
                        drawCircle(
                            color = Color(0xFF18181B),
                            radius = 28.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = Color(0xFF3F3F46),
                            radius = 28.dp.toPx(),
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // DISPLAY DIGITAL INTERIOR: MARCHA & VELOCIDAD
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = gearStr,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isRedline) Color(0xFFEF4444) else Color.White
                        )
                        Text(
                            text = "$speedKmh KM/H",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF38BDF8)
                        )
                        Text(
                            text = "$currentRpm RPM",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = if (isRedline) Color(0xFFEF4444) else Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // TELEMETRÍA INFERIOR: PISTA & LAP TIME
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F1117))
                        .border(1.dp, Color(0xFF27272A), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentMedia?.title ?: "PIT LANE IDLE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = currentMedia?.artist ?: "AURA RACING",
                            fontSize = 10.sp,
                            color = Color(0xFFEF4444),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "LAP: $lapTimeStr",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // CONTROLES MOTORSPORT: PADDLE SHIFTERS & ENGINE START
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LEVA IZQUIERDA (PADDLE - / PREVIOUS)
                    PaddleShifterButton(
                        label = "◀ PADDLE -",
                        onClick = {
                            AuraHaptic.heavy(view)
                            onPreviousClick()
                        }
                    )

                    // BOTÓN CENTRAL DE ARRANQUE "ENGINE START / STOP"
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .shadow(16.dp, CircleShape, spotColor = if (isPlaying) Color(0xFFEF4444) else Color(0xFF22C55E))
                            .clip(CircleShape)
                            .background(startButtonBrush)
                            .border(2.5.dp, Color(0xFFE4E4E7), CircleShape)
                            .clickable {
                                AuraHaptic.heavy(view)
                                onPlayPauseClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ENGINE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = if (isPlaying) "STOP" else "START",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    // LEVA DERECHA (PADDLE + / NEXT)
                    PaddleShifterButton(
                        label = "PADDLE + ▶",
                        onClick = {
                            AuraHaptic.heavy(view)
                            onNextClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PaddleShifterButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(82.dp)
            .height(46.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E2028))
            .border(1.5.dp, Color(0xFF3F3F46), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFE4E4E7)
        )
    }
}
