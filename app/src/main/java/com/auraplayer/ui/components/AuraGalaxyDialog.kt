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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.data.model.MediaModel
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 🌌 Aura DNA: La Galaxia Musical (Sonic Constellation Graph).
 * Proyecta la biblioteca de canciones como un mapa de estrellas interactivo en el cosmos,
 * donde cada canción es un nodo estelar conectado por líneas de constelación por álbum/género.
 */
@Composable
fun AuraGalaxyDialog(
    songs: List<MediaModel>,
    currentMedia: MediaModel?,
    onSongClick: (MediaModel) -> Unit,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }
    var selectedStarSong by remember { mutableStateOf<MediaModel?>(currentMedia) }

    val infiniteTransition = rememberInfiniteTransition(label = "galaxy_pulsar")
    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starTwinkle"
    )

    // Pre-calcular coordenadas cósmicas para las canciones
    data class StarNode(val song: MediaModel, val x: Float, val y: Float, val color: Color, val radius: Float)
    val starPalette = remember {
        listOf(
            Color(0xFF00F0FF), // Cian pulsar
            Color(0xFFB388FF), // Amatista nebulosa
            Color(0xFFEC4899), // Rosa estelar
            Color(0xFFFBBF24), // Gigante dorada
            Color(0xFF10B981)  // Esmeralda interestelar
        )
    }

    val nodes = remember(songs) {
        val total = songs.size.coerceAtLeast(1)
        songs.take(120).mapIndexed { idx, s ->
            val angle = (idx.toFloat() / total.toFloat()) * (Math.PI * 6.0).toFloat()
            val armRadius = 110f + (idx * 16f)
            val posX = cos(angle) * armRadius + ((idx * 47) % 60 - 30)
            val posY = sin(angle) * armRadius + ((idx * 83) % 60 - 30)
            val color = starPalette[idx % starPalette.size]
            val r = 6f + ((idx % 4) * 2.5f)
            StarNode(s, posX, posY, color, r)
        }
    }

    val cardBackgroundBrush = remember {
        Brush.verticalGradient(
            listOf(Color(0xFF140D26), Color(0xFF07040E))
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF05030A))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        panOffsetX += dragAmount.x
                        panOffsetY += dragAmount.y
                    }
                }
                .pointerInput(nodes) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f + panOffsetX
                        val centerY = size.height / 2f + panOffsetY
                        val tappedNode = nodes.find { node ->
                            val starScreenX = centerX + node.x
                            val starScreenY = centerY + node.y
                            val dist = sqrt((tapOffset.x - starScreenX) * (tapOffset.x - starScreenX) +
                                    (tapOffset.y - starScreenY) * (tapOffset.y - starScreenY))
                            dist <= 36f
                        }
                        if (tappedNode != null) {
                            AuraHaptic.click(view)
                            selectedStarSong = tappedNode.song
                        }
                    }
                }
        ) {
            // LIENZO DE LA GALAXIA
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f + panOffsetX
                val cy = size.height / 2f + panOffsetY

                // Filamentos de constelación que unen canciones consecutivas
                for (i in 0 until nodes.size - 1) {
                    val n1 = nodes[i]
                    val n2 = nodes[i + 1]
                    if (n1.song.album == n2.song.album || (i % 3 != 0)) {
                        drawLine(
                            color = n1.color.copy(alpha = 0.18f),
                            start = Offset(cx + n1.x, cy + n1.y),
                            end = Offset(cx + n2.x, cy + n2.y),
                            strokeWidth = 1.2f
                        )
                    }
                }

                // Dibujar nodos estelares
                nodes.forEach { node ->
                    val starX = cx + node.x
                    val starY = cy + node.y
                    val isPlayingThis = currentMedia?.id == node.song.id
                    val isSelectedThis = selectedStarSong?.id == node.song.id

                    // Halo exterior si está reproduciéndose o seleccionada
                    if (isPlayingThis || isSelectedThis) {
                        drawCircle(
                            color = node.color.copy(alpha = starTwinkle * 0.45f),
                            radius = node.radius * 3.4f,
                            center = Offset(starX, starY)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = node.radius * 2.2f,
                            center = Offset(starX, starY),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // Estrella central
                    drawCircle(
                        color = if (isPlayingThis) Color.White else node.color,
                        radius = node.radius,
                        center = Offset(starX, starY)
                    )
                }
            }

            // CABECERA SUPERIOR: TÍTULO Y BOTÓN DE CERRAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFF00F0FF), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AURA DNA • GALAXIA MUSICAL",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF00F0FF),
                            letterSpacing = 1.2.sp
                        )
                    }
                    Text(
                        text = "Arrastra para explorar • Toca una estrella para escuchar",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                IconButton(
                    onClick = {
                        AuraHaptic.click(view)
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.10f))
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }

            // TARJETA INFERIOR: ESTRELLA SELECCIONADA Y ACCIÓN REPRODUCIR
            selectedStarSong?.let { song ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .shadow(20.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFF00F0FF).copy(alpha = 0.4f))
                        .clip(RoundedCornerShape(22.dp))
                        .background(cardBackgroundBrush)
                        .border(1.dp, Color(0xFFB388FF).copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = song.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${song.artist} • ${song.album.ifBlank { "Aura Track" }}",
                                fontSize = 12.sp,
                                color = Color(0xFF00F0FF),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Button(
                            onClick = {
                                AuraHaptic.heavy(view)
                                onSongClick(song)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                            modifier = Modifier.height(42.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Viajar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                        }
                    }
                }
            }
        }
    }
}
