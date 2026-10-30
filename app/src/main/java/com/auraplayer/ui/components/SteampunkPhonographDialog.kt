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
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * 🕰️ Fonógrafo Steampunk Victoriano (Aura 1890)
 * Reproductor de música acústico-mecánico del siglo XIX con:
 * - Campana/Trompeta de latón pulido con vibración física reactiva al subwoofer.
 * - Disco de pizarra de 78 RPM con surcos concéntricos finos tallados y aguja de rubí.
 * - Tren de engranajes mecánicos dentados que rotan de forma sincronizada.
 * - Manivela de cuerda táctil interactiva: arrastra en círculos para dar cuerda sintiendo los engranajes hápticos.
 * - Chasis de madera de caoba noble con remaches de bronce y palanca de freno vintage.
 */
@Composable
fun SteampunkPhonographDialog(
    currentMedia: MediaModel?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current

    // Nivel de energía de graves para la pulsación de la trompeta acústica
    var hornPulseEnergy by remember { mutableFloatStateOf(0.05f) }
    LaunchedEffect(isPlaying) {
        val visMgr = RealtimeVisualizerManager.instance
        while (true) {
            val target = if (isPlaying) visMgr.getBassEnergy(true) else 0.05f
            hornPulseEnergy = (hornPulseEnergy * 0.72f) + (target * 0.28f)
            delay(16)
        }
    }

    // Rotación del disco de pizarra a 78 RPM (~770ms por vuelta)
    val infiniteTransition = rememberInfiniteTransition(label = "phonograph_rotation")
    val recordRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(770, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "recordRotation"
    )

    // Manivela interactiva de cuerda
    var crankAngle by remember { mutableFloatStateOf(0f) }
    var windSpringLevel by remember { mutableFloatStateOf(1.0f) } // Cuerda de 0f a 1f

    // Pre-cache de degradados para regla de rendimiento 120 FPS
    val woodChassisBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF3B1D11), Color(0xFF26120A), Color(0xFF180A05))
        )
    }
    val brassGoldBrush = remember {
        Brush.horizontalGradient(
            listOf(Color(0xFFE5C158), Color(0xFFF9E89D), Color(0xFF9E782F))
        )
    }
    val darkRecordBrush = remember {
        Brush.radialGradient(
            listOf(Color(0xFF26262B), Color(0xFF131317), Color(0xFF09090C))
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xEE080504)),
            contentAlignment = Alignment.Center
        ) {
            // CHASIS DE MADERA DE CAOBA
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(woodChassisBrush)
                    .border(2.5.dp, brassGoldBrush, RoundedCornerShape(26.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // CABECERA VICTORIANA: PLACA DE BRONCE GRABADA & BOTÓN CERRAR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⚙️ AURA GRAMOPHONE No. 1890",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFFF9E89D),
                            letterSpacing = 1.sp
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
                            .background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFFF9E89D), modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // SECCIÓN SUPERIOR: TROMPETA ACÚSTICA DE LATÓN Y TREN DE ENGRANAJES
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(115.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // TROMPETA DE LATÓN (CON PULSACIÓN HIFI REACTIVA A GRAVES)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(
                            modifier = Modifier
                                .size(110.dp)
                                .graphicsLayer {
                                    val scale = 1f + (hornPulseEnergy * 0.12f)
                                    scaleX = scale
                                    scaleY = scale
                                }
                        ) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val maxRadius = size.width * 0.44f

                            // Cono exterior de latón
                            drawCircle(
                                color = Color(0xFF9E782F),
                                radius = maxRadius,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = Color(0xFFD4AF37),
                                radius = maxRadius * 0.86f,
                                center = Offset(cx, cy)
                            )
                            drawCircle(
                                color = Color(0xFFF5E08C),
                                radius = maxRadius * 0.68f,
                                center = Offset(cx, cy)
                            )

                            // Garganta acústica interior oscura
                            drawCircle(
                                color = Color(0xFF2E1906),
                                radius = maxRadius * 0.38f,
                                center = Offset(cx, cy)
                            )

                            // 12 Radios acampanados de la trompeta
                            for (r in 0 until 12) {
                                val angleRad = (r * 30f) * (PI / 180f).toFloat()
                                drawLine(
                                    color = Color(0xFF4A320F).copy(alpha = 0.7f),
                                    start = Offset(cx + cos(angleRad) * (maxRadius * 0.38f), cy + sin(angleRad) * (maxRadius * 0.38f)),
                                    end = Offset(cx + cos(angleRad) * maxRadius, cy + sin(angleRad) * maxRadius),
                                    strokeWidth = 1.4.dp.toPx()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // ENGRANAJES MECÁNICOS STEAMPUNK ROTATIVOS
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(115.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1C0D07))
                            .border(1.dp, Color(0xFF755523), RoundedCornerShape(12.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val curAngle = if (isPlaying) recordRotation else 0f

                            // Engranaje Mayor 1 (centro superior, rotación +1x)
                            drawGear(
                                center = Offset(w * 0.36f, h * 0.45f),
                                radius = 24.dp.toPx(),
                                teeth = 12,
                                rotationDeg = curAngle,
                                color = Color(0xFFD4AF37)
                            )

                            // Engranaje Secundario 2 (derecha, rotación contra-reloj -1.5x)
                            drawGear(
                                center = Offset(w * 0.74f, h * 0.62f),
                                radius = 17.dp.toPx(),
                                teeth = 8,
                                rotationDeg = -curAngle * 1.5f,
                                color = Color(0xFFB8860B)
                            )
                        }

                        Text(
                            text = "SPRING MOTOR",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFFD4AF37).copy(alpha = 0.8f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // DISCO DE PIZARRA DE 78 RPM CON AGUJA FONOCAPTORA
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .background(darkRecordBrush)
                        .border(2.dp, Color(0xFF473418), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(if (isPlaying) recordRotation else 0f)
                    ) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val r = size.width / 2f

                        // Surcos concéntricos del disco de pizarra
                        for (g in 5..18) {
                            val grooveRadius = r * (g / 20f)
                            drawCircle(
                                color = Color.White.copy(alpha = 0.04f + ((g % 3) * 0.02f)),
                                radius = grooveRadius,
                                center = Offset(cx, cy),
                                style = Stroke(width = 1.dp.toPx())
                            )
                        }

                        // Etiqueta central vintage Victoriana
                        drawCircle(
                            color = Color(0xFF6B1D1D), // Rojo lacre clásico
                            radius = r * 0.38f,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = Color(0xFFD4AF37),
                            radius = r * 0.38f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                        // Agujero del eje del tocadiscos
                        drawCircle(
                            color = Color(0xFF100804),
                            radius = 6.dp.toPx(),
                            center = Offset(cx, cy)
                        )
                    }

                    // Título de la pista grabado en la etiqueta central
                    Column(
                        modifier = Modifier
                            .width(100.dp)
                            .padding(4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentMedia?.title ?: "AURA 78 RPM",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFFF9E89D),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "78 RPM • SHELLAC",
                            fontSize = 7.5.sp,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFFE5C158),
                            textAlign = TextAlign.Center
                        )
                    }

                    // Aguja fonocaptora de rubí
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        // Brazo fonocaptor desde la esquina superior derecha
                        val armStart = Offset(w * 0.95f, h * 0.08f)
                        val needlePoint = Offset(w * 0.65f, h * 0.42f)

                        drawLine(
                            color = Color(0xFFD4AF37),
                            start = armStart,
                            end = needlePoint,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        // Cabezal con gema roja de rubí
                        drawCircle(
                            color = Color(0xFFEF4444),
                            radius = 4.dp.toPx(),
                            center = needlePoint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // MANIVELA TÁCTIL DE CUERDA INTERACTIVA
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E0F07))
                        .border(1.dp, Color(0xFF5A3E16), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MANIVELA DE CUERDA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Color(0xFFE5C158)
                        )
                        Text(
                            text = "Gira el pomo para dar cuerda a la fonola",
                            fontSize = 9.sp,
                            color = Color(0xFFA89578)
                        )
                    }

                    // Pomo interactivo de cuerda
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(brassGoldBrush)
                            .border(1.dp, Color(0xFFFFFFFF), CircleShape)
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val angle = atan2(dragAmount.y, dragAmount.x) * (180f / PI.toFloat())
                                    crankAngle += angle * 0.15f
                                    AuraHaptic.tick(view)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().rotate(crankAngle)) {
                            // Manivela con pomo de madera
                            drawLine(
                                color = Color(0xFF4A320F),
                                start = Offset(size.width / 2f, size.height / 2f),
                                end = Offset(size.width * 0.85f, size.height / 2f),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawCircle(
                                color = Color(0xFF3B1D11),
                                radius = 5.dp.toPx(),
                                center = Offset(size.width * 0.85f, size.height / 2f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CONTROLES DE REPRODUCCIÓN RETRO VICTORIANOS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            AuraHaptic.tick(view)
                            onPreviousClick()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF26120A))
                            .border(1.dp, Color(0xFF755523), CircleShape)
                    ) {
                        Icon(Icons.Default.SkipPrevious, null, tint = Color(0xFFF9E89D), modifier = Modifier.size(22.dp))
                    }

                    // PALANCA DE FRENO / REPRODUCCIÓN (PLAY/PAUSE)
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .shadow(10.dp, CircleShape, spotColor = Color(0xFFD4AF37))
                            .clip(CircleShape)
                            .background(brassGoldBrush)
                            .clickable {
                                AuraHaptic.heavy(view)
                                onPlayPauseClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Frenar" else "Accionar",
                            tint = Color(0xFF26120A),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            AuraHaptic.tick(view)
                            onNextClick()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF26120A))
                            .border(1.dp, Color(0xFF755523), CircleShape)
                    ) {
                        Icon(Icons.Default.SkipNext, null, tint = Color(0xFFF9E89D), modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

/**
 * Función auxiliar para dibujar un engranaje dentado mecánico victoriano en Canvas.
 */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGear(
    center: Offset,
    radius: Float,
    teeth: Int,
    rotationDeg: Float,
    color: Color
) {
    // Disco central del engranaje
    drawCircle(
        color = color,
        radius = radius * 0.78f,
        center = center
    )
    drawCircle(
        color = Color(0xFF100703),
        radius = radius * 0.28f,
        center = center
    )

    // Dientes salientes del engranaje
    for (t in 0 until teeth) {
        val angleDeg = rotationDeg + (t * (360f / teeth))
        val angleRad = angleDeg * (PI / 180f).toFloat()
        val r1 = radius * 0.72f
        val r2 = radius * 1.05f

        drawLine(
            color = color,
            start = Offset(center.x + cos(angleRad) * r1, center.y + sin(angleRad) * r1),
            end = Offset(center.x + cos(angleRad) * r2, center.y + sin(angleRad) * r2),
            strokeWidth = 2.4.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}
