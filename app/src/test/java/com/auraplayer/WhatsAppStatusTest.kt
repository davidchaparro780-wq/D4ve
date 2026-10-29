package com.auraplayer

import com.auraplayer.data.repository.WhatsAppStatusManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

/**
 * Pruebas unitarias para la lógica de extracción de fechas e inspección
 * de estados de WhatsApp.
 */
class WhatsAppStatusTest {

    @Test
    fun `extractDateFromFileName con formato clasico WhatsApp VID`() {
        val millis = WhatsAppStatusManager.extractDateFromFileName("VID-20230514-WA0002.mp4")
        assertNotNull(millis)
        val cal = Calendar.getInstance().apply { timeInMillis = millis!! }
        assertEquals(2023, cal.get(Calendar.YEAR))
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH))
        assertEquals(14, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `extractDateFromFileName con formato clasico WhatsApp IMG`() {
        val millis = WhatsAppStatusManager.extractDateFromFileName("IMG-20241028-WA0015.jpg")
        assertNotNull(millis)
        val cal = Calendar.getInstance().apply { timeInMillis = millis!! }
        assertEquals(2024, cal.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, cal.get(Calendar.MONTH))
        assertEquals(28, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `extractDateFromFileName con formato con guiones`() {
        val millis = WhatsAppStatusManager.extractDateFromFileName("status_2022-11-05_preview.mp4")
        assertNotNull(millis)
        val cal = Calendar.getInstance().apply { timeInMillis = millis!! }
        assertEquals(2022, cal.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, cal.get(Calendar.MONTH))
        assertEquals(5, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `extractDateFromFileName sin fecha valida devuelve null`() {
        assertNull(WhatsAppStatusManager.extractDateFromFileName("audio_sample.mp3"))
        assertNull(WhatsAppStatusManager.extractDateFromFileName("IMG_random_photo.png"))
        assertNull(WhatsAppStatusManager.extractDateFromFileName(""))
    }

    @Test
    fun `extractDateFromFileName con anio fuera de rango razonable devuelve null`() {
        assertNull(WhatsAppStatusManager.extractDateFromFileName("VID-19950101-WA0001.mp4"))
        assertNull(WhatsAppStatusManager.extractDateFromFileName("IMG-20500101-WA0001.jpg"))
    }
}
