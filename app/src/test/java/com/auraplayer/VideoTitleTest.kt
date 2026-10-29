package com.auraplayer

import com.auraplayer.ui.screens.cleanVideoTitle
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pruebas unitarias para la sanitización y embellecimiento de títulos de video.
 */
class VideoTitleTest {

    @Test
    fun `titulo normal quita extensiones comunes`() {
        assertEquals("Concierto en Vivo", cleanVideoTitle("Concierto en Vivo.mp4", "Camera"))
        assertEquals("Trailer Pelicula", cleanVideoTitle("Trailer Pelicula.mkv", "Downloads"))
        assertEquals("Tutorial Kotlin", cleanVideoTitle("Tutorial Kotlin.webm", "ScreenRecorder"))
        assertEquals("Viejo clip", cleanVideoTitle("Viejo clip.3gp", "WhatsApp Video"))
    }

    @Test
    fun `nombre hash con carpeta reconocible usa nombre de carpeta`() {
        val hashName = "0e46a9d0ce7ac6fe2189ea9a11b9a234.mp4"
        assertEquals("Video WhatsApp Video", cleanVideoTitle(hashName, "WhatsApp Video"))
        assertEquals("Video Reels", cleanVideoTitle(hashName, "Reels"))
    }

    @Test
    fun `nombre hash con carpeta generica usa prefijo con hash recortado`() {
        val hashName = "a1b2c3d4e5f6071829.mp4"
        assertEquals("Video #A1B2C3", cleanVideoTitle(hashName, "Videos"))
        assertEquals("Video #A1B2C3", cleanVideoTitle(hashName, "0"))
        assertEquals("Video #A1B2C3", cleanVideoTitle(hashName, ""))
    }

    @Test
    fun `titulo corto con espacios se preserva intacto`() {
        assertEquals("Mi Video Familiar", cleanVideoTitle("Mi Video Familiar", "Camera"))
    }
}
