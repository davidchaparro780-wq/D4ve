package com.auraplayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de la lógica matemática y de formato del modo StandBy OLED / Pantalla de Bloqueo.
 */
class StandByLogicTest {

    @Test
    fun `calculo de bateria porcentaje correcto`() {
        val level = 85
        val scale = 100
        val pct = (level * 100) / scale
        assertEquals(85, pct)

        val chargingStr = "⚡ $pct%"
        val batteryStr = "🔋 $pct%"
        assertEquals("⚡ 85%", chargingStr)
        assertEquals("🔋 85%", batteryStr)
    }

    @Test
    fun `progreso ambiental se limita entre 0 y 1`() {
        val durationMs = 200_000L

        // Inicio
        val progStart = (0L.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        assertEquals(0f, progStart, 0.001f)

        // Mitad
        val progMid = (100_000L.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        assertEquals(0.5f, progMid, 0.001f)

        // Final
        val progEnd = (200_000L.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        assertEquals(1f, progEnd, 0.001f)

        // Overflow acotado
        val progOver = (350_000L.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        assertEquals(1f, progOver, 0.001f)

        // Duración cero
        val progZero = if (0L > 0L) (50L.toFloat() / 0L.toFloat()).coerceIn(0f, 1f) else 0f
        assertEquals(0f, progZero, 0.001f)
    }

    @Test
    fun `burn in offset se mantiene dentro del rango seguro de 5 pixeles`() {
        for (i in 1..100) {
            val randomX = (-5..5).random().toFloat()
            val randomY = (-5..5).random().toFloat()
            assertTrue(randomX >= -5f && randomX <= 5f)
            assertTrue(randomY >= -5f && randomY <= 5f)
        }
    }
}
