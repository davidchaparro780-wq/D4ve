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
import com.auraplayer.audio.RelaxAmbienceManager
import com.auraplayer.data.model.MediaModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

/**
 * ☕ Modo Lofi Chill Cafe: Pantalla completa de estudio, relajación y concentración.
 * Cuenta con ventana con lluvia procedural animada, lámpara cálida, taza con vapor ascendente,
 * reloj retro de mesa y controles minimalistas.
 */
@Composable
fun LofiCafeDialog(
    currentMedia: MediaModel?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    val ambienceMgr = remember { RelaxAmbienceManager.instance }

    // Reloj digital con actualización en vivo
    var currentTimeStr by remember { mutableStateOf("00:00") }
    var currentDateStr by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES"))
        while (true) {
            val now = Date()
            currentTimeStr = timeFmt.format(now)
            currentDateStr = dateFmt.format(now).replaceFirstChar { it.uppercase() }
            delay(1000)
        }
    }

    // Animación infinita para lluvia y vapor
    val infiniteTransition = rememberInfiniteTransition(label = "lofi_ambient_loop")
    val rainPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rainPhase"
    )
    val steamPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "steamPhase"
    )

    // Pre-cache brush to avoid allocation on draw loop (120 FPS zero-allocation rule)
    val windowGlassBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF13192B), Color(0xFF090D18))
        )
    }
    val backgroundBrush = remember {
        Brush.verticalGradient(
            listOf(
                Color(0xFF0F0B18), // Noche profunda
                Color(0xFF181024), // Tono acogedor
                Color(0xFF0A0710)
            )
        )
    }
    val clockBackgroundBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF221710), Color(0xFF140D0A))
        )
    }
    val playPauseBrush = remember {
        Brush.radialGradient(
            listOf(Color(0xFFF59E0B), Color(0xFFB45309))
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            // 1. VENTANA PROCEDURAL CON LLUVIA
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Marco de ventana
                val winX = w * 0.08f
                val winY = h * 0.08f
                val winW = w * 0.84f
                val winH = h * 0.40f

                // Luz azul nocturna detrás del vidrio
                drawRoundRect(
                    brush = windowGlassBrush,
                    topLeft = Offset(winX, winY),
                    size = Size(winW, winH),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                )

                // Gotas de lluvia cayendo en diagonal
                val numDrops = 38
                for (i in 0 until numDrops) {
                    val dropX = winX + ((i * 37f + 13f) % winW)
                    val dropProgress = (rainPhase + (i * 0.17f)) % 1f
                    val dropY = winY + (dropProgress * winH)
                    val dropLen = 14.dp.toPx()

                    drawLine(
                        color = Color(0xFF88A8D8).copy(alpha = 0.35f),
                        start = Offset(dropX, dropY),
                        end = Offset(dropX - 4.dp.toPx(), dropY + dropLen),
                        strokeWidth = 1.6.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }

                // Marco de madera de la ventana
                drawRoundRect(
                    color = Color(0xFF281C14),
                    topLeft = Offset(winX, winY),
                    size = Size(winW, winH),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                    style = Stroke(width = 4.dp.toPx())
                )
                // División central de la ventana
                drawLine(
                    color = Color(0xFF281C14),
                    start = Offset(winX + winW / 2f, winY),
                    end = Offset(winX + winW / 2f, winY + winH),
                    strokeWidth = 3.dp.toPx()
                )

                // 2. TAZA DE CAFÉ HUMEANTE
                val mugX = winX + winW * 0.72f
                val mugY = winY + winH + 18.dp.toPx()

                // Vapor ascendente sinusoidal
                for (stream in 0..2) {
                    val path = Path()
                    val startX = mugX + (stream - 1) * 8.dp.toPx()
                    path.moveTo(startX, mugY)
                    val steamHeight = 45.dp.toPx()
                    for (step in 1..20) {
                        val progress = step / 20f
                        val currY = mugY - (progress * steamHeight)
                        val wobble = sin(steamPhase + stream * 1.5f + progress * 4f).toFloat() * 6.dp.toPx()
                        path.lineTo(startX + wobble, currY)
                    }
                    drawPath(
                        path = path,
                        color = Color.White.copy(alpha = (0.28f * (1f - (stream * 0.05f)))),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Cuerpo de la taza
                drawRoundRect(
                    color = Color(0xFFE2D4BE),
                    topLeft = Offset(mugX - 14.dp.toPx(), mugY),
                    size = Size(28.dp.toPx(), 26.dp.toPx()),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
                // Asa de la taza
                drawCircle(
                    color = Color(0xFFE2D4BE),
                    radius = 7.dp.toPx(),
                    center = Offset(mugX + 16.dp.toPx(), mugY + 12.dp.toPx()),
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            // BOTÓN CERRAR
            IconButton(
                onClick = {
                    AuraHaptic.click(view)
                    onDismiss()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color(0xFFFBBF24)
                )
            }

            // CONTENIDO INFERIOR: RELOJ + CANCIÓN + CONTROLES + AMBIENTE
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // RELOJ RETRO FLIP-CLOCK
                Box(
                    modifier = Modifier
                        .shadow(16.dp, RoundedCornerShape(18.dp), spotColor = Color(0xFFF59E0B).copy(alpha = 0.35f))
                        .clip(RoundedCornerShape(18.dp))
                        .background(clockBackgroundBrush)
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 28.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentTimeStr,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFDE68A),
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = currentDateStr,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFD97706)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // CANCIÓN ACTUAL
                if (currentMedia != null) {
                    Text(
                        text = currentMedia.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFBEB),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "${currentMedia.artist} • Lofi Chill Room",
                        fontSize = 12.5.sp,
                        color = Color(0xFFFBBF24).copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // CONTROLES DE REPRODUCCIÓN RETRO
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    IconButton(
                        onClick = {
                            AuraHaptic.tick(view)
                            onPreviousClick()
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF241A14))
                    ) {
                        Icon(Icons.Default.SkipPrevious, null, tint = Color(0xFFFDE68A), modifier = Modifier.size(24.dp))
                    }

                    // Play/Pause Cálido
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .shadow(12.dp, CircleShape, spotColor = Color(0xFFF59E0B))
                            .clip(CircleShape)
                            .background(playPauseBrush)
                            .clickable {
                                AuraHaptic.heavy(view)
                                onPlayPauseClick()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = Color(0xFF140D0A),
                            modifier = Modifier.size(32.dp)
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
                            .background(Color(0xFF241A14))
                    ) {
                        Icon(Icons.Default.SkipNext, null, tint = Color(0xFFFDE68A), modifier = Modifier.size(24.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // SELECTOR DE SONIDO AMBIENTAL RÁPIDO
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val ambienceOptions = listOf(
                        Pair("🌧️ Lluvia", com.auraplayer.audio.AmbienceSound.RAIN),
                        Pair("🔥 Fogata", com.auraplayer.audio.AmbienceSound.CAMPFIRE),
                        Pair("☕ Marrón", com.auraplayer.audio.AmbienceSound.BROWN_NOISE),
                        Pair("🍃 Viento", com.auraplayer.audio.AmbienceSound.WIND)
                    )

                    ambienceOptions.forEach { (label, sound) ->
                        val isCurrent = ambienceMgr.currentSound == sound
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCurrent) Color(0xFFF59E0B).copy(alpha = 0.25f) else Color(0xFF1F1612))
                                .border(
                                    width = if (isCurrent) 1.dp else 0.5.dp,
                                    color = if (isCurrent) Color(0xFFF59E0B) else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    AuraHaptic.click(view)
                                    if (isCurrent) {
                                        ambienceMgr.setSound(com.auraplayer.audio.AmbienceSound.NONE)
                                    } else {
                                        ambienceMgr.setSound(sound)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) Color(0xFFFDE68A) else Color(0xFFD1D5DB)
                            )
                        }
                    }
                }
            }
        }
    }
}
