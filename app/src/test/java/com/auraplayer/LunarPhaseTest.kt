package com.auraplayer

import com.auraplayer.audio.LunarPhaseManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests del cálculo de fase lunar (referencia: luna nueva 6 enero 2000 UTC).
 */
class LunarPhaseTest {

    @Test
    fun `luna nueva en el epoch de referencia`() {
        val phase = LunarPhaseManager.getPhaseAt(947182440_000L)
        assertEquals("Luna Nueva", phase.phaseName)
        assertTrue("Iluminación de luna nueva debe ser ~0%", phase.illuminationPercent <= 1)
    }

    @Test
    fun `luna llena medio ciclo despues`() {
        // Medio mes sinódico (~15.13 días) tras el epoch
        val halfCycleMs = (29.53058867 * 86400.0 * 1000.0 / 2.0).toLong()
        val phase = LunarPhaseManager.getPhaseAt(947182440_000L + halfCycleMs)
        assertEquals("Luna Llena", phase.phaseName)
        assertTrue("Iluminación de luna llena debe ser ~100%", phase.illuminationPercent >= 99)
    }

    @Test
    fun `cuarto creciente a un cuarto de ciclo`() {
        val quarterCycleMs = (29.53058867 * 86400.0 * 1000.0 / 4.0).toLong()
        val phase = LunarPhaseManager.getPhaseAt(947182440_000L + quarterCycleMs)
        assertEquals("Cuarto Creciente", phase.phaseName)
    }

    @Test
    fun `cuarto menguante a tres cuartos de ciclo`() {
        val threeQuartersMs = (29.53058867 * 86400.0 * 1000.0 * 3.0 / 4.0).toLong()
        val phase = LunarPhaseManager.getPhaseAt(947182440_000L + threeQuartersMs)
        assertEquals("Cuarto Menguante", phase.phaseName)
    }

    @Test
    fun `el ciclo se repite tras un mes sinodico completo`() {
        val fullCycleMs = (29.53058867 * 86400.0 * 1000.0).toLong()
        val atEpoch = LunarPhaseManager.getPhaseAt(947182440_000L)
        // Buffer de 5 s: el truncamiento a milisegundos deja el ciclo exacto en 99.99996% (menguante)
        val oneCycleLater = LunarPhaseManager.getPhaseAt(947182440_000L + fullCycleMs + 5000L)
        assertEquals(atEpoch.phaseName, oneCycleLater.phaseName)
    }

    @Test
    fun `iluminacion siempre en rango 0 a 100`() {
        // Muestrea 60 puntos a lo largo de un ciclo
        val cycleMs = (29.53058867 * 86400.0 * 1000.0).toLong()
        for (i in 0 until 60) {
            val phase = LunarPhaseManager.getPhaseAt(947182440_000L + cycleMs * i / 60)
            assertTrue("Iluminación fuera de rango: ${phase.illuminationPercent}", phase.illuminationPercent in 0..100)
        }
    }
}
