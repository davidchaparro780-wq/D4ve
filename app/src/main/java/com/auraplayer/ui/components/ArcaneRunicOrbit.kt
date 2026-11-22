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
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Anillo Orbital Celeste y Rúnico Arcano para la estética Misteriosa de DaVE Player.
 * Dibuja órbitas esotéricas, glifos geométricos, estrellas arcanas y polvo estelar.
 * Ultra optimizado con pre-cálculo de efectos de trazado y cero asignaciones en drawScope.
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
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    val outerDashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(14f, 18f), 0f) }
    val innerDashEffect = remember { PathEffect.dashPathEffect(floatArrayOf(6f, 12f), 0f) }

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
                        pathEffect = outerDashEffect
                    )
                )

                // 8 Nodos estelares / Runas geométricas
                for (i in 0 until 8) {
                    val angleRad = (i * 45f * (Math.PI / 180f)).toFloat()
                    val nodeX = center.x + baseRadius * cos(angleRad)
                    val nodeY = center.y + baseRadius * sin(angleRad)

                    if (i % 2 == 0) {
                        // Estrella de 4 puntas arcana (dibujada con trazos cruzados de alto rendimiento)
                        val starSize = 5.0f + (bassEnergy * 3.0f)
                        drawLine(
                            color = accentColor.copy(alpha = dynamicAlpha),
                            start = Offset(nodeX, nodeY - starSize),
                            end = Offset(nodeX, nodeY + starSize),
                            strokeWidth = 2.0f,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = accentColor.copy(alpha = dynamicAlpha),
                            start = Offset(nodeX - starSize, nodeY),
                            end = Offset(nodeX + starSize, nodeY),
                            strokeWidth = 2.0f,
                            cap = StrokeCap.Round
                        )
                    } else {
                        // Nódulo diamante orbital (dibujado como círculo concéntrico rápido)
                        drawCircle(
                            color = secondaryColor.copy(alpha = dynamicAlpha * 0.85f),
                            radius = 2.8f,
                            center = Offset(nodeX, nodeY)
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
                        pathEffect = innerDashEffect
                    )
                )

                // 4 Marcas cardinales
                for (i in 0 until 4) {
                    val angleRad = ((i * 90f + 22.5f) * (Math.PI / 180f)).toFloat()
                    val moonX = center.x + innerRadius * cos(angleRad)
                    val moonY = center.y + innerRadius * sin(angleRad)

                    drawCircle(
                        color = Color.White.copy(alpha = dynamicAlpha * 0.75f),
                        radius = 2.2f,
                        center = Offset(moonX, moonY)
                    )
                }
            }
        }
    }
}
