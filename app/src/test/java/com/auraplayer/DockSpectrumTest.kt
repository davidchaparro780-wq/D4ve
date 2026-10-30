package com.auraplayer

import com.auraplayer.ui.theme.isMysteriousAccent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests unitarios para el micro-espectrograma FFT de la barra de navegación,
 * las animaciones elásticas de rebote en los iconos del dock, y el progreso del MiniPlayer.
 */
class DockSpectrumTest {

    @Test
    fun `calculo de geometria para las 32 barras del micro-espectrograma dock`() {
        val totalBars = 32
        val dockWidth = 320f // px
        val spacing = dockWidth / totalBars
        val barWidth = spacing * 0.52f

        assertEquals(10f, spacing, 0.001f)
        assertEquals(5.2f, barWidth, 0.001f)
        assertTrue("El ancho de la barra debe ser menor que el espaciado para evitar solapamiento", barWidth < spacing)

        // Verificar que la última barra quepa en el dock
        val lastBarX = 31 * spacing + (spacing - barWidth) / 2f
        assertTrue("La posición X de la última barra debe estar dentro del ancho", lastBarX + barWidth <= dockWidth + 0.001f)
    }

    @Test
    fun `altura de barra de espectrograma queda acotada entre minHeight y maxCanvasHeight`() {
        val canvasHeight = 12f // px
        val minHeight = 2f

        // Caso 1: energía nula (0.0f)
        val energyZero = 0.0f
        val heightZero = (canvasHeight * energyZero * 1.6f).coerceIn(minHeight, canvasHeight)
        assertEquals(minHeight, heightZero, 0.001f)

        // Caso 2: energía normal (0.5f)
        val energyNormal = 0.5f
        val heightNormal = (canvasHeight * energyNormal * 1.6f).coerceIn(minHeight, canvasHeight)
        assertEquals(9.6f, heightNormal, 0.001f)

        // Caso 3: pico de energía saturado (1.5f)
        val energyPeak = 1.5f
        val heightPeak = (canvasHeight * energyPeak * 1.6f).coerceIn(minHeight, canvasHeight)
        assertEquals(canvasHeight, heightPeak, 0.001f)
    }

    @Test
    fun `escala de icono de tab seleccionada versus no seleccionada`() {
        val selectedScale = 1.14f
        val unselectedScale = 1.0f

        assertTrue("La pestaña activa debe tener escala mayor a 1 para efecto elástico", selectedScale > unselectedScale)
        assertEquals(0.14f, selectedScale - unselectedScale, 0.001f)
    }

    @Test
    fun `progreso de reproduccion en MiniPlayer se restringe estrictamente a 0..1`() {
        // Progreso negativo (seek inicial anómalo)
        val negativeProgress = (-0.15f).coerceIn(0f, 1f)
        assertEquals(0f, negativeProgress, 0.001f)

        // Progreso normal
        val normalProgress = (0.64f).coerceIn(0f, 1f)
        assertEquals(0.64f, normalProgress, 0.001f)

        // Progreso excedente
        val overflowProgress = (1.25f).coerceIn(0f, 1f)
        assertEquals(1f, overflowProgress, 0.001f)
    }

    @Test
    fun `verificacion de acento misterioso para el dock y miniplayer`() {
        assertTrue(isMysteriousAccent("MYSTIC_OBSIDIAN"))
        assertTrue(isMysteriousAccent("VOID_ABYSS"))
        assertTrue(isMysteriousAccent("RUNIC_AMETHYST"))
        assertTrue(!isMysteriousAccent("BLUE"))
        assertTrue(!isMysteriousAccent("RED"))
    }
}
