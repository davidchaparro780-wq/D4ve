package com.auraplayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pruebas unitarias para las fórmulas y matemáticas de las nuevas experiencias de UI:
 * - Modo Lofi Chill Cafe: Ondas de vapor y mapeo de lluvia.
 * - Aura DNA Galaxia Musical: Posicionamiento polar en espiral logarítmica.
 * - Modo Boombox 80s: Balística de agujas VU meter, formateador de cinta y límites.
 */
class UiNewExperiencesTest {

    @Test
    fun testAnalogVuMeterNeedleAngleClamping() {
        // En AnalogVuMeter:
        // angleDeg = -45f + (clampedEnergy * 90f)
        // Coerce energy de 0f a 1f
        fun calculateNeedleAngleDeg(energy: Float): Float {
            val clamped = energy.coerceIn(0f, 1f)
            return -45f + (clamped * 90f)
        }

        // Caso mínimo (silencio / energía 0)
        assertEquals(-45f, calculateNeedleAngleDeg(0.0f), 0.001f)
        assertEquals(-45f, calculateNeedleAngleDeg(-0.5f), 0.001f)

        // Caso central (50% de energía = 0 grados / posición vertical)
        assertEquals(0f, calculateNeedleAngleDeg(0.5f), 0.001f)

        // Caso máximo (100% de energía = +45 grados hacia la zona roja)
        assertEquals(45f, calculateNeedleAngleDeg(1.0f), 0.001f)
        assertEquals(45f, calculateNeedleAngleDeg(2.5f), 0.001f)
    }

    @Test
    fun testAnalogVuMeterTipCoordinates() {
        // Verifica que la punta de la aguja mantenga la longitud del vector invariable
        val pivotX = 100f
        val pivotY = 150f
        val needleLen = 80f

        for (energy in listOf(0f, 0.25f, 0.5f, 0.75f, 1f)) {
            val clamped = energy.coerceIn(0f, 1f)
            val angleDeg = -45f + (clamped * 90f)
            val angleRad = (angleDeg - 90f) * (PI / 180f).toFloat()

            val tipX = pivotX + cos(angleRad) * needleLen
            val tipY = pivotY + sin(angleRad) * needleLen

            val dist = kotlin.math.sqrt((tipX - pivotX) * (tipX - pivotX) + (tipY - pivotY) * (tipY - pivotY))
            assertEquals(needleLen, dist, 0.01f)
        }
    }

    @Test
    fun testCassetteCounterFormatting() {
        fun formatCounter(positionMs: Long): String {
            val totalSecs = positionMs / 1000
            val countVal = (totalSecs % 9999).toInt()
            return String.format("%04d", countVal)
        }

        assertEquals("0000", formatCounter(0L))
        assertEquals("0005", formatCounter(5_200L))
        assertEquals("0123", formatCounter(123_000L))
        assertEquals("9998", formatCounter(9_998_000L))
        // Desbordamiento a 0000 tras 9999
        assertEquals("0000", formatCounter(9_999_000L))
    }

    @Test
    fun testGalaxyStarSpiralCoordinates() {
        // En AuraGalaxyDialog:
        // angle = (idx / total) * 6 * PI
        // armRadius = 110f + (idx * 16f)
        val total = 100
        for (idx in 0..10) {
            val angle = (idx.toFloat() / total.toFloat()) * (Math.PI * 6.0).toFloat()
            val armRadius = 110f + (idx * 16f)
            val posX = cos(angle) * armRadius
            val posY = sin(angle) * armRadius

            val calculatedRadius = kotlin.math.sqrt(posX * posX + posY * posY)
            assertEquals(armRadius, calculatedRadius, 0.05f)
            assertTrue("Radio del brazo debe ser positivo y creciente", armRadius >= 110f)
        }
    }

    @Test
    fun testLofiRainProgressCycle() {
        // Verifica que la fase de lluvia cicla limpiamente en el rango [0..1]
        val rainPhase = 0.85f
        for (i in 0..10) {
            val dropProgress = (rainPhase + (i * 0.17f)) % 1f
            assertTrue("dropProgress debe estar entre 0.0 y 1.0", dropProgress in 0f..1f)
        }
    }
}
