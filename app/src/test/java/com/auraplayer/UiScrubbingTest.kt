package com.auraplayer

import com.auraplayer.data.model.formatDurationMs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de la lógica matemática de interacción táctil y scrubbing tooltip en la UI.
 */
class UiScrubbingTest {

    @Test
    fun `calculo de tiempo en scrubbing tooltip`() {
        val durationMs = 180_000L // 3 minutos
        val seekbarWidth = 1000f

        // Inicio
        val timeStart = ((0f / seekbarWidth).coerceIn(0f, 1f) * durationMs).toLong()
        assertEquals("00:00", formatDurationMs(timeStart))

        // Mitad (500px)
        val timeMid = ((500f / seekbarWidth).coerceIn(0f, 1f) * durationMs).toLong()
        assertEquals("01:30", formatDurationMs(timeMid))

        // 75% (750px)
        val time75 = ((750f / seekbarWidth).coerceIn(0f, 1f) * durationMs).toLong()
        assertEquals("02:15", formatDurationMs(time75))

        // Fin (1000px)
        val timeEnd = ((1000f / seekbarWidth).coerceIn(0f, 1f) * durationMs).toLong()
        assertEquals("03:00", formatDurationMs(timeEnd))

        // Desborde fuera de la pantalla
        val timeOverflow = ((1500f / seekbarWidth).coerceIn(0f, 1f) * durationMs).toLong()
        assertEquals("03:00", formatDurationMs(timeOverflow))
    }

    @Test
    fun `acotamiento de la burbuja tooltip dentro de los limites de pantalla`() {
        val seekbarWidth = 400f
        val bubbleHalfWidth = 28f
        val maxOffset = seekbarWidth - (bubbleHalfWidth * 2)

        // Toque en el borde izquierdo (0)
        val clampedStart = (0f - bubbleHalfWidth).coerceIn(0f, maxOffset)
        assertEquals(0f, clampedStart, 0.001f)

        // Toque en el centro (200)
        val clampedCenter = (200f - bubbleHalfWidth).coerceIn(0f, maxOffset)
        assertEquals(172f, clampedCenter, 0.001f)

        // Toque en el borde derecho (400)
        val clampedEnd = (400f - bubbleHalfWidth).coerceIn(0f, maxOffset)
        assertEquals(maxOffset, clampedEnd, 0.001f)
    }

    @Test
    fun `swipe up miniplayer acumulador activa en umbral de 40px`() {
        var accumulatedDragY = 0f
        var triggered = false

        val frameDeltas = listOf(-5f, -8f, -12f, -16f) // suma = -41f
        for (delta in frameDeltas) {
            accumulatedDragY += delta
            if (accumulatedDragY < -40f) {
                triggered = true
                accumulatedDragY = 0f
                break
            }
        }

        assertTrue("El gesto swipe-up debe haberse disparado al superar los 40px", triggered)
        assertEquals(0f, accumulatedDragY, 0.001f)
    }
}
