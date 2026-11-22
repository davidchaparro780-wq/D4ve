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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.auraplayer.audio.RealtimeVisualizerManager
import kotlin.math.cos
import kotlin.math.sin

/**
 * Corona de Plasma Líquido 360° (360° Neon Plasma Crown).
 * Anillo perimetral fluido que ondula y genera llamaradas de neón vivas
 * sincronizadas con las 32 bandas del analizador FFT en tiempo real.
 * Ultra optimizado con cero asignaciones en el ciclo de dibujado.
 */
@Composable
fun NeonPlasmaCrown(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF38BDF8),
    secondaryColor: Color = Color(0xFFEC4899),
    rayCount: Int = 28
) {
    val infiniteTransition = rememberInfiniteTransition(label = "plasma_crown")
    val rotationPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationPhase"
    )

    val cachedPath = remember { Path() }
    val brushColors = remember(primaryColor, secondaryColor) {
        listOf(
            primaryColor.copy(alpha = 0.65f),
            secondaryColor.copy(alpha = 0.75f),
            Color(0xFF8B5CF6).copy(alpha = 0.65f),
            primaryColor.copy(alpha = 0.65f)
        )
    }
    val sweepBrush = remember(brushColors) {
        Brush.sweepGradient(brushColors)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val center = Offset(centerX, centerY)
        val baseRadius = size.minDimension * 0.44f

        val visManager = RealtimeVisualizerManager.instance
        val angleStep = (2 * Math.PI) / rayCount

        cachedPath.reset()

        for (i in 0..rayCount) {
            val idx = i % rayCount
            val angle = (idx * angleStep) + rotationPhase
            val energy = if (isPlaying) visManager.getBand(idx, rayCount, isPlaying) else 0.04f
            val waveOffset = (sin(angle * 4.0 + rotationPhase * 2.0) * 0.35 + 0.65).toFloat()
            val rayLength = (energy * waveOffset * size.minDimension * 0.080f)
            val r = baseRadius + rayLength

            val x = centerX + (cos(angle) * r).toFloat()
            val y = centerY + (sin(angle) * r).toFloat()

            if (i == 0) {
                cachedPath.moveTo(x, y)
            } else {
                cachedPath.lineTo(x, y)
            }
        }
        cachedPath.close()

        drawPath(
            path = cachedPath,
            brush = sweepBrush,
            style = Stroke(
                width = if (isPlaying) 3.0f else 1.2f,
                cap = StrokeCap.Round
            )
        )
    }
}
