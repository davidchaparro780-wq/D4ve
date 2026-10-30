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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.data.model.MediaModel

/**
 * 💿 Minidisc Sony 2000s (Hi-MD Retro)
 * Clásico reproductor magneto-óptico de principios de los 2000s con:
 * - Carcasa cuadrada translúcida azul zafiro con persiana de aluminio grabado.
 * - Disco óptico giratorio con lectura láser ultravioleta dinámica desplazándose radialmente.
 * - Display LCD remoto de mando de cable con marquesina matricial ATRAC3plus.
 * - Botón mecánico táctil de expulsión "EJECT" y botones de control cromados.
 */
@Composable
fun MinidiscRetroDialog(
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
    var isShutterOpen by remember { mutableStateOf(true) }

    // Rotación del minidisc (~400ms por vuelta)
    val infiniteTransition = rememberInfiniteTransition(label = "minidisc_spin")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "discRotation"
    )

    // Pre-cache de pinceles (120 FPS sin alocaciones)
    val minidiscCaseBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0xEE1E3A8A), Color(0xDD172554), Color(0xCC0F172A)) // Azul translúcido policarbonato
        )
    }
    val shutterBrush = remember {
        Brush.horizontalGradient(
            listOf(Color(0xFF94A3B8), Color(0xFFF1F5F9), Color(0xFF64748B))
        )
    }
    val remoteLcdBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF064E3B), Color(0xFF022C22))
        )
    }
    val discMirrorBrush = remember {
        Brush.sweepGradient(
            listOf(Color(0xFF94A3B8), Color(0xFFC7D2FE), Color(0xFFE2E8F0), Color(0xFF94A3B8))
        )
    }

    // Progreso radial del láser de lectura (0f a 1f)
    val laserProgress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0.3f

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xEE050814)),
            contentAlignment = Alignment.Center
        ) {
            // CARTUCHO CUADRADO HI-MD
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(minidiscCaseBrush)
                    .border(2.dp, Color(0x6660A5FA), RoundedCornerShape(22.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // CABECERA: MARCA NET-MD Y BOTÓN CERRAR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "SONY NetMD • Hi-MD 1GB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF93C5FD),
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            AuraHaptic.click(view)
                            onDismiss()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // VENTANA DEL CARTUCHO CON EL DISCO ÓPTICO-MAGNÉTICO GIRATORIO
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(if (isPlaying) discRotation else 0f)
                    ) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val r = size.width * 0.42f

                        // Disco plateado brillante
                        drawCircle(
                            brush = discMirrorBrush,
                            radius = r,
                            center = Offset(cx, cy)
                        )
                        // Borde exterior
                        drawCircle(
                            color = Color(0xFF475569),
                            radius = r,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        // Centro de acoplamiento magnético metálico
                        drawCircle(
                            color = Color(0xFF64748B),
                            radius = r * 0.32f,
                            center = Offset(cx, cy)
                        )
                        drawCircle(
                            color = Color(0xFF0F172A),
                            radius = r * 0.12f,
                            center = Offset(cx, cy)
                        )
                    }

                    // PERSIANA DESLIZANTE DE ALUMINIO (SHUTTER)
                    if (isShutterOpen) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.35f)
                                .height(95.dp)
                                .align(Alignment.CenterEnd)
                                .clip(RoundedCornerShape(4.dp))
                                .background(shutterBrush)
                                .border(1.dp, Color(0xFF475569), RoundedCornerShape(4.dp))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Hi-MD\n1GB",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }

                    // HAZ LÁSER ULTRAVIOLETA DE LECTURA ÓPTICA
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val minRadius = size.width * 0.16f
                        val maxRadius = size.width * 0.40f

                        val currentRadius = minRadius + (laserProgress * (maxRadius - minRadius))
                        val laserOffset = Offset(cx + currentRadius, cy)

                        // Punto láser UV parpadeante
                        if (isPlaying) {
                            drawCircle(
                                color = Color(0xFF818CF8),
                                radius = 4.dp.toPx(),
                                center = laserOffset
                            )
                            drawCircle(
                                color = Color(0xFFC7D2FE),
                                radius = 2.dp.toPx(),
                                center = laserOffset
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // DISPLAY LCD REMOTO ALARGADO DE MANDO WALKMAN
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(remoteLcdBrush)
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ATRAC3plus / 256kbps",
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF6EE7B7)
                            )
                            Text(
                                text = if (isPlaying) "▶ PLAY" else "❚❚ PAUSE",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF34D399)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentMedia?.title ?: "NO DISC LOADED",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF34D399),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${currentMedia?.artist ?: "UNKNOWN"} • MD-LP4",
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF059669),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // BOTONES DE REPRODUCCIÓN RETRO Y BOTÓN EJECT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // BOTÓN EJECT MECÁNICO
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF334155))
                            .border(1.dp, Color(0xFF64748B), RoundedCornerShape(6.dp))
                            .clickable {
                                AuraHaptic.heavy(view)
                                isShutterOpen = !isShutterOpen
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⏏ EJECT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E8F0)
                        )
                    }

                    // GRUPO DE CONTROLES CROMADOS (PREV, PLAY/PAUSE, NEXT)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                AuraHaptic.tick(view)
                                onPreviousClick()
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(Icons.Default.FastRewind, null, tint = Color(0xFF93C5FD), modifier = Modifier.size(20.dp))
                        }

                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .shadow(12.dp, CircleShape, spotColor = Color(0xFF3B82F6))
                                .clip(CircleShape)
                                .background(Color(0xFF2563EB))
                                .border(1.5.dp, Color(0xFF93C5FD), CircleShape)
                                .clickable {
                                    AuraHaptic.heavy(view)
                                    onPlayPauseClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                AuraHaptic.tick(view)
                                onNextClick()
                            },
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B))
                        ) {
                            Icon(Icons.Default.FastForward, null, tint = Color(0xFF93C5FD), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}
