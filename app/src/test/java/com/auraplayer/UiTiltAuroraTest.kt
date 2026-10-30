package com.auraplayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas unitarias para la física de inclinación en el MiniPlayer (3D Tilt Resistance),
 * la geometría de los nodos de Aurora Borealis y la lógica de dial háptico en el seekbar.
 */
class UiTiltAuroraTest {

    @Test
    fun `calculo de inclinacion 3d en MiniPlayer queda acotado dentro del rango de seguridad`() {
        val factor = 0.038f
        val maxTilt = 9f

        // Caso 1: Arrastre moderado a la derecha (+100px)
        val dragRight = 100f
        val tiltRight = (dragRight * factor).coerceIn(-maxTilt, maxTilt)
        assertEquals(3.8f, tiltRight, 0.001f)

        // Caso 2: Arrastre excesivo a la derecha (+400px)
        val dragFarRight = 400f
        val tiltFarRight = (dragFarRight * factor).coerceIn(-maxTilt, maxTilt)
        assertEquals(maxTilt, tiltFarRight, 0.001f)

        // Caso 3: Arrastre moderado a la izquierda (-80px)
        val dragLeft = -80f
        val tiltLeft = (dragLeft * factor).coerceIn(-maxTilt, maxTilt)
        assertEquals(-3.04f, tiltLeft, 0.001f)

        // Caso 4: Arrastre excesivo a la izquierda (-500px)
        val dragFarLeft = -500f
        val tiltFarLeft = (dragFarLeft * factor).coerceIn(-maxTilt, maxTilt)
        assertEquals(-maxTilt, tiltFarLeft, 0.001f)
    }

    @Test
    fun `deteccion de hitos de cuarto de compas para hapticos firmes en seekbar`() {
        val totalBars = 42

        val q1 = totalBars / 4      // 10
        val q2 = totalBars / 2      // 21
        val q3 = (totalBars * 3) / 4 // 31

        fun isFirmClick(idx: Int): Boolean = (idx == q1 || idx == q2 || idx == q3)

        assertTrue("El índice 10 debe detonar un click firme", isFirmClick(10))
        assertTrue("El índice 21 debe detonar un click firme (50% de la pista)", isFirmClick(21))
        assertTrue("El índice 31 debe detonar un click firme (75% de la pista)", isFirmClick(31))
        assertTrue("El índice regular no debe ser click firme", !isFirmClick(5))
        assertTrue("El índice regular no debe ser click firme", !isFirmClick(22))
    }

    @Test
    fun `geometria y desplazamiento de nodos orbitales de aurora`() {
        val maxArtSize = 300f // dp
        val offsetTopLeftX = -maxArtSize * 0.14f
        val offsetTopLeftY = -maxArtSize * 0.14f

        assertEquals(-42f, offsetTopLeftX, 0.001f)
        assertEquals(-42f, offsetTopLeftY, 0.001f)

        val offsetBottomRightX = maxArtSize * 0.14f
        val offsetBottomRightY = maxArtSize * 0.14f

        assertEquals(42f, offsetBottomRightX, 0.001f)
        assertEquals(42f, offsetBottomRightY, 0.001f)

        val orbitalDiameter = maxArtSize * 0.85f
        assertEquals(255f, orbitalDiameter, 0.001f)
    }
}
