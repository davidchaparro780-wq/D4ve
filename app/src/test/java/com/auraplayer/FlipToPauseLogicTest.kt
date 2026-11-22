package com.auraplayer

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class FlipToPauseLogicTest {

    private fun isPhoneFaceDown(x: Float, y: Float, z: Float): Boolean {
        return z < -7.0f && abs(x) < 4.5f && abs(y) < 4.5f
    }

    @Test
    fun `telefono sobre mesa boca abajo activa pausa`() {
        // Z ≈ -9.8 m/s² (hacia el suelo desde la pantalla)
        val x = 0.2f
        val y = -0.1f
        val z = -9.81f
        assertTrue(isPhoneFaceDown(x, y, z))
    }

    @Test
    fun `telefono sobre mesa boca arriba no activa pausa`() {
        // Z ≈ +9.8 m/s² (pantalla hacia el techo)
        val x = 0.1f
        val y = 0.2f
        val z = 9.81f
        assertFalse(isPhoneFaceDown(x, y, z))
    }

    @Test
    fun `telefono sostenido en mano en vertical no activa pausa`() {
        val x = 0.5f
        val y = 9.2f
        val z = 1.2f
        assertFalse(isPhoneFaceDown(x, y, z))
    }

    @Test
    fun `telefono inclinado diagonalmente no activa falso positivo`() {
        val x = 6.0f
        val y = 6.0f
        val z = -5.0f
        assertFalse(isPhoneFaceDown(x, y, z))
    }
}
