package com.auraplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.data.model.MediaModel

/**
 * Skin de Carátula Cuadrada Completa (Full Square Album Art Hi-Fi).
 * Muestra la portada del álbum en su proporción cuadrada original completa (1:1),
 * con esquinas redondeadas de 26dp, borde sutil de cristal líquido y reactividad háptica.
 */
@Composable
fun SquareAlbumArtSkin(
    song: MediaModel,
    isPlaying: Boolean,
    accentColor: Color,
    onTap: () -> Unit,
    onDoubleTap: (Offset) -> Unit,
    onLongPress: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current

    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = "Carátula Cuadrada Completa de ${song.title}. Toca para cambiar de skin."
            }
            .aspectRatio(1f)
            .shadow(
                elevation = if (isPlaying) 18.dp else 8.dp,
                shape = RoundedCornerShape(26.dp),
                spotColor = accentColor.copy(alpha = 0.45f),
                ambientColor = Color.Black
            )
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xFF101422))
            .border(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.40f),
                        accentColor.copy(alpha = 0.50f),
                        Color.White.copy(alpha = 0.15f)
                    )
                ),
                RoundedCornerShape(26.dp)
            )
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
        if (song.artworkUri != null) {
            AsyncImage(
                model = song.artworkUri,
                contentDescription = "Carátula Cuadrada Completa",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            listOf(
                                Color(0xFF1E293B),
                                Color(0xFF0F172A),
                                Color(0xFF020617)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = accentColor
                )
            }
        }

        // Borde interior sutil para realce de carátula
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.12f),
                    RoundedCornerShape(26.dp)
                )
        )
    }
}
