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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auraplayer.data.model.MediaModel
import java.util.Locale

/**
 * 💽 Skin Retro Discman CD 2000s para DaVE Player.
 * Simula un reproductor portátil de CD óptico con ventana acrílica transparente,
 * disco holográfico tornasol giratorio en tiempo real y pantalla LCD retro con ESP (Anti-Shock).
 */
@Composable
fun RetroDiscmanSkin(
    song: MediaModel?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onTap: () -> Unit,
    onDoubleTap: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    // Rotación continua de alta velocidad del CD óptico
    val infiniteTransition = rememberInfiniteTransition(label = "discman_rotation")
    val cdAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cd_spin"
    )

    // Formateo de tiempo digital LCD
    val seconds = (currentPositionMs / 1000) % 60
    val minutes = (currentPositionMs / 1000) / 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .border(2.dp, Color(0xFF334155), CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E293B),
                        Color(0xFF0F172A),
                        Color(0xFF020617)
                    )
                )
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { offset -> onDoubleTap(offset) }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Bisel exterior con textura metálica y remaches
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width / 2f

            // Anillo exterior de titanio
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFF475569),
                        Color(0xFF1E293B),
                        Color(0xFF64748B),
                        Color(0xFF334155),
                        Color(0xFF475569)
                    ),
                    center = center
                ),
                radius = outerRadius - 4.dp.toPx(),
                style = Stroke(width = 8.dp.toPx())
            )

            // Surco de la tapa acrílica
            drawCircle(
                color = Color(0x66000000),
                radius = outerRadius - 16.dp.toPx(),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Ventana Acrílica Transparente que muestra el Disco Compacto (CD)
        Box(
            modifier = Modifier
                .fillMaxSize(0.86f)
                .clip(CircleShape)
                .background(Color(0x99050814))
                .border(1.5.dp, Color(0x40FFFFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // El Disco Compacto Óptico (CD)
            Box(
                modifier = Modifier
                    .fillMaxSize(0.92f)
                    .graphicsLayer {
                        if (isPlaying) rotationZ = cdAngle
                    }
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Capa de difracción holográfica tornasol del CD
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.width / 2f

                    // Espejo de policarbonato plateado con reflejo prismático
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFFD4D8E2),
                                Color(0xFFFF99C8), // Magenta iridiscente
                                Color(0xFFFCF6BD), // Amarillo
                                Color(0xFFD0F4DE), // Verde menta
                                Color(0xFFA9DEF9), // Cian
                                Color(0xFFE4C1F9), // Violeta
                                Color(0xFFD4D8E2)  // Plateado base
                            ),
                            center = center
                        ),
                        radius = radius
                    )

                    // Pistas concéntricas diminutas del disco
                    for (i in 1..5) {
                        drawCircle(
                            color = Color(0x22FFFFFF),
                            radius = radius * (0.35f + (i * 0.12f)),
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Reflejo especular en ángulo ("V" de luz blanca reflectiva)
                    rotate(degrees = 45f, pivot = center) {
                        drawLine(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x88FFFFFF), Color.Transparent),
                                center = center,
                                radius = radius
                            ),
                            start = Offset(center.x - radius, center.y),
                            end = Offset(center.x + radius, center.y),
                            strokeWidth = 24.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Anillo de plástico transparente interior (Clear Inner Ring)
                Box(
                    modifier = Modifier
                        .fillMaxSize(0.42f)
                        .clip(CircleShape)
                    .background(Color(0x66FFFFFF))
                    .border(1.dp, Color(0x88FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Serigrafía central del CD (Carátula del Álbum)
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.70f)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xAAFFFFFF), CircleShape)
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (song?.artworkUri != null) {
                            AsyncImage(
                                model = song.artworkUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Agujero central del husillo (Spindle Hole)
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF020617))
                            .border(1.dp, Color(0x99FFFFFF), CircleShape)
                    )
                }
            }

            // Reflejo exterior fijo de la tapa acrílica
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f

                // Brillo curvado de vidrio sobre la tapa
                drawArc(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x40FFFFFF), Color.Transparent)
                    ),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(0f, 0f),
                    size = size
                )
            }
        }

        // Pantallita LCD Retro de Segmentos Digitales (Estilo Sony Discman)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.5.dp, Color(0xFF1E293B), RoundedCornerShape(8.dp))
                .background(Color(0xFF0F2624)) // Verde azulado LCD retroiluminado
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Indicador ESP (Electronic Shock Protection)
                Text(
                    text = "ESP 45s",
                    color = Color(0xFF00F0FF),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                // Track y Estado
                Text(
                    text = if (isPlaying) "TRK 01 ▶" else "TRK 01 ❚❚",
                    color = Color(0xFF5EEAD4),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold
                )

                // Reloj digital
                Text(
                    text = timeFormatted,
                    color = Color(0xFF2DD4BF),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Logo metálico "COMPACT DISC DIGITAL AUDIO" en la parte superior
        Text(
            text = "COMPACT DISC • DIGITAL AUDIO",
            color = Color(0xFF94A3B8),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 18.dp)
        )
    }
}
