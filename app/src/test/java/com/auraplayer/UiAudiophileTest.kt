package com.auraplayer

import com.auraplayer.data.model.computeAudioFormatBadge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests de la lógica del badge audiófilo Hi-Fi, temporizador zen y física del tilt 3D.
 */
class UiAudiophileTest {

    @Test
    fun `formato de tiempo para pill de sleep timer`() {
        val seconds = 1523 // 25 min 23 sec
        val min = seconds / 60
        val sec = seconds % 60
        val formatted = String.format("%02d:%02d", min, sec)
        assertEquals("25:23", formatted)

        val textPill = "⏳ $formatted (+10m)"
        assertEquals("⏳ 25:23 (+10m)", textPill)
    }

    @Test
    fun `coercion de angulos de inclinacion 3d parallax dentro del rango seguro`() {
        var tiltX = 0f
        var tiltY = 0f

        // Arrastre grande hacia abajo
        val dragAmountY = 300f
        tiltX = (tiltX - dragAmountY * 0.08f).coerceIn(-14f, 14f)
        assertEquals(-14f, tiltX, 0.001f)

        // Arrastre grande hacia la derecha
        val dragAmountX = 400f
        tiltY = (tiltY + dragAmountX * 0.08f).coerceIn(-14f, 14f)
        assertEquals(14f, tiltY, 0.001f)

        // Micro-arrastre moderado
        tiltX = 0f
        tiltX = (tiltX - 50f * 0.08f).coerceIn(-14f, 14f)
        assertEquals(-4f, tiltX, 0.001f)
    }

    @Test
    fun `badge de audio audiophile detecta flac y mp3 de alta resolucion`() {
        val flacBadge = computeAudioFormatBadge("/music/track.flac", 30_000_000L, 200_000L)
        assertEquals("FLAC", flacBadge)

        // MP3 320k
        val mp3Size = 8_000_000L
        val mp3DurationMs = 200_000L
        val mp3Badge = computeAudioFormatBadge("/music/track.mp3", mp3Size, mp3DurationMs)
        assertEquals("320k", mp3Badge)
    }
}
