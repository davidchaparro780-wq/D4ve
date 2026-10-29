package com.auraplayer

import com.auraplayer.audio.EqualizerManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para la validación de presets del ecualizador de 10 bandas.
 */
class EqualizerPresetsTest {

    @Test
    fun `preset flat pone todas las bandas en 0 dB`() {
        val eq = EqualizerManager.instance
        eq.applyPreset(0)
        for (i in 0 until 10) {
            assertEquals("La banda $i debe ser 0 en preset Flat", 0f, eq.bandLevels[i], 0.001f)
        }
        assertEquals(0, eq.currentPresetIndex)
    }

    @Test
    fun `preset cyber bass refuerza las frecuencias subgraves`() {
        val eq = EqualizerManager.instance
        eq.applyPreset(1) // Cyber Bass
        assertTrue("Subgrave 31Hz debe estar amplificado", eq.bandLevels[0] >= 7f)
        assertTrue("Grave 62Hz debe estar amplificado", eq.bandLevels[1] >= 6f)
        assertEquals(1, eq.currentPresetIndex)
    }

    @Test
    fun `preset airpods pro activa perfil balanceado`() {
        val eq = EqualizerManager.instance
        eq.applyPreset(8) // AirPods Pro
        assertEquals(8, eq.currentPresetIndex)
        assertTrue("Subgrave debe tener refuerzo moderado", eq.bandLevels[0] > 0f)
        assertTrue("Agudos aire deben tener presencia", eq.bandLevels[9] > 0f)
    }

    @Test
    fun `preset sony 1000xm refuerza graves y agudos`() {
        val eq = EqualizerManager.instance
        eq.applyPreset(9) // Sony 1000XM
        assertEquals(9, eq.currentPresetIndex)
        assertTrue("Graves reforzados", eq.bandLevels[0] >= 5f)
        assertTrue("Brillo de agudos", eq.bandLevels[9] >= 5f)
    }

    @Test
    fun `ajustar banda individual marca preset como personalizado`() {
        val eq = EqualizerManager.instance
        eq.applyPreset(0)
        assertEquals(0, eq.currentPresetIndex)
        eq.setBandLevel(2, 6f)
        assertEquals(-1, eq.currentPresetIndex)
        assertEquals(6f, eq.bandLevels[2], 0.001f)
    }
}
