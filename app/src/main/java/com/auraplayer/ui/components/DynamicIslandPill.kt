package com.auraplayer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.data.model.MediaModel
import kotlinx.coroutines.delay

/**
 * 💊 Dynamic Island / Cápsula Dinámica Notch Pill para DaVE Player.
 * Flota en la parte superior con física de resorte (Spring animation),
 * mostrando el estado de reproducción actual en una cápsula ultra-fluida a 120 FPS.
 */
@Composable
fun DynamicIslandPill(
    currentMedia: MediaModel?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onPillClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentMedia == null) return

    val view = LocalView.current
    var isExpanded by remember { mutableStateOf(false) }

    // Auto-expande temporalmente la cápsula al cambiar de tema
    LaunchedEffect(currentMedia.id) {
        isExpanded = true
        delay(4000)
        isExpanded = false
    }

    // Auto-colapso tras inactividad si el usuario la abrió manualmente
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            delay(5000)
            isExpanded = false
        }
    }

    // Dimensiones animadas con física de resorte elástico (iOS Dynamic Island style)
    val pillHeight by animateDpAsState(
        targetValue = if (isExpanded) 74.dp else 34.dp,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dynamic_island_height"
    )

    val pillWidthFraction by animateFloat(
        targetValue = if (isExpanded) 0.92f else 0.40f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dynamic_island_width"
    )

    // Rotación continua de la mini carátula
    val infiniteTransition = rememberInfiniteTransition(label = "disc_spin")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_rotation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(pillWidthFraction)
                .height(pillHeight)
                .clip(RoundedCornerShape(if (isExpanded) 22.dp else 17.dp))
                .border(
                    width = 1.dp,
                    color = if (isExpanded) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color(0x30FFFFFF),
                    shape = RoundedCornerShape(if (isExpanded) 22.dp else 17.dp)
                )
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xF80B101E),
                            Color(0xF804070F)
                        )
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            AuraHaptic.click(view)
                            if (isExpanded) {
                                onPillClick()
                            } else {
                                isExpanded = true
                            }
                        },
                        onLongPress = {
                            AuraHaptic.heavy(view)
                            isExpanded = !isExpanded
                        }
                    )
                }
                .padding(horizontal = if (isExpanded) 12.dp else 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isExpanded) {
                // Vista Expandida (Información Completa + Controles de Reproducción)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Carátula Redonda
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            .background(Color(0xFF141A29)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentMedia.artworkUri != null) {
                            AsyncImage(
                                model = currentMedia.artworkUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Título y Artista
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentMedia.title,
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentMedia.artist,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Controles Compactos
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                AuraHaptic.click(view)
                                onPreviousClick()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Anterior",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    AuraHaptic.heavy(view)
                                    onPlayPauseClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                AuraHaptic.click(view)
                                onNextClick()
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Siguiente",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            } else {
                // Vista Colapsada (Píldora Minimalista Flotante)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mini Vinilo giratorio
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .border(0.5.dp, Color(0x44FFFFFF), CircleShape)
                            .graphicsLayer {
                                if (isPlaying) rotationZ = discRotation
                            }
                            .background(Color(0xFF131A29)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (currentMedia.artworkUri != null) {
                            AsyncImage(
                                model = currentMedia.artworkUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Barras de ecualizador miniaturizadas
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        val bar1Height by infiniteTransition.animateFloat(
                            initialValue = 4f, targetValue = 14f,
                            animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
                            label = "b1"
                        )
                        val bar2Height by infiniteTransition.animateFloat(
                            initialValue = 12f, targetValue = 5f,
                            animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
                            label = "b2"
                        )
                        val bar3Height by infiniteTransition.animateFloat(
                            initialValue = 6f, targetValue = 15f,
                            animationSpec = infiniteRepeatable(tween(290, easing = LinearEasing), RepeatMode.Reverse),
                            label = "b3"
                        )

                        val activeColor = MaterialTheme.colorScheme.primary

                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(if (isPlaying) bar1Height.dp else 4.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(activeColor)
                        )
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(if (isPlaying) bar2Height.dp else 8.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(activeColor)
                        )
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height(if (isPlaying) bar3Height.dp else 5.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(activeColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Indicador de reproducción verde neón / acento
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) Color(0xFF10B981) else Color(0xFF64748B))
                    )
                }
            }
        }
    }
}
