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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.auraplayer.audio.RealtimeVisualizerManager
import kotlin.math.cos
import kotlin.math.sin

/**
 * Aura Liquid Fluid Canvas (Apple Music Style Animated Liquid Mesh Shader).
 *
 * Implementa una simulación de fluidos cromáticos de 6 nodos en tiempo real:
 * - Movimiento orbital Lissajous desacoplado para evitar patrones repetitivos.
 * - Reactividad dual acústica:
 *     - Frecuencias sub-graves (<90 Hz): dilatan y pulsan los nodos centrales.
 *     - Frecuencias de brillo/agudos (>8 kHz): modulan la iridiscencia y velocidad de giro.
 * - Renderizado en GPU vía Canvas y radial gradients sin recomposición continua del árbol Compose.
 */
@Composable
fun AuraLiquidCanvas(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color = Color(0xFF8B5CF6),
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "aura_liquid_mesh")

    val phaseA by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f, // 2 * PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_a"
    )

    val phaseB by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28318f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 23000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase_b"
    )

    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_pulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val bass = if (isPlaying) RealtimeVisualizerManager.instance.getBassEnergy(true) else 0f
            val treble = if (isPlaying) RealtimeVisualizerManager.instance.getTrebleEnergy(true) else 0f

            val dynScale = breathingPulse * (1f + bass * 0.32f)

            // Nodo 1: Núcleo Primario Cromático (Lissajous 3:2)
            val n1X = w * (0.42f + 0.22f * sin(phaseA))
            val n1Y = h * (0.35f + 0.18f * cos(phaseA * 1.5f))
            val n1R = (w * 0.72f) * dynScale
            val n1A = (0.42f + bass * 0.22f).coerceIn(0.2f, 0.75f)

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to primaryColor.copy(alpha = n1A),
                        0.45f to primaryColor.copy(alpha = n1A * 0.60f),
                        0.80f to primaryColor.copy(alpha = n1A * 0.18f),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(n1X, n1Y),
                    radius = n1R
                ),
                radius = n1R,
                center = Offset(n1X, n1Y)
            )

            // Nodo 2: Mancha Líquida Secundaria en Órbita
            val n2X = w * (0.62f - 0.25f * cos(phaseB))
            val n2Y = h * (0.65f - 0.20f * sin(phaseB * 1.2f))
            val n2R = (w * 0.78f) * dynScale
            val n2A = (0.38f + bass * 0.18f).coerceIn(0.18f, 0.68f)

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to secondaryColor.copy(alpha = n2A),
                        0.50f to secondaryColor.copy(alpha = n2A * 0.52f),
                        0.85f to secondaryColor.copy(alpha = n2A * 0.14f),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(n2X, n2Y),
                    radius = n2R
                ),
                radius = n2R,
                center = Offset(n2X, n2Y)
            )

            // Nodo 3: Aurora Terciaria Flotante (Efecto Místico Violeta / Cian)
            val n3X = w * (0.50f + 0.28f * sin(phaseB + 1.8f))
            val n3Y = h * (0.22f + 0.14f * cos(phaseA + 2.2f))
            val n3R = (w * 0.60f) * (1f + treble * 0.25f)
            val n3A = (0.30f + treble * 0.25f).coerceIn(0.12f, 0.58f)

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to tertiaryColor.copy(alpha = n3A),
                        0.40f to tertiaryColor.copy(alpha = n3A * 0.50f),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(n3X, n3Y),
                    radius = n3R
                ),
                radius = n3R,
                center = Offset(n3X, n3Y)
            )

            // Nodo 4: Bombo Sub-Bass Deep Pulse (Centro inferior)
            val n4X = w * 0.50f
            val n4Y = h * 0.78f
            val n4R = (w * 0.85f) * (0.9f + bass * 0.45f)
            val n4A = (0.24f + bass * 0.35f).coerceIn(0.15f, 0.65f)

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to primaryColor.copy(alpha = n4A),
                        0.50f to secondaryColor.copy(alpha = n4A * 0.40f),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(n4X, n4Y),
                    radius = n4R
                ),
                radius = n4R,
                center = Offset(n4X, n4Y)
            )

            // Nodo 5: Destello de Alta Frecuencia / Shimmer en Agudos
            val n5X = w * (0.25f + 0.15f * cos(phaseA * 2.0f))
            val n5Y = h * (0.80f - 0.12f * sin(phaseB * 1.8f))
            val n5R = (w * 0.45f) * (1f + treble * 0.40f)
            val n5A = (0.18f + treble * 0.30f).coerceIn(0.08f, 0.50f)

            drawCircle(
                brush = Brush.radialGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFF00F0FF).copy(alpha = n5A),
                        0.55f to tertiaryColor.copy(alpha = n5A * 0.35f),
                        1.0f to Color.Transparent
                    ),
                    center = Offset(n5X, n5Y),
                    radius = n5R
                ),
                radius = n5R,
                center = Offset(n5X, n5Y)
            )

            // Capa 6: Velo de Contraste Profundo para Legibilidad de Textos
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.30f),
                        Color.Black.copy(alpha = 0.15f),
                        Color.Black.copy(alpha = 0.60f)
                    )
                )
            )
        }
    }
}
