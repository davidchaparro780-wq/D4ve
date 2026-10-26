package com.auraplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Nebulosa de constelacion estelar que reacciona al audio.
 * Dibuja un mapa de estrellas con lineas de constelacion que pulsan
 * segun la energia de baja frecuencia. Solo visible en temas arcanos.
 */
@Composable
fun StellarConstellationBanner(
    bassEnergy: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "constellation")

    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 90000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift"
    )

    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    // Static seed positions (normalized 0..1) for 14 star nodes
    val starSeeds = remember {
        listOf(
            0.05f to 0.3f, 0.14f to 0.7f, 0.23f to 0.2f, 0.31f to 0.6f,
            0.39f to 0.15f, 0.47f to 0.8f, 0.55f to 0.35f, 0.63f to 0.65f,
            0.70f to 0.1f, 0.78f to 0.55f, 0.84f to 0.25f, 0.90f to 0.75f,
            0.95f to 0.4f, 0.50f to 0.5f
        )
    }

    // Constellation edges connecting star nodes
    val edges = remember {
        listOf(
            0 to 1, 1 to 3, 3 to 5, 5 to 7, 7 to 9, 9 to 11, 11 to 12,
            0 to 2, 2 to 4, 4 to 6, 6 to 13, 13 to 8, 8 to 10
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        val w = size.width
        val h = size.height
        val energy = bassEnergy.coerceIn(0f, 1f)

        // Compute drifted star positions with subtle floating
        val driftRad = Math.toRadians(drift.toDouble()).toFloat()
        val stars = starSeeds.mapIndexed { i, (nx, ny) ->
            val phase = i * 0.62f
            val floatAmt = 4f + energy * 6f
            val sx = nx * w + floatAmt * cos(driftRad + phase)
            val sy = ny * h + floatAmt * 0.5f * sin(driftRad * 0.7f + phase)
            Offset(sx, sy)
        }

        // Draw constellation lines
        val lineAlpha = (0.15f + energy * 0.3f) * twinkle
        edges.forEach { (a, b) ->
            if (a < stars.size && b < stars.size) {
                drawLine(
                    color = accentColor.copy(alpha = lineAlpha),
                    start = stars[a],
                    end = stars[b],
                    strokeWidth = 0.8f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Draw star nodes
        stars.forEachIndexed { i, pos ->
            val phase = i * 0.85f
            val tAlpha = (0.5f + 0.5f * sin(driftRad * 0.3f + phase)) * twinkle
            val radius = 1.5f + energy * 2.5f + (if (i == 13) 2f else 0f)

            // Outer glow
            drawCircle(
                color = accentColor.copy(alpha = tAlpha * 0.25f),
                radius = radius * 2.2f,
                center = pos
            )
            // Core
            drawCircle(
                color = Color.White.copy(alpha = tAlpha),
                radius = radius,
                center = pos
            )
        }

        // Subtle nebula mist at bottom edge
        drawRect(
            color = accentColor.copy(alpha = 0.04f + energy * 0.06f),
            topLeft = Offset(0f, h * 0.7f),
            size = androidx.compose.ui.geometry.Size(w, h * 0.3f)
        )
    }
}
