package com.auraplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
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
import kotlin.math.sin

/**
 * 🕯️ Santuario Nocturno (Llama Viva Contemplativa)
 * Experiencia zen de meditación y descanso nocturno con:
 * - Llama de vela procedural viva que danza, oscila y titila al compás de la música.
 * - Resplandor cálido difuso y humo sutil ascendente en penumbra.
 * - Modo contemplativo limpio: la interfaz se atenúa automáticamente para evitar fatiga visual.
 * - Al tocar la pantalla, despiertan suavemente los controles cálidos de reproducción.
 */
@Composable
fun AuraSanctuaryDialog(
    currentMedia: MediaModel?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    var areControlsVisible by remember { mutableStateOf(true) }

    // Ocultar controles automáticamente tras 4 segundos para inmersión zen
    LaunchedEffect(areControlsVisible) {
        if (areControlsVisible) {
            delay(4000)
            areControlsVisible = false
        }
    }

    // Nivel de energía de graves para el parpadeo de la llama
    var flameBassEnergy by remember { mutableFloatStateOf(0.05f) }
    LaunchedEffect(isPlaying) {
        val visMgr = RealtimeVisualizerManager.instance
        while (true) {
            val target = if (isPlaying) visMgr.getBassEnergy(true) else 0.05f
            flameBassEnergy = (flameBassEnergy * 0.70f) + (target * 0.30f)
            delay(16)
        }
    }

    // Animación continua del oscilar del fuego
    val infiniteTransition = rememberInfiniteTransition(label = "flame_flicker")
    val flamePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flamePhase"
    )

    // Pre-cache de degradados para 120 FPS sin alocaciones
    val sanctuaryBgBrush = remember {
        Brush.radialGradient(
            listOf(Color(0xFF261506), Color(0xFF140902), Color(0xFF070301))
        )
    }
    val candleBodyBrush = remember {
        Brush.horizontalGradient(
            listOf(Color(0xFFE2D4BE), Color(0xFFFFFBEB), Color(0xFFD6C5A9))
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(sanctuaryBgBrush)
                .pointerInput(Unit) {
                    detectTapGestures {
                        AuraHaptic.click(view)
                        areControlsVisible = !areControlsVisible
                    }
                }
        ) {
            // LLAMA VIVA PROCEDURAL & VELA EN CANVAS
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h * 0.52f

                // 1. Resplandor cálido ambiental detrás de la llama
                val auraRadius = (90.dp.toPx()) * (1f + flameBassEnergy * 0.35f)
                drawCircle(
                    color = Color(0xFFF59E0B).copy(alpha = 0.14f + flameBassEnergy * 0.12f),
                    radius = auraRadius * 1.5f,
                    center = Offset(cx, cy - 40.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFFFDE68A).copy(alpha = 0.20f + flameBassEnergy * 0.18f),
                    radius = auraRadius * 0.8f,
                    center = Offset(cx, cy - 40.dp.toPx())
                )

                // 2. Humo tenue ascendente
                val smokePath = Path()
                smokePath.moveTo(cx, cy - 80.dp.toPx())
                for (s in 1..12) {
                    val progress = s / 12f
                    val sy = cy - 80.dp.toPx() - (progress * 130.dp.toPx())
                    val sx = cx + sin(flamePhase * 1.5f + progress * 4f).toFloat() * (progress * 24.dp.toPx())
                    smokePath.lineTo(sx, sy)
                }
                drawPath(
                    path = smokePath,
                    color = Color.White.copy(alpha = 0.08f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )

                // 3. Mecha negra de la vela
                drawLine(
                    color = Color(0xFF1E1711),
                    start = Offset(cx, cy),
                    end = Offset(cx, cy - 18.dp.toPx()),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // 4. Llama viva con oscilación sinusoidal
                val flickerWobble = sin(flamePhase * 3f).toFloat() * 3.dp.toPx()
                val flameHeight = (42.dp.toPx()) * (1f + flameBassEnergy * 0.25f)
                val flameWidth = (16.dp.toPx()) * (1f + flameBassEnergy * 0.12f)
                val flameTip = Offset(cx + flickerWobble, cy - 18.dp.toPx() - flameHeight)

                // Capa exterior anaranjada de la llama
                val outerFlame = Path()
                outerFlame.moveTo(cx - flameWidth, cy - 16.dp.toPx())
                outerFlame.quadraticTo(
                    cx - flameWidth * 1.2f, cy - 18.dp.toPx() - (flameHeight * 0.45f),
                    flameTip.x, flameTip.y
                )
                outerFlame.quadraticTo(
                    cx + flameWidth * 1.2f, cy - 18.dp.toPx() - (flameHeight * 0.45f),
                    cx + flameWidth, cy - 16.dp.toPx()
                )
                outerFlame.close()
                drawPath(outerFlame, Color(0xFFF97316).copy(alpha = 0.85f))

                // Núcleo amarillo brillante de la llama
                val innerFlame = Path()
                innerFlame.moveTo(cx - flameWidth * 0.55f, cy - 16.dp.toPx())
                innerFlame.quadraticTo(
                    cx - flameWidth * 0.7f, cy - 18.dp.toPx() - (flameHeight * 0.35f),
                    flameTip.x * 0.3f + cx * 0.7f, cy - 18.dp.toPx() - (flameHeight * 0.75f)
                )
                innerFlame.quadraticTo(
                    cx + flameWidth * 0.7f, cy - 18.dp.toPx() - (flameHeight * 0.35f),
                    cx + flameWidth * 0.55f, cy - 16.dp.toPx()
                )
                innerFlame.close()
                drawPath(innerFlame, Color(0xFFFEF08A))

                // Núcleo azul de combustión en la base de la mecha
                drawCircle(
                    color = Color(0xFF38BDF8).copy(alpha = 0.75f),
                    radius = 3.5.dp.toPx(),
                    center = Offset(cx, cy - 15.dp.toPx())
                )

                // 5. Cuerpo de la vela cilíndrica de cera
                val candleWidth = 54.dp.toPx()
                val candleHeight = 110.dp.toPx()
                drawRoundRect(
                    brush = candleBodyBrush,
                    topLeft = Offset(cx - candleWidth / 2f, cy),
                    size = Size(candleWidth, candleHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )

                // Cera derretida en la base
                drawCircle(
                    color = Color(0xFFD6C5A9),
                    radius = candleWidth * 0.65f,
                    center = Offset(cx, cy + candleHeight * 0.95f)
                )
            }

            // BOTÓN SUPERIOR DE SALIDA
            AnimatedVisibility(
                visible = areControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                IconButton(
                    onClick = {
                        AuraHaptic.click(view)
                        onDismiss()
                    },
                    modifier = Modifier
                        .padding(20.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(Icons.Default.Close, null, tint = Color(0xFFFDE68A))
                }
            }

            // CONTROLES INFERIORES CONTEMPLATIVOS
            AnimatedVisibility(
                visible = areControlsVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TÍTULO DE LA PISTA EN TIPOGRAFÍA SERIF CÁLIDA
                    Text(
                        text = currentMedia?.title ?: "Santuario de Calma",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFFFFFBEB),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${currentMedia?.artist ?: "Aura Nocturna"} • Meditación",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Serif,
                        color = Color(0xFFD97706),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // CONTROLES MINIMALISTAS EN PENUMBRA
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                AuraHaptic.tick(view)
                                onPreviousClick()
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF261506))
                        ) {
                            Icon(Icons.Default.SkipPrevious, null, tint = Color(0xFFFDE68A), modifier = Modifier.size(24.dp))
                        }

                        // Play/Pause Cálido
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFD97706))
                                .border(1.dp, Color(0xFFFDE68A), CircleShape)
                                .clickable {
                                    AuraHaptic.heavy(view)
                                    onPlayPauseClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pausa" else "Reproducir",
                                tint = Color(0xFF140902),
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                AuraHaptic.tick(view)
                                onNextClick()
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF261506))
                        ) {
                            Icon(Icons.Default.SkipNext, null, tint = Color(0xFFFDE68A), modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}
