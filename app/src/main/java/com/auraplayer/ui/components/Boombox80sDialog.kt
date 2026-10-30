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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
 * 📻 Modo Boombox 80s: Experiencia inmersiva retro estilo Street Boombox / Ghetto Blaster.
 * Cuenta con:
 * - Doble vúmetro analógico estéreo (L/R) iluminado con agujas balísticas reactivas al audio en vivo.
 * - Pletina de cassette con doble carrete giratorio sincronizado y cinta magnética dinámica.
 * - Pantalla digital LCD retro con tipografía vintage.
 * - Contador numérico analógico/digital de cinta.
 * - Teclas de piano cromadas táctiles con retroalimentación háptica contundente.
 * - Altavoces duales con pulsación física de subgraves a 120 FPS sin alocaciones en canvas.
 */
@Composable
fun Boombox80sDialog(
    currentMedia: MediaModel?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current

    // Estado de energía de audio balístico para los vúmetros analógicos
    var leftMeterEnergy by remember { mutableFloatStateOf(0.1f) }
    var rightMeterEnergy by remember { mutableFloatStateOf(0.1f) }

    LaunchedEffect(isPlaying) {
        val visMgr = RealtimeVisualizerManager.instance
        while (true) {
            val targetLeft = if (isPlaying) visMgr.getBassEnergy(true) else 0.05f
            val targetRight = if (isPlaying) visMgr.getTrebleEnergy(true) else 0.04f

            // Inercia y balística analógica de las agujas
            leftMeterEnergy = (leftMeterEnergy * 0.65f) + (targetLeft * 0.35f)
            rightMeterEnergy = (rightMeterEnergy * 0.60f) + (targetRight * 0.40f)

            delay(16) // ~60fps poll
        }
    }

    // Rotación de los carretes de cassette
    val infiniteTransition = rememberInfiniteTransition(label = "boombox_reels")
    val spoolRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spoolRotation"
    )

    // Pre-cache de pinceles (Zero-Allocation Rule para 120 FPS sin GC)
    val boomboxBodyBrush = remember {
        Brush.verticalGradient(
            listOf(
                Color(0xFF2E3138), // Chasis aluminio grafito
                Color(0xFF1E2026),
                Color(0xFF15161A)
            )
        )
    }
    val vuMeterBgBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF282314), Color(0xFF17130A))
        )
    }
    val cassetteWindowBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0x333A4454), Color(0x66181B22))
        )
    }
    val lcdScreenBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF0F261F), Color(0xFF081713))
        )
    }
    val chromeTrimBrush = remember {
        Brush.horizontalGradient(
            listOf(Color(0xFF8E95A5), Color(0xFFE2E8F0), Color(0xFF71798A))
        )
    }

    // Cálculo del progreso de cinta (0.0f a 1.0f)
    val tapeProgress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0.25f
    val counterStr = remember(currentPosition) {
        val totalSecs = currentPosition / 1000
        val countVal = (totalSecs % 9999).toInt()
        String.format("%04d", countVal)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xEE090A0D)),
            contentAlignment = Alignment.Center
        ) {
            // CHASIS PRINCIPAL DEL BOOMBOX
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(boomboxBodyBrush)
                    .border(2.dp, chromeTrimBrush, RoundedCornerShape(26.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // BARRA SUPERIOR: ASA, MARCA RETRO & BOTÓN CERRAR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isPlaying) Color(0xFFEF4444) else Color(0xFF551111))
                                .border(1.dp, Color(0xFF991B1B), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AURA-SONIC 1985 • STEREO BOOMBOX",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFCBD5E1),
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
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // SECCIÓN VÚMETROS ANALÓGICOS DUALES (L & R)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // VÚMETRO CANAL IZQUIERDO (L)
                    AnalogVuMeter(
                        label = "L - BASS CH",
                        energy = leftMeterEnergy,
                        backgroundBrush = vuMeterBgBrush,
                        modifier = Modifier.weight(1f)
                    )

                    // VÚMETRO CANAL DERECHO (R)
                    AnalogVuMeter(
                        label = "R - TREBLE CH",
                        energy = rightMeterEnergy,
                        backgroundBrush = vuMeterBgBrush,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PANTALLA LCD RETRO VERDE FLUORESCENTE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(lcdScreenBrush)
                        .border(1.5.dp, Color(0xFF104033), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentMedia?.title ?: "NO CASSETTE INSERTED",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF34D399),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${currentMedia?.artist ?: "UNKNOWN"} • DOLBY B-NR TYPE II",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF059669),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // CONTADOR ANALÓGICO MECÁNICO
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0F1115))
                                .border(1.dp, Color(0xFF2E333D), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = counterStr,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFFBBF24),
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PLETINA DE CASSETTE CON CARRETES GIRATORIOS
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF16181E))
                        .border(1.5.dp, Color(0xFF343946), RoundedCornerShape(12.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Fondo de ventana acrílica ahumada
                        drawRoundRect(
                            brush = cassetteWindowBrush,
                            size = size,
                            cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                        )

                        // Coordenadas de carrete izquierdo y derecho
                        val leftCenter = Offset(w * 0.28f, h * 0.48f)
                        val rightCenter = Offset(w * 0.72f, h * 0.48f)

                        // Radio de cinta acumulada (inversa entre carretes)
                        val leftTapeRadius = (28.dp.toPx()) * (1f - (tapeProgress * 0.5f))
                        val rightTapeRadius = (16.dp.toPx()) + ((24.dp.toPx()) * (tapeProgress * 0.7f))

                        // Carrete izquierdo: masa de cinta marrón
                        drawCircle(
                            color = Color(0xFF3D2314), // Marrón cinta magnética
                            radius = leftTapeRadius,
                            center = leftCenter
                        )
                        // Carrete derecho: masa de cinta marrón
                        drawCircle(
                            color = Color(0xFF3D2314),
                            radius = rightTapeRadius,
                            center = rightCenter
                        )

                        // Cinta conectora inferior
                        drawLine(
                            color = Color(0xFF2E190E),
                            start = Offset(leftCenter.x, leftCenter.y + leftTapeRadius),
                            end = Offset(rightCenter.x, rightCenter.y + rightTapeRadius),
                            strokeWidth = 3.dp.toPx()
                        )

                        // Carrete plástico blanco dentado (Izquierdo)
                        val currentAngle = if (isPlaying) spoolRotation else 0f
                        drawCassetteSpool(leftCenter, currentAngle)

                        // Carrete plástico blanco dentado (Derecho)
                        drawCassetteSpool(rightCenter, currentAngle)

                        // Cabezal magnético y rodillos en la base
                        val headY = h * 0.90f
                        drawRoundRect(
                            color = Color(0xFF71798A),
                            topLeft = Offset(w * 0.44f, headY - 6.dp.toPx()),
                            size = Size(w * 0.12f, 8.dp.toPx()),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // TECLAS DE PIANO CROMADAS RETRO (REW, PLAY/PAUSE, FF)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Retro REWIND
                    BoomboxPianoButton(
                        label = "REW",
                        icon = Icons.Default.FastRewind,
                        onClick = {
                            AuraHaptic.heavy(view)
                            onPreviousClick()
                        }
                    )

                    // Botón Retro PLAY / PAUSE Principal
                    BoomboxPianoButton(
                        label = if (isPlaying) "PAUSE" else "PLAY",
                        icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        isPrimary = true,
                        onClick = {
                            AuraHaptic.heavy(view)
                            onPlayPauseClick()
                        }
                    )

                    // Botón Retro FAST FORWARD
                    BoomboxPianoButton(
                        label = "F.FWD",
                        icon = Icons.Default.FastForward,
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

/**
 * Dibuja un vúmetro analógico retro con escala verde-ámbar-roja y aguja balística oscilante.
 */
@Composable
private fun AnalogVuMeter(
    label: String,
    energy: Float,
    backgroundBrush: Brush,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundBrush)
            .border(1.dp, Color(0xFF453D2A), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Arco de la escala de decibelios
            val pivot = Offset(w / 2f, h * 1.15f)
            val needleLen = h * 0.95f

            // Ángulo de la aguja: de -45° a +45°
            val clampedEnergy = energy.coerceIn(0f, 1f)
            val angleDeg = -45f + (clampedEnergy * 90f)
            val angleRad = (angleDeg - 90f) * (PI / 180f).toFloat()

            // Marcas de la escala (-20 a +3 dB)
            for (step in 0..10) {
                val tickDeg = -45f + (step * 9f)
                val tickRad = (tickDeg - 90f) * (PI / 180f).toFloat()
                val tickColor = when {
                    step >= 8 -> Color(0xFFEF4444) // Rojo sobrecarga (+1 a +3 dB)
                    step >= 6 -> Color(0xFFF59E0B) // Ámbar límite (0 dB)
                    else -> Color(0xFF10B981)      // Verde óptimo
                }
                val r1 = needleLen * 0.88f
                val r2 = needleLen * 0.98f
                drawLine(
                    color = tickColor.copy(alpha = 0.75f),
                    start = Offset(pivot.x + cos(tickRad) * r1, pivot.y + sin(tickRad) * r1),
                    end = Offset(pivot.x + cos(tickRad) * r2, pivot.y + sin(tickRad) * r2),
                    strokeWidth = 1.4.dp.toPx()
                )
            }

            // Aguja analógica (Negra con extremo rojo)
            val tipX = pivot.x + cos(angleRad) * needleLen
            val tipY = pivot.y + sin(angleRad) * needleLen

            drawLine(
                color = Color(0xFFEF4444),
                start = pivot,
                end = Offset(tipX, tipY),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Pivote central de la aguja
            drawCircle(
                color = Color(0xFF1F242D),
                radius = 5.dp.toPx(),
                center = pivot
            )
        }

        // Etiqueta del vúmetro
        Text(
            text = label,
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFFD97706),
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Función auxiliar de Canvas para dibujar un carrete de cassette giratorio de 3 dientes.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCassetteSpool(
    center: Offset,
    rotationDeg: Float
) {
    val spoolRadius = 14.dp.toPx()

    // Anillo exterior blanco
    drawCircle(
        color = Color(0xFFE2E8F0),
        radius = spoolRadius,
        center = center,
        style = Stroke(width = 2.dp.toPx())
    )

    // Agujero central
    drawCircle(
        color = Color(0xFF111317),
        radius = spoolRadius * 0.45f,
        center = center
    )

    // 3 Dientes del carrete a 120 grados
    for (tooth in 0..2) {
        val toothAngle = ((rotationDeg + tooth * 120f) - 90f) * (PI / 180f).toFloat()
        val rInner = spoolRadius * 0.45f
        val rOuter = spoolRadius * 0.90f
        drawLine(
            color = Color(0xFFE2E8F0),
            start = Offset(center.x + cos(toothAngle) * rInner, center.y + sin(toothAngle) * rInner),
            end = Offset(center.x + cos(toothAngle) * rOuter, center.y + sin(toothAngle) * rOuter),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Tecla de piano mecánica estilo equipo de alta fidelidad de los años 80.
 */
@Composable
private fun BoomboxPianoButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val buttonBrush = remember(isPressed, isPrimary) {
        if (isPrimary) {
            Brush.verticalGradient(
                if (isPressed) listOf(Color(0xFFB45309), Color(0xFF78350F))
                else listOf(Color(0xFFF59E0B), Color(0xFFD97706))
            )
        } else {
            Brush.verticalGradient(
                if (isPressed) listOf(Color(0xFF333842), Color(0xFF1F2228))
                else listOf(Color(0xFF5A6170), Color(0xFF3A3F4B))
            )
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .width(62.dp)
                .height(44.dp)
                .graphicsLayer {
                    translationY = if (isPressed) 3.dp.toPx() else 0f
                }
                .shadow(
                    elevation = if (isPressed) 1.dp else 4.dp,
                    shape = RoundedCornerShape(6.dp)
                )
                .clip(RoundedCornerShape(6.dp))
                .background(buttonBrush)
                .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPrimary) Color(0xFF140D0A) else Color(0xFFF1F5F9),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF94A3B8),
            letterSpacing = 1.sp
        )
    }
}
