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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.sin

/**
 * Capa de Bruma Espectral y Polvo Estelar Flotante para los modos Arcanos y Enigmaticos.
 * Genera una atmosfera viva de eter cosmico con particulas luminiscentes que reaccionan
 * a la musica a 120 FPS sin sobrecargar la GPU.
 */
@Composable
fun PhantomMistOverlay(
    accentColor: Color,
    secondaryColor: Color,
    bassEnergy: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "phantom_mist_transition")

    val driftPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 12000 else 24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift_phase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val particleCount = 20

        val energyBoost = (bassEnergy * 0.35f).coerceIn(0f, 0.5f)

        for (i in 0 until particleCount) {
            val seedX = (i * 97) % 1000 / 1000f
            val seedY = (i * 131) % 1000 / 1000f
            val speedFactor = 0.5f + (i % 5) * 0.2f

            // Movimiento ondulante lento
            val offsetX = sin(driftPhase * speedFactor + (i * 1.5f)) * (24f + energyBoost * 20f)
            val offsetY = ((driftPhase * speedFactor * 15f + seedY * h) % h)

            // Modulo ajustado a positivo: evita partículas invisibles con offset negativo
            val px = ((seedX * w + offsetX) % w + w) % w
            val py = offsetY

            val alpha = ((0.15f + (i % 4) * 0.12f) + energyBoost).coerceIn(0.1f, 0.75f)
            val radius = (1.5f + (i % 3) * 1.2f) * (1f + energyBoost * 0.8f)
            val color = if (i % 2 == 0) accentColor.copy(alpha = alpha) else secondaryColor.copy(alpha = alpha * 0.8f)

            drawCircle(
                color = color,
                radius = radius,
                center = Offset(px, py)
            )
        }
    }
}
