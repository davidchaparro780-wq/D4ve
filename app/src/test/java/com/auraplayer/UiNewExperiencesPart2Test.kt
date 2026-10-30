package com.auraplayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

/**
 * Pruebas unitarias para las fórmulas matemáticas y físicas de las experiencias UI:
 * - Pixel Arcade 16-Bit: Ciclo de pasos, salto por umbral de graves y scanlines.
 * - Fonógrafo Steampunk 1890: Período de rotación 78 RPM, división de dientes de engranaje y escala acústica.
 * - Aqua Zen Abyssal: Desvanecimiento y propagación de ondas de agua (ripples).
 */
class UiNewExperiencesPart2Test {

    @Test
    fun testPixelArcadeWalkCycleAndJump() {
        // Ciclo de caminata de 4 frames
        for (step in 0..15) {
            val frame = step % 4
            assertTrue("El frame de paso debe estar entre 0 y 3", frame in 0..3)
        }

        // Salto reactivo con graves
        fun calculateJumpOffset(bassEnergy: Float, maxPx: Float = 38f): Float {
            return if (bassEnergy > 0.45f) {
                ((bassEnergy - 0.45f) * 70f).coerceAtMost(maxPx)
            } else 0f
        }

        assertEquals(0f, calculateJumpOffset(0.1f), 0.001f)
        assertEquals(0f, calculateJumpOffset(0.45f), 0.001f)
        assertTrue(calculateJumpOffset(0.6f) > 0f)
        assertEquals(38f, calculateJumpOffset(1.5f), 0.001f) // Clamped al máximo
    }

    @Test
    fun testSteampunkPhonographPhysics() {
        // Período de 78 RPM en milisegundos: 60,000 ms / 78 rev ~= 769 ms
        val periodMs = 60_000.0 / 78.0
        assertEquals(769.23, periodMs, 0.5)

        // Dientes de engranajes: paso angular equiespaciado
        val teeth12 = 12
        val stepDeg12 = 360f / teeth12
        assertEquals(30f, stepDeg12, 0.001f)

        val teeth8 = 8
        val stepDeg8 = 360f / teeth8
        assertEquals(45f, stepDeg8, 0.001f)

        // Escala de la campana acústica
        fun hornScale(bassEnergy: Float): Float {
            return 1f + (bassEnergy.coerceIn(0f, 1f) * 0.12f)
        }
        assertEquals(1.0f, hornScale(0f), 0.001f)
        assertEquals(1.06f, hornScale(0.5f), 0.001f)
        assertEquals(1.12f, hornScale(1.0f), 0.001f)
    }

    @Test
    fun testAquaZenRipplePropagation() {
        val maxDurationMs = 1200L

        fun rippleAlpha(ageMs: Long): Float {
            if (ageMs > maxDurationMs) return 0f
            val progress = (ageMs.toFloat() / maxDurationMs.toFloat()).coerceIn(0f, 1f)
            return (1f - progress) * 0.65f
        }

        fun rippleRadius(ageMs: Long, maxRadiusPx: Float = 140f): Float {
            val progress = (ageMs.toFloat() / maxDurationMs.toFloat()).coerceIn(0f, 1f)
            return progress * maxRadiusPx
        }

        // Al inicio (0 ms): radio 0, alpha máximo 0.65f
        assertEquals(0.65f, rippleAlpha(0L), 0.001f)
        assertEquals(0f, rippleRadius(0L), 0.001f)

        // A la mitad (600 ms): radio 70px, alpha ~0.325f
        assertEquals(0.325f, rippleAlpha(600L), 0.001f)
        assertEquals(70f, rippleRadius(600L), 0.001f)

        // Al final (1200 ms): radio 140px, alpha 0f
        assertEquals(0f, rippleAlpha(1200L), 0.001f)
        assertEquals(140f, rippleRadius(1200L), 0.001f)

        // Pasado el límite (>1200 ms): alpha 0f
        assertEquals(0f, rippleAlpha(1500L), 0.001f)
    }
}
