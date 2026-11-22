package com.auraplayer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.data.model.MediaModel
import kotlinx.coroutines.launch

/**
 * Skin de Carátula Cuadrada Completa HD (Full Square Album Art 1:1).
 * Muestra la portada del álbum en su proporción cuadrada original completa sin recortes,
 * esquinas redondeadas de 24dp, borde de cristal líquido iridiscente y fondo reactivo de alta fidelidad.
 */
@Composable
fun SquareAlbumArtSkin(
    song: MediaModel,
    isPlaying: Boolean,
    accentColor: Color,
    onTap: () -> Unit,
    onDoubleTap: (Offset) -> Unit,
    onLongPress: (() -> Unit)? = null,
    onSwipeNext: (() -> Unit)? = null,
    onSwipePrevious: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val coroutineScope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = "Carátula Cuadrada Completa de ${song.title}. Toca para alternar skin. Desliza para cambiar de canción."
            }
            .aspectRatio(1f)
            .graphicsLayer {
                translationX = dragOffset.value
                rotationZ = (dragOffset.value / 32f).coerceIn(-6f, 6f)
            }
            .shadow(
                elevation = if (isPlaying) 18.dp else 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = accentColor.copy(alpha = 0.50f),
                ambientColor = Color.Black
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F1424))
            .border(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.45f),
                        accentColor.copy(alpha = 0.65f),
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .pointerInput(onSwipeNext, onSwipePrevious) {
                if (onSwipeNext != null || onSwipePrevious != null) {
                    detectHorizontalDragGestures(
                        onDragStart = {},
                        onDragEnd = {
                            val current = dragOffset.value
                            coroutineScope.launch {
                                if (current < -75f && onSwipeNext != null) {
                                    AuraHaptic.click(view)
                                    onSwipeNext()
                                } else if (current > 75f && onSwipePrevious != null) {
                                    AuraHaptic.click(view)
                                    onSwipePrevious()
                                }
                                dragOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                dragOffset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            coroutineScope.launch {
                                dragOffset.snapTo((dragOffset.value + dragAmount * 0.72f).coerceIn(-130f, 130f))
                            }
                        }
                    )
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { offset -> onDoubleTap(offset) },
                    onLongPress = {
                        AuraHaptic.heavy(view)
                        onLongPress?.invoke()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Fondo HD por defecto: Card vinilo / gradiente de alta fidelidad que se muestra siempre
        // garantizando que NUNCA aparezca un cuadro negro vacío si la canción no tiene carátula o si Coil está cargando
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1E1B4B),
                            Color(0xFF0F172A),
                            Color(0xFF090D1A)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Anillos concéntricos de vinilo decorativos de fondo
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxR = size.width * 0.45f
                for (i in 1..4) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.04f + (i * 0.015f)),
                        radius = maxR * (i / 4f),
                        center = center
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = accentColor
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = song.title.ifBlank { "DaVE Player" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = song.artist.ifBlank { "Música Local HD" },
                    fontSize = 11.5.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Si existe carátula real del archivo/álbum, se superpone en alta definición
        if (song.artworkUri != null) {
            AsyncImage(
                model = song.artworkUri,
                contentDescription = "Carátula Cuadrada Completa",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
            )
        }

        // Borde interior sutil de cristal líquido para realce de bordes 1:1
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.12f),
                    RoundedCornerShape(24.dp)
                )
        )
    }
}

