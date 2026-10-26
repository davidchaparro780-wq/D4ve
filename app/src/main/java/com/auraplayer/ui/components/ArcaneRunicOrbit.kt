package com.auraplayer.ui.components

import androidx.compose.animation.core.AnimRepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Anillo Orbital Celeste y Rúnico Arcano para la estética Misteriosa de DaVE Player.
 * Dibuja órbitas esotéricas, glifos geométricos, estrellas arcanas y polvo estelar
 * que rotan de forma hipnótica y reaccionan al pulso del bajo.
 */
@Composable
fun ArcaneRunicOrbit(
    accentColor: Color,
    secondaryColor: Color,
    bassEnergy: Float,
    isPlaying: Boolean,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "arcane_orbit_transition")

    // Rotación lenta del anillo exterior celeste
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 26000 else 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    // Contra-rotación del anillo interior místico
    val innerOrbitAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 38000 else 80000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_orbit_angle"
    )

    // Pulso de resplandor esotérico
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = AnimRepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    val energyBoost = (bassEnergy * 0.35f).coerceIn(0f, 0.4f)
    val dynamicAlpha = ((0.45f + energyBoost) * glowPulse).coerceIn(0.2f, 0.95f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension * 0.48f

            // 1. Nebulosa de vacío místico en el fondo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = dynamicAlpha * 0.25f),
                        secondaryColor.copy(alpha = dynamicAlpha * 0.12f),
                        Color(0xFF070414).copy(alpha = dynamicAlpha * 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.05f
                ),
                radius = baseRadius * 1.05f,
                center = center
            )

            // 2. Anillo Exterior: Órbita Rúnica punteada y Glifos Estelares
            rotate(degrees = orbitAngle, pivot = center) {
                // Anillo punteado celestial
                drawCircle(
                    color = accentColor.copy(alpha = dynamicAlpha * 0.7f),
                    radius = baseRadius,
                    center = center,
                    style = Stroke(
                        width = 1.6f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 18f), 0f)
                    )
                )

                // 8 Nodos estelares / Runas geométricas cardinales y diagonales
                for (i in 0 until 8) {
                    val angleRad = Math.toRadians((i * 45).toDouble()).toFloat()
                    val nodeX = center.x + baseRadius * cos(angleRad)
                    val nodeY = center.y + baseRadius * sin(angleRad)

                    if (i % 2 == 0) {
                        // Estrella de 4 puntas arcana
                        val starSize = 5.5f + (bassEnergy * 3.5f)
                        val starPath = Path().apply {
                            moveTo(nodeX, nodeY - starSize)
                            lineTo(nodeX + (starSize * 0.35f), nodeY)
                            lineTo(nodeX, nodeY + starSize)
                            lineTo(nodeX - (starSize * 0.35f), nodeY)
                            close()
                            moveTo(nodeX - starSize, nodeY)
                            lineTo(nodeX, nodeY + (starSize * 0.35f))
                            lineTo(nodeX + starSize, nodeY)
                            lineTo(nodeX, nodeY - (starSize * 0.35f))
                            close()
                        }
                        drawPath(
                            path = starPath,
                            color = accentColor.copy(alpha = dynamicAlpha)
                        )
                    } else {
                        // Nódulo diamante orbital
                        val diamondSize = 3.5f
                        val diamondPath = Path().apply {
                            moveTo(nodeX, nodeY - diamondSize)
                            lineTo(nodeX + diamondSize, nodeY)
                            lineTo(nodeX, nodeY + diamondSize)
                            lineTo(nodeX - diamondSize, nodeY)
                            close()
                        }
                        drawPath(
                            path = diamondPath,
                            color = secondaryColor.copy(alpha = dynamicAlpha * 0.85f)
                        )
                    }
                }
            }

            // 3. Anillo Interior: Circunferencia mística fina y fases lunares
            val innerRadius = baseRadius * 0.88f
            rotate(degrees = innerOrbitAngle, pivot = center) {
                drawCircle(
                    color = secondaryColor.copy(alpha = dynamicAlpha * 0.45f),
                    radius = innerRadius,
                    center = center,
                    style = Stroke(
                        width = 1.0f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 12f), 0f)
                    )
                )

                // 4 Marcas lunares / cardinales
                for (i in 0 until 4) {
                    val angleRad = Math.toRadians((i * 90 + 22.5).toDouble()).toFloat()
                    val moonX = center.x + innerRadius * cos(angleRad)
                    val moonY = center.y + innerRadius * sin(angleRad)

                    // Pequeña media luna o círculo estelar
                    drawCircle(
                        color = Color.White.copy(alpha = dynamicAlpha * 0.75f),
                        radius = 2.2f,
                        center = Offset(moonX, moonY)
                    )
                    drawCircle(
                        color = accentColor.copy(alpha = dynamicAlpha * 0.4f),
                        radius = 4.2f,
                        center = Offset(moonX, moonY),
                        style = Stroke(width = 0.8f)
                    )
                }
            }

            // 4. Polvo estelar cósmico místico (Micro-sparkles orbitales)
            val stardustCount = 12
            for (j in 0 until stardustCount) {
                val seed = (j * 37 + orbitAngle.toInt()) % 360
                val rad = Math.toRadians(seed.toDouble()).toFloat()
                val dist = baseRadius * (0.82f + (j % 5) * 0.04f)
                val px = center.x + dist * cos(rad)
                val py = center.y + dist * sin(rad)
                val sparkleAlpha = (0.25f + (j % 4) * 0.18f) * dynamicAlpha

                drawCircle(
                    color = if (j % 2 == 0) accentColor.copy(alpha = sparkleAlpha) else secondaryColor.copy(alpha = sparkleAlpha),
                    radius = if (j % 3 == 0) 1.8f else 1.2f,
                    center = Offset(px, py)
                )
            }
        }
    }
}
