package com.auraplayer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Texto arcano de desplazamiento automatico.
 * Muestra mensajes esotericos criptograficos rotando con efecto de
 * desvanecimiento lateral. Ideal para la franja inferior del reproductor
 * en temas oscuros.
 */
@Composable
fun NecronomicScrollText(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val messages = remember {
        listOf(
            "ᛟ RESONANCIA DEL VELO COSMICO ACTIVADA",
            "ᚢ FRECUENCIAS DEL UMBRAL ENTRE MUNDOS",
            "ᛏ ALINEACION DE CHAKRAS SONICOS EN CURSO",
            "ᚱ CODIGOS SAGRADOS DE PITAGORAS ACTIVOS",
            "ᛈ CAMPO MORFOGENETICO SINTONIZADO",
            "ᚨ VIBRACION 432 Hz: NATURALEZA INFINITA",
            "ᛉ GEOMETRIA SAGRADA DEL SONIDO PURO"
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "necro_scroll")

    // Cycle through messages every 4s
    val cycleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = messages.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = messages.size * 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cycle"
    )

    // Fade in/out per message
    val fadeProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fade"
    )

    val messageIndex = cycleProgress.toInt().coerceIn(0, messages.size - 1)
    val alpha = when {
        fadeProgress < 0.12f -> fadeProgress / 0.12f
        fadeProgress > 0.85f -> (1f - fadeProgress) / 0.15f
        else -> 1f
    }.coerceIn(0f, 1f) * 0.9f

    Box(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = messages[messageIndex],
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = accentColor.copy(alpha = alpha),
            maxLines = 1,
            letterSpacing = 1.8.sp,
            modifier = Modifier.graphicsLayer { this.alpha = alpha }
        )
    }
}
