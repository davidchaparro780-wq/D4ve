package com.auraplayer

import com.auraplayer.data.model.computeAudioFormatBadge
import com.auraplayer.data.model.formatDurationMs
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests del formateo de duraciones y las insignias de calidad de audio
 * que se muestran en toda la interfaz de la biblioteca.
 */
class MediaFormattingTest {

    // ---- formatDurationMs ----

    @Test
    fun `cero formatea como 00-00`() {
        assertEquals("00:00", formatDurationMs(0L))
    }

    @Test
    fun `negativo se recorta a 00-00`() {
        assertEquals("00:00", formatDurationMs(-5000L))
    }

    @Test
    fun `menos de un minuto`() {
        assertEquals("00:59", formatDurationMs(59_999L))
    }

    @Test
    fun `minuto clasico`() {
        assertEquals("05:05", formatDurationMs(305_000L))
    }

    @Test
    fun `mas de una hora usa formato H-MM-SS`() {
        assertEquals("1:02:03", formatDurationMs(3_723_000L))
    }

    @Test
    fun `horas con decenas`() {
        assertEquals("12:00:00", formatDurationMs(43_200_000L))
    }

    // ---- computeAudioFormatBadge ----

    @Test
    fun `flac se detecta como lossless`() {
        assertEquals("FLAC", computeAudioFormatBadge("/music/cancion.flac", 1L, 1L))
    }

    @Test
    fun `wav y alac se detectan`() {
        assertEquals("WAV", computeAudioFormatBadge("/a.WAV", 1L, 1L))
        assertEquals("ALAC", computeAudioFormatBadge("/a.alac", 1L, 1L))
    }

    @Test
    fun `mp3 con bitrate 320 o mas marca 320k`() {
        // 320 kbps exactos: 2,400,000 bytes * 8 / 60 s / 1000 = 320
        assertEquals("320k", computeAudioFormatBadge("/a.mp3", 2_400_000L, 60_000L))
    }

    @Test
    fun `mp3 con bitrate 256k`() {
        // 2,097,152 bytes * 8 / 60 / 1000 = 279 -> rango 256k
        assertEquals("256k", computeAudioFormatBadge("/a.mp3", 2_097_152L, 60_000L))
    }

    @Test
    fun `mp3 de bitrate bajo marca MP3`() {
        // 128 kbps: 960,000 bytes * 8 / 60 / 1000 = 128
        assertEquals("MP3", computeAudioFormatBadge("/a.mp3", 960_000L, 60_000L))
    }

    @Test
    fun `mp3 sin tamano o duracion conocida cae a MP3`() {
        assertEquals("MP3", computeAudioFormatBadge("/a.mp3", 0L, 60_000L))
        assertEquals("MP3", computeAudioFormatBadge("/a.mp3", 2_400_000L, 0L))
    }

    @Test
    fun `extensiones de streaming`() {
        assertEquals("OPUS", computeAudioFormatBadge("/a.ogg", 1L, 1L))
        assertEquals("OPUS", computeAudioFormatBadge("/a.opus", 1L, 1L))
        assertEquals("AAC", computeAudioFormatBadge("/a.m4a", 1L, 1L))
    }

    @Test
    fun `extension desconocida se muestra en mayusculas`() {
        assertEquals("WV", computeAudioFormatBadge("/a.wv", 1L, 1L))
    }

    @Test
    fun `sin extension devuelve vacio`() {
        assertEquals("", computeAudioFormatBadge("/carpeta/sinsextension", 1L, 1L))
    }
}
