package com.auraplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

/**
 * Anillos concéntricos de pulso acústico (Aura Ripple Rings).
 * Ondas circulares de luz y energía que emanan desde detrás de la carátula,
 * expandiéndose y desvaneciéndose hacia el borde para un efecto visual hipnótico.
 */
@Composable
fun AuraRippleRings(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    accentColor: Color = Color(0xFF38BDF8),
    ringCount: Int = 3
) {
    if (!isPlaying) return

    val infiniteTransition = rememberInfiniteTransition(label = "aura_ripples")

    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0.33f,
        targetValue = 1.33f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0.66f,
        targetValue = 1.66f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase3"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val baseRadius = size.minDimension * 0.42f
        val maxExtraRadius = size.minDimension * 0.07f

        val p1 = phase1 % 1f
        val p2 = phase2 % 1f
        val p3 = phase3 % 1f

        val r1 = baseRadius + (maxExtraRadius * p1)
        val a1 = ((1f - p1) * 0.40f).coerceIn(0f, 1f)
        drawCircle(
            color = accentColor.copy(alpha = a1),
            radius = r1,
            style = Stroke(width = 3.5f * (1f - p1 * 0.5f))
        )

        val r2 = baseRadius + (maxExtraRadius * p2)
        val a2 = ((1f - p2) * 0.40f).coerceIn(0f, 1f)
        drawCircle(
            color = accentColor.copy(alpha = a2),
            radius = r2,
            style = Stroke(width = 3.5f * (1f - p2 * 0.5f))
        )

        val r3 = baseRadius + (maxExtraRadius * p3)
        val a3 = ((1f - p3) * 0.40f).coerceIn(0f, 1f)
        drawCircle(
            color = accentColor.copy(alpha = a3),
            radius = r3,
            style = Stroke(width = 3.5f * (1f - p3 * 0.5f))
        )
    }
}
