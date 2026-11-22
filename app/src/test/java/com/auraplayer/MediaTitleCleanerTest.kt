package com.auraplayer

import com.auraplayer.util.MediaTitleCleaner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaTitleCleanerTest {

    @Test
    fun `limpieza de etiquetas de video y audio oficial`() {
        val r1 = MediaTitleCleaner.clean("Bad Bunny - Tití Me Preguntó (Official Video)")
        assertEquals("Tití Me Preguntó", r1.title)
        assertEquals("Bad Bunny", r1.artist)
        assertTrue(r1.wasCleaned)

        val r2 = MediaTitleCleaner.clean("Coldplay - Yellow [Official Audio]")
        assertEquals("Yellow", r2.title)
        assertEquals("Coldplay", r2.artist)

        val r3 = MediaTitleCleaner.clean("Duki - She Don't Give a FO (Video Oficial)")
        assertEquals("She Don't Give a FO", r3.title)
        assertEquals("Duki", r3.artist)
    }

    @Test
    fun `limpieza de sitios de descarga y extensiones`() {
        val r1 = MediaTitleCleaner.clean("y2mate.com - Queen - Bohemian Rhapsody [HD].mp3")
        assertEquals("Bohemian Rhapsody", r1.title)
        assertEquals("Queen", r1.artist)

        val r2 = MediaTitleCleaner.clean("snaptube_Adele - Rolling In The Deep [1080p].m4a")
        assertEquals("Rolling In The Deep", r2.title)
        assertEquals("Adele", r2.artist)
    }

    @Test
    fun `limpieza de guiones bajos como espacios`() {
        val r1 = MediaTitleCleaner.clean("Rosalia_-_Despecha_(Audio_Oficial).mp3")
        assertEquals("Despecha", r1.title)
        assertEquals("Rosalia", r1.artist)
    }

    @Test
    fun `preserva artista si ya coincide`() {
        val r1 = MediaTitleCleaner.clean("Imagine Dragons - Believer (Official Music Video)", "Imagine Dragons")
        assertEquals("Believer", r1.title)
    }
}
