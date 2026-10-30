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
import coil.compose.AsyncImage
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.audio.RealtimeVisualizerManager
import com.auraplayer.data.model.MediaModel
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * 🚀 Aura Holo-Cockpit 2077 (Nave Espacial Sci-Fi)
 * Interfaz holográfica de mando interestelar con:
 * - Esfera holográfica cuántica central con 3 anillos orbitales giroscópicos en rotación continua.
 * - Carátula suspendida en levitación con halo de energía pulsante reactivo al beat.
 * - Pantalla de telemetría HUD espacial con frecuencias de plasma y estado de escudos.
 * - Controles tácticos de cabina blindada y botón de ignición hiperespacial "WARP DRIVE".
 */
@Composable
fun HoloCockpitDialog(
    currentMedia: MediaModel?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current

    // Nivel de energía de plasma reactivo a graves
    var plasmaEnergy by remember { mutableFloatStateOf(0.1f) }
    LaunchedEffect(isPlaying) {
        val visMgr = RealtimeVisualizerManager.instance
        while (true) {
            val target = if (isPlaying) visMgr.getBassEnergy(true) else 0.05f
            plasmaEnergy = (plasmaEnergy * 0.70f) + (target * 0.30f)
            delay(16)
        }
    }

    // Rotación continua de los anillos orbitales
    val infiniteTransition = rememberInfiniteTransition(label = "holo_orbit")
    val orbitPhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitPhase1"
    )
    val orbitPhase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbitPhase2"
    )

    // Pre-cache de degradados para 120 FPS
    val cockpitBgBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF04060E), Color(0xFF081226), Color(0xFF02040A))
        )
    }
    val hudCardBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF0D1B2A), Color(0xFF050B14))
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cockpitBgBrush)
        ) {
            // RETÍCULA HUD ESPACIAL & ANILLOS GIROSCÓPICOS EN CANVAS
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h * 0.42f

                // 1. Líneas de radar táctico en perspectiva
                for (grid in 1..4) {
                    val gridY = cy + (grid * 55.dp.toPx())
                    drawLine(
                        color = Color(0xFF00F0FF).copy(alpha = 0.06f),
                        start = Offset(0f, gridY),
                        end = Offset(w, gridY),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // 2. Esfera cuántica con 3 anillos giroscópicos
                val baseRadius = 88.dp.toPx() * (1f + plasmaEnergy * 0.15f)

                // Anillo 1: Horizontal elíptico
                drawOval(
                    color = Color(0xFF00F0FF).copy(alpha = 0.55f),
                    topLeft = Offset(cx - baseRadius * 1.3f, cy - baseRadius * 0.35f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 2.6f, baseRadius * 0.70f),
                    style = Stroke(width = 1.8.dp.toPx())
                )

                // Anillo 2: Inclinado (simulado con puntos orbitales)
                for (i in 0..16) {
                    val angle = orbitPhase1 + (i * (PI / 8.0)).toFloat()
                    val px = cx + cos(angle) * (baseRadius * 1.2f)
                    val py = cy + sin(angle) * (baseRadius * 0.65f)
                    drawCircle(
                        color = Color(0xFFFF007F).copy(alpha = 0.75f),
                        radius = 2.dp.toPx(),
                        center = Offset(px, py)
                    )
                }

                // Anillo 3: En rotación contraria con nodos de energía
                for (i in 0..12) {
                    val angle = -orbitPhase2 + (i * (PI / 6.0)).toFloat()
                    val px = cx + sin(angle) * (baseRadius * 0.55f)
                    val py = cy + cos(angle) * (baseRadius * 1.25f)
                    drawCircle(
                        color = Color(0xFF38BDF8).copy(alpha = 0.85f),
                        radius = 2.5.dp.toPx(),
                        center = Offset(px, py)
                    )
                }

                // Cruces de calibración en las 4 esquinas del HUD
                val cornerSize = 18.dp.toPx()
                drawLine(Color(0xFF00F0FF).copy(alpha = 0.4f), Offset(20.dp.toPx(), 20.dp.toPx()), Offset(20.dp.toPx() + cornerSize, 20.dp.toPx()), 1.5.dp.toPx())
                drawLine(Color(0xFF00F0FF).copy(alpha = 0.4f), Offset(20.dp.toPx(), 20.dp.toPx()), Offset(20.dp.toPx(), 20.dp.toPx() + cornerSize), 1.5.dp.toPx())
                drawLine(Color(0xFF00F0FF).copy(alpha = 0.4f), Offset(w - 20.dp.toPx(), 20.dp.toPx()), Offset(w - 20.dp.toPx() - cornerSize, 20.dp.toPx()), 1.5.dp.toPx())
                drawLine(Color(0xFF00F0FF).copy(alpha = 0.4f), Offset(w - 20.dp.toPx(), 20.dp.toPx()), Offset(w - 20.dp.toPx(), 20.dp.toPx() + cornerSize), 1.5.dp.toPx())
            }

            // CABECERA SUPERIOR: TELEMETRÍA DE CABINA & CERRAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AURA-CORVETTE • NCC-2077",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF00F0FF),
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        text = "WARP STATUS: ${if (isPlaying) "MAX VELOCITY" else "SUB-LIGHT STANDBY"}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isPlaying) Color(0xFF34D399) else Color(0xFF94A3B8)
                    )
                }

                IconButton(
                    onClick = {
                        AuraHaptic.click(view)
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            // CARÁTULA EN SUSPENSIÓN HOLOGRÁFICA CUÁNTICA
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 130.dp)
                    .size(140.dp)
                    .shadow(24.dp, CircleShape, spotColor = Color(0xFF00F0FF).copy(alpha = 0.8f))
                    .clip(CircleShape)
                    .background(Color(0xFF060B18))
                    .border(2.dp, Color(0xFF00F0FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (currentMedia?.artworkUri != null) {
                    AsyncImage(
                        model = currentMedia.artworkUri,
                        contentDescription = currentMedia.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                } else {
                    Text("🚀", fontSize = 42.sp)
                }
            }

            // PANEL INFERIOR DE NAVEGACIÓN Y CONTROLES
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TARJETA DE NAVEGACIÓN ESTELAR
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(hudCardBrush)
                        .border(1.2.dp, Color(0xFF00F0FF).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "COORDINATES // TARGET TRACK",
                                fontSize = 8.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = "PLASMA: ${(plasmaEnergy * 100).toInt()}%",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFFF007F)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentMedia?.title ?: "ORBITAL VOID",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${currentMedia?.artist ?: "UNKNOWN"} • SECTOR 7",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00F0FF),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // CONTROLES DE PROPULSIÓN INTERPLANETARIA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // RETRO-PROPULSORES (PREV)
                    IconButton(
                        onClick = {
                            AuraHaptic.heavy(view)
                            onPreviousClick()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0D1B2A))
                            .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.SkipPrevious, null, tint = Color(0xFF00F0FF), modifier = Modifier.size(24.dp))
                    }

                    // IGNICIÓN WARP DRIVE (PLAY/PAUSE)
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .shadow(20.dp, CircleShape, spotColor = Color(0xFF00F0FF))
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(listOf(Color(0xFF00F0FF), Color(0xFF0284C7)))
                            )
                            .clickable {
                                AuraHaptic.heavy(view)
                                onPlayPauseClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Desactivar Warp" else "Iniciar Warp",
                            tint = Color(0xFF030816),
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    // HIPER-SALTO (NEXT)
                    IconButton(
                        onClick = {
                            AuraHaptic.heavy(view)
                            onNextClick()
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0D1B2A))
                            .border(1.dp, Color(0xFF00F0FF).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.SkipNext, null, tint = Color(0xFF00F0FF), modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}
