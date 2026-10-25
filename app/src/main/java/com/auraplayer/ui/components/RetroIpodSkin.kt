package com.auraplayer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.data.model.MediaModel
import java.util.Locale
import kotlin.math.atan2

/**
 * 🕹️ Skin Retro iPod Classic con Click Wheel Táctil Háptica.
 * Réplica interactiva del icónico reproductor con rueda de clic giratoria física:
 * - Detección angular polar con rotación 360°.
 * - Micro-vibraciones mecánicas (AuraHaptic.tick) al girar el pulgar.
 * - Pantalla LCD retro con indicador de batería, arte de álbum y progreso.
 */
@Composable
fun RetroIpodSkin(
    song: MediaModel?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onTap: () -> Unit,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeekRelative: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    // Formato de tiempo digital
    val curMins = (currentPositionMs / 1000) / 60
    val curSecs = (currentPositionMs / 1000) % 60
    val totalMins = (durationMs / 1000) / 60
    val totalSecs = (durationMs / 1000) % 60
    val timeFormatted = String.format(Locale.US, "%d:%02d", curMins, curSecs)
    val remainingFormatted = String.format(Locale.US, "-%d:%02d", (durationMs - currentPositionMs).coerceAtLeast(0) / 60000, ((durationMs - currentPositionMs).coerceAtLeast(0) / 1000) % 60)
    val progress = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    // Chasis exterior iPod de aluminio satinado
    Box(
        modifier = modifier
            .shadow(20.dp, RoundedCornerShape(26.dp), spotColor = Color(0xFF64748B).copy(alpha = 0.5f))
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE2E8F0),
                        Color(0xFFCBD5E1),
                        Color(0xFF94A3B8)
                    )
                )
            )
            .border(2.dp, Color(0xFFF8FAFC), RoundedCornerShape(26.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // PANTALLA LCD SUPERIOR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.92f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1E2638))
                    .border(2.5.dp, Color(0xFF0F172A), RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Barra de estado LCD superior
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isPlaying) "▶ DaVE Pod" else "❚❚ DaVE Pod",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            fontFamily = FontFamily.Monospace
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "100%",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            // Icono batería
                            Box(
                                modifier = Modifier
                                    .width(14.dp)
                                    .height(8.dp)
                                    .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(2.dp))
                                    .padding(1.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF10B981), RoundedCornerShape(1.dp))
                                )
                            }
                        }
                    }

                    // Contenido central: Arte + Info de Pista
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail arte
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(6.dp)),
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
                                    Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Info de texto LCD
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = song?.title ?: "Sin reproducir",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song?.artist ?: "DaVE Player",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = song?.album ?: "Audio Hi-Fi",
                                fontSize = 9.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Barra de progreso y tiempos LCD
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF0F172A))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(progress)
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF00F0FF), Color(0xFF38BDF8))
                                        )
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = timeFormatted,
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = remainingFormatted,
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // CLICK WHEEL INFERIOR HÁPTICA
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1.08f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val wheelSize = maxWidth.coerceAtMost(maxHeight)
                val wheelCenter = Offset(wheelSize.value / 2f, wheelSize.value / 2f)

                var lastAngle by remember { mutableFloatStateOf(0f) }
                var accumulatedAngle by remember { mutableFloatStateOf(0f) }

                Box(
                    modifier = Modifier
                        .size(wheelSize)
                        .shadow(12.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.45f))
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                            )
                        )
                        .border(1.5.dp, Color(0xFF94A3B8), CircleShape)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val dx = offset.x - size.width / 2f
                                    val dy = offset.y - size.height / 2f
                                    lastAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    accumulatedAngle = 0f
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val dx = change.position.x - size.width / 2f
                                    val dy = change.position.y - size.height / 2f
                                    val currentAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

                                    var delta = currentAngle - lastAngle
                                    if (delta > 180f) delta -= 360f
                                    if (delta < -180f) delta += 360f

                                    accumulatedAngle += delta
                                    lastAngle = currentAngle

                                    // Cada 16 grados de giro, emite un tick háptico y adelanta/atrasa
                                    if (accumulatedAngle >= 16f) {
                                        AuraHaptic.tick(view)
                                        onSeekRelative(4000L) // +4 segundos
                                        accumulatedAngle = 0f
                                    } else if (accumulatedAngle <= -16f) {
                                        AuraHaptic.tick(view)
                                        onSeekRelative(-4000L) // -4 segundos
                                        accumulatedAngle = 0f
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Botón superior: MENU (Tocar para cambiar de skin)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        AuraHaptic.click(view)
                                        onTap()
                                    }
                                )
                            }
                    ) {
                        Text(
                            text = "MENU",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Botón inferior: PLAY/PAUSE
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        AuraHaptic.click(view)
                                        onTogglePlay()
                                    }
                                )
                            }
                    ) {
                        Text(
                            text = "⏯",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Botón izquierdo: PREV
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        AuraHaptic.click(view)
                                        onPrev()
                                    }
                                )
                            }
                    ) {
                        Text(
                            text = "⏮",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Botón derecho: NEXT
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        AuraHaptic.click(view)
                                        onNext()
                                    }
                                )
                            }
                    ) {
                        Text(
                            text = "⏭",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                    }

                    // BOTÓN CENTRAL: SELECT (Tocar para Play/Pause o cambiar de skin)
                    Box(
                        modifier = Modifier
                            .size(wheelSize * 0.40f)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0))
                                )
                            )
                            .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        AuraHaptic.click(view)
                                        onTogglePlay()
                                    },
                                    onLongPress = {
                                        AuraHaptic.heavy(view)
                                        onTap()
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SELECT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}
