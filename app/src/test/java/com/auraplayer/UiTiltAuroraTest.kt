package com.auraplayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para el escalado y alfa de brillo dinámico optimizado a 120 FPS
 * y la lógica de dial háptico en el seekbar.
 */
class UiTiltAuroraTest {

    @Test
    fun `calculo de alfa de brillo ambiental se acota entre 0,2 y 0,95`() {
        fun computeGlowAlpha(bassEnergy: Float, isPlaying: Boolean): Float {
            return if (isPlaying) (0.35f + (bassEnergy * 0.55f)).coerceIn(0.2f, 0.95f) else 0.2f
        }

        // Caso 1: Pausado
        assertEquals(0.2f, computeGlowAlpha(0.8f, false), 0.001f)

        // Caso 2: Bajo mínimo
        assertEquals(0.35f, computeGlowAlpha(0.0f, true), 0.001f)

        // Caso 3: Bajo medio (0.5f)
        assertEquals(0.625f, computeGlowAlpha(0.5f, true), 0.001f)

        // Caso 4: Bajo extremo saturado (2.0f)
        assertEquals(0.95f, computeGlowAlpha(2.0f, true), 0.001f)
    }

    @Test
    fun `deteccion de hitos de cuarto de compas para hapticos firmes en seekbar`() {
        val totalBars = 42

        val q1 = totalBars / 4      // 10
        val q2 = totalBars / 2      // 21
        val q3 = (totalBars * 3) / 4 // 31

        fun isFirmClick(idx: Int): Boolean = (idx == q1 || idx == q2 || idx == q3)

        assertTrue("El índice 10 debe detonar un click firme", isFirmClick(10))
        assertTrue("El índice 21 debe detonar un click firme (50% de la pista)", isFirmClick(21))
        assertTrue("El índice 31 debe detonar un click firme (75% de la pista)", isFirmClick(31))
        assertTrue("El índice regular no debe ser click firme", !isFirmClick(5))
        assertTrue("El índice regular no debe ser click firme", !isFirmClick(22))
    }
}
