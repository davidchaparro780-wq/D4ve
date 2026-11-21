package com.auraplayer.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Fondo Ambilight Fluido Reactivo (Aura Liquid Fluid Mesh Canvas).
 * Puente retrocompatible de alto rendimiento que delega a AuraLiquidCanvas
 * para renderizado GPU orgánico estilo Apple Music.
 */
@Composable
fun FluidAmbilightGlow(
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color = Color(0xFF8B5CF6),
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier
) {
    AuraLiquidCanvas(
        primaryColor = primaryColor,
        secondaryColor = secondaryColor,
        tertiaryColor = tertiaryColor,
        isPlaying = isPlaying,
        modifier = modifier
    )
}
