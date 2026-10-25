package com.auraplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.auraplayer.audio.RealtimeVisualizerManager
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fondo Ambilight Fluido Reactivo (Aura Mesh Gradient).
 * Crea una atmósfera envolvente e hipnótica estilo Apple Music iOS 18 / Canvas,
 * con esferas de luz líquida orgánica que respiran al ritmo visual de la música.
 */
@Composable
fun FluidAmbilightGlow(
    primaryColor: Color,
    secondaryColor: Color,
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fluid_ambilight")

    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambilight_phase"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambilight_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            val liveBass = if (isPlaying) RealtimeVisualizerManager.instance.getBassEnergy(true) else 0f
            val reactiveScale = pulseScale * (1f + liveBass * 0.28f)

            // Esfera de luz 1 (Primaria - Arriba izquierda / centro)
            val x1 = width * (0.35f + 0.2f * sin(phase))
            val y1 = height * (0.3f + 0.15f * cos(phase))
            val r1 = (width * 0.65f) * reactiveScale
            val a1 = (0.40f + liveBass * 0.20f).coerceIn(0.2f, 0.70f)

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to primaryColor.copy(alpha = a1),
                        0.4f to primaryColor.copy(alpha = a1 * 0.55f),
                        0.75f to primaryColor.copy(alpha = a1 * 0.20f),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(x1, y1),
                    radius = r1
                ),
                radius = r1,
                center = Offset(x1, y1)
            )

            // Esfera de luz 2 (Secundaria - Abajo derecha)
            val x2 = width * (0.65f - 0.2f * cos(phase))
            val y2 = height * (0.7f - 0.15f * sin(phase))
            val r2 = (width * 0.7f) * reactiveScale
            val a2 = (0.35f + liveBass * 0.18f).coerceIn(0.18f, 0.65f)

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to secondaryColor.copy(alpha = a2),
                        0.4f to secondaryColor.copy(alpha = a2 * 0.50f),
                        0.75f to secondaryColor.copy(alpha = a2 * 0.18f),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(x2, y2),
                    radius = r2
                ),
                radius = r2,
                center = Offset(x2, y2)
            )

            // Esfera de luz 3 (Acento central profundo reactivo)
            val x3 = width * 0.5f
            val y3 = height * 0.5f
            val r3 = (width * 0.5f) * (1f + liveBass * 0.35f)
            val a3 = (0.20f + liveBass * 0.22f).coerceIn(0.12f, 0.55f)

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to primaryColor.copy(alpha = a3),
                        0.5f to primaryColor.copy(alpha = a3 * 0.40f),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(x3, y3),
                    radius = r3
                ),
                radius = r3,
                center = Offset(x3, y3)
            )
        }
    }
}
