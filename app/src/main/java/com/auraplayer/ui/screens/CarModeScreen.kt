package com.auraplayer.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.data.model.MediaModel
import kotlinx.coroutines.delay

@Composable
fun CarModeScreen(
    currentMedia: MediaModel?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit,
    isFavorite: Boolean = false,
    onToggleFavorite: (() -> Unit)? = null
) {
    val view = LocalView.current
    var gestureFeedbackText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(gestureFeedbackText) {
        if (gestureFeedbackText != null) {
            delay(1200)
            gestureFeedbackText = null
        }
    }

    BackHandler {
        onClose()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {},
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount < -60f) {
                            AuraHaptic.heavy(view)
                            gestureFeedbackText = "⏭ Siguiente"
                            onNext()
                        } else if (dragAmount > 60f) {
                            AuraHaptic.heavy(view)
                            gestureFeedbackText = "⏮ Anterior"
                            onPrevious()
                        }
                    }
                )
            },
        color = Color(0xFF07090E)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header HUD
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF131A2B))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "MODO CONDUCCIÓN HUD",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (onToggleFavorite != null) {
                            IconButton(
                                onClick = {
                                    AuraHaptic.heavy(view)
                                    onToggleFavorite()
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E2433))
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorito",
                                    tint = if (isFavorite) Color(0xFFEC4899) else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                AuraHaptic.click(view)
                                onClose()
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E2433))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Salir",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                // Giant Song Info Card (Tap for blind Play/Pause)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = Color(0xFF38BDF8).copy(alpha = 0.3f))
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF131B2E), Color(0xFF0B101D))
                            )
                        )
                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    AuraHaptic.heavy(view)
                                    onPlayPause()
                                    gestureFeedbackText = if (isPlaying) "⏸ Pausa" else "▶️ Reproduciendo"
                                }
                            )
                        }
                        .padding(vertical = 40.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentMedia?.title ?: "Sin reproducción activa",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 38.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = currentMedia?.artist ?: "DaVE Player HUD",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "👆 Toca la tarjeta para pausar / reanudar",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Blind Gesture Hint & Giant Tactile Controls
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Gesture Hint Pill
                    Text(
                        text = "👈 Desliza para cambiar • Controles táctiles gigantes",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Track (84dp)
                        IconButton(
                            onClick = {
                                AuraHaptic.heavy(view)
                                onPrevious()
                            },
                            modifier = Modifier
                                .size(84.dp)
                                .shadow(8.dp, CircleShape, spotColor = Color.Black)
                                .clip(CircleShape)
                                .background(Color(0xFF1E2433))
                                .border(1.dp, Color(0xFF334155), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Anterior",
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                        }

                        // Play / Pause Giant (110dp)
                        IconButton(
                            onClick = {
                                AuraHaptic.heavy(view)
                                onPlayPause()
                            },
                            modifier = Modifier
                                .size(110.dp)
                                .shadow(14.dp, CircleShape, spotColor = Color(0xFF38BDF8).copy(alpha = 0.6f))
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF00F0FF), Color(0xFF3B82F6))
                                    )
                                )
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Reproducir / Pausar",
                                tint = Color.White,
                                modifier = Modifier.size(72.dp)
                            )
                        }

                        // Next Track (84dp)
                        IconButton(
                            onClick = {
                                AuraHaptic.heavy(view)
                                onNext()
                            },
                            modifier = Modifier
                                .size(84.dp)
                                .shadow(8.dp, CircleShape, spotColor = Color.Black)
                                .clip(CircleShape)
                                .background(Color(0xFF1E2433))
                                .border(1.dp, Color(0xFF334155), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Siguiente",
                                tint = Color.White,
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }
                }
            }

            // Momentary Gesture Feedback Overlay Banner
            AnimatedVisibility(
                visible = gestureFeedbackText != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                gestureFeedbackText?.let { feedback ->
                    Box(
                        modifier = Modifier
                            .shadow(16.dp, RoundedCornerShape(20.dp), spotColor = Color.Black)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xEE0F172A))
                            .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = feedback,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
