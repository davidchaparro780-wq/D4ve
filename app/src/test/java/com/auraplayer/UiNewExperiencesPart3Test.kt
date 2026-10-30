package com.auraplayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pruebas unitarias para las fórmulas y matemáticas de las experiencias UI:
 * - Supercar Cockpit GT: Cálculo de RPM, marcha engranada, ángulo de tacómetro y Redline.
 * - Minidisc Hi-MD: Desplazamiento radial del haz láser ultravioleta.
 * - Holo-Cockpit 2077: Invarianza orbital y pulso cuántico.
 * - Santuario Nocturno: Altura dinámica de la llama viva.
 */
class UiNewExperiencesPart3Test {

    @Test
    fun testSupercarRpmAndRedline() {
        fun calculateRpm(bassEnergy: Float): Int {
            return (1000f + (bassEnergy.coerceIn(0f, 1f) * 8000f)).toInt()
        }

        fun isRedline(rpm: Int): Boolean = rpm >= 7500

        // Ralentí
        assertEquals(1000, calculateRpm(0f))
        assertFalse(isRedline(calculateRpm(0f)))

        // Régimen medio
        assertEquals(5000, calculateRpm(0.5f))
        assertFalse(isRedline(calculateRpm(0.5f)))

        // Entrada a Redline
        val redlineRpm = calculateRpm(0.85f)
        assertTrue(redlineRpm >= 7500)
        assertTrue(isRedline(redlineRpm))

        // Corte de inyección a 9,000 RPM
        assertEquals(9000, calculateRpm(1.0f))
        assertEquals(9000, calculateRpm(2.5f))
        assertTrue(isRedline(calculateRpm(1.0f)))
    }

    @Test
    fun testSupercarGearLogic() {
        fun calculateGear(speedKmh: Int, isPlaying: Boolean): String {
            if (!isPlaying) return "N"
            return when {
                speedKmh < 50 -> "1"
                speedKmh < 90 -> "2"
                speedKmh < 140 -> "3"
                speedKmh < 190 -> "4"
                speedKmh < 240 -> "5"
                else -> "6"
            }
        }

        assertEquals("N", calculateGear(120, false))
        assertEquals("1", calculateGear(30, true))
        assertEquals("2", calculateGear(60, true))
        assertEquals("3", calculateGear(110, true))
        assertEquals("4", calculateGear(160, true))
        assertEquals("5", calculateGear(210, true))
        assertEquals("6", calculateGear(270, true))
    }

    @Test
    fun testMinidiscLaserProgress() {
        fun calculateLaserRadius(posMs: Long, durMs: Long, minR: Float = 30f, maxR: Float = 100f): Float {
            val progress = if (durMs > 0) (posMs.toFloat() / durMs.toFloat()).coerceIn(0f, 1f) else 0f
            return minR + (progress * (maxR - minR))
        }

        assertEquals(30f, calculateLaserRadius(0L, 200_000L), 0.001f)
        assertEquals(65f, calculateLaserRadius(100_000L, 200_000L), 0.001f)
        assertEquals(100f, calculateLaserRadius(200_000L, 200_000L), 0.001f)
        assertEquals(100f, calculateLaserRadius(300_000L, 200_000L), 0.001f) // Clamped
    }

    @Test
    fun testHoloCockpitOrbitInvariance() {
        val cx = 150f
        val cy = 150f
        val r = 80f

        for (angleDeg in listOf(0f, 45f, 90f, 180f, 270f)) {
            val angleRad = angleDeg * (PI / 180f).toFloat()
            val px = cx + cos(angleRad) * r
            val py = cy + sin(angleRad) * r
            val dist = kotlin.math.sqrt((px - cx) * (px - cx) + (py - cy) * (py - cy))
            assertEquals(r, dist, 0.01f)
        }
    }

    @Test
    fun testSanctuaryFlameHeight() {
        fun flameHeight(bassEnergy: Float, baseHeight: Float = 42f): Float {
            return baseHeight * (1f + bassEnergy.coerceIn(0f, 1f) * 0.25f)
        }

        assertEquals(42f, flameHeight(0f), 0.001f)
        assertEquals(47.25f, flameHeight(0.5f), 0.001f)
        assertEquals(52.5f, flameHeight(1.0f), 0.001f)
    }
}
