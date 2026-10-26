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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.auraplayer.audio.AuraHaptic
import com.auraplayer.data.model.MediaModel
import kotlin.math.cos
import kotlin.math.sin

/**
 * Skin Monolito Arcano: Obelisco de Obsidiana Cuantica y Runas Esotericas.
 * Rinde homenaje a los artefactos cosmicos antiguos con columnas de energia de audio,
 * runas orbitales y proyeccion focal del album.
 */
@Composable
fun ArcaneMonolithSkin(
    song: MediaModel,
    isPlaying: Boolean,
    bassEnergy: Float,
    accentColor: Color,
    secondaryColor: Color,
    onTap: () -> Unit,
    onDoubleTap: (Offset) -> Unit,
    onLongPress: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val infiniteTransition = rememberInfiniteTransition(label = "monolith_transition")

    // Rotacion de las runas orbitales
    val runeOrbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 18000 else 45000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rune_orbit_angle"
    )

    // Pulso de energia del monolito
    val energyPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "energy_pulse"
    )

    val liveEnergy = if (isPlaying) (bassEnergy * 0.45f) else 0f
    val dynamicGlow = ((0.55f + liveEnergy) * energyPulse).coerceIn(0.2f, 1f)

    Box(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = "Skin Monolito Arcano. Toca para cambiar de skin. Doble toque en los bordes adelanta o retrocede 10 segundos, al centro marca favorito. Mantén pulsado para abrir la Consola de Resonancia Arcana."
            }
            .aspectRatio(1f)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(36.dp),
                spotColor = accentColor.copy(alpha = 0.45f),
                ambientColor = Color(0xFF06030F)
            )
            .clip(RoundedCornerShape(36.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1B0E30),
                        Color(0xFF0F071D),
                        Color(0xFF07030C)
                    )
                )
            )
            .border(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        accentColor.copy(alpha = 0.6f),
                        secondaryColor.copy(alpha = 0.3f),
                        Color(0x33FFFFFF)
                    )
                ),
                RoundedCornerShape(36.dp)
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
            }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Fondo Canvas: Columnas de Frecuencia Runicas y Orbita
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val w = size.width
            val h = size.height

            // 1. Rayo mistico central (Eje de luz cosmica)
            drawLine(
                brush = Brush.verticalGradient(
                    listOf(
                        Color.Transparent,
                        accentColor.copy(alpha = dynamicGlow * 0.7f),
                        secondaryColor.copy(alpha = dynamicGlow * 0.9f),
                        accentColor.copy(alpha = dynamicGlow * 0.7f),
                        Color.Transparent
                    )
                ),
                start = Offset(center.x, h * 0.05f),
                end = Offset(center.x, h * 0.95f),
                strokeWidth = 3f + (bassEnergy * 4f),
                cap = StrokeCap.Round
            )

            // 2. Fisuras de energia laterales tipo ecualizador en la piedra
            val barCount = 7
            val barSpacing = h * 0.09f
            val startY = center.y - (barCount / 2 * barSpacing)

            for (i in 0 until barCount) {
                val y = startY + (i * barSpacing)
                val distFromCenter = kotlin.math.abs(i - barCount / 2f)
                val barLen = (w * 0.38f - distFromCenter * 14f) * (0.6f + (bassEnergy * 0.5f))

                // Fila izquierda
                drawLine(
                    color = accentColor.copy(alpha = dynamicGlow * 0.65f),
                    start = Offset(center.x - 30.dp.toPx(), y),
                    end = Offset(center.x - 30.dp.toPx() - barLen, y),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )

                // Fila derecha
                drawLine(
                    color = secondaryColor.copy(alpha = dynamicGlow * 0.65f),
                    start = Offset(center.x + 30.dp.toPx(), y),
                    end = Offset(center.x + 30.dp.toPx() + barLen, y),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }

            // 3. Circunferencia exterior y nodos runicos
            val orbitRadius = w * 0.44f
            rotate(degrees = runeOrbitAngle, pivot = center) {
                drawCircle(
                    color = accentColor.copy(alpha = dynamicGlow * 0.45f),
                    radius = orbitRadius,
                    center = center,
                    style = Stroke(
                        width = 1.4f,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 16f), 0f)
                    )
                )

                // 6 Nodos estelares arcanos
                for (k in 0 until 6) {
                    val rad = Math.toRadians((k * 60).toDouble()).toFloat()
                    val nx = center.x + orbitRadius * cos(rad)
                    val ny = center.y + orbitRadius * sin(rad)

                    drawCircle(
                        color = Color.White.copy(alpha = dynamicGlow * 0.85f),
                        radius = 2.5f + (bassEnergy * 2f),
                        center = Offset(nx, ny)
                    )
                    drawCircle(
                        color = secondaryColor.copy(alpha = dynamicGlow * 0.5f),
                        radius = 5.5f,
                        center = Offset(nx, ny),
                        style = Stroke(width = 1f)
                    )
                }
            }
        }

        // Medallon Central: Prisma con Caratula del Album
        Box(
            modifier = Modifier
                .size(150.dp)
                .shadow(16.dp, CircleShape, spotColor = accentColor)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xFF281845), Color(0xFF130924), Color(0xFF080310))
                    )
                )
                .border(
                    2.dp,
                    Brush.sweepGradient(
                        listOf(
                            accentColor,
                            secondaryColor,
                            Color(0xFFE040FB),
                            accentColor
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (song.artworkUri != null) {
                AsyncImage(
                    model = song.artworkUri,
                    contentDescription = "Caratula Arcana",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize(0.88f)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                    tint = accentColor
                )
            }

            // Ojo de Horus / Anillo de luz en el nucleo
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xCC07030C))
                    .border(1.2.dp, Color(0xFF00E5FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ᛟ",
                    color = Color(0xFF00E5FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Etiqueta inferior con el modo
        Text(
            text = "* MONOLITO ARCANO (Toca para Vinilo)",
            style = MaterialTheme.typography.labelSmall,
            color = accentColor.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)
        )
    }
}
