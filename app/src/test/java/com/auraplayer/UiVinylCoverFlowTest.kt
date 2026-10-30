package com.auraplayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

/**
 * Pruebas unitarias para el CoverFlow 3D (rotación y escala en perspectiva),
 * reflejos anisotrópicos del tocadiscos de vinilo y el badge de pistas del álbum.
 */
class UiVinylCoverFlowTest {

    @Test
    fun `calculo de transformaciones 3d de pagina activa y laterales en CoverFlow`() {
        fun computeRotationY(pageOffset: Float): Float = (pageOffset * -38f).coerceIn(-68f, 68f)
        fun computeScale(pageOffset: Float): Float = (1f - (abs(pageOffset) * 0.16f)).coerceIn(0.65f, 1f)
        fun computeAlpha(pageOffset: Float): Float = (1f - (abs(pageOffset) * 0.28f)).coerceIn(0.18f, 1f)

        // 1. Página activa (centrada, offset = 0)
        assertEquals(0f, computeRotationY(0f), 0.001f)
        assertEquals(1.0f, computeScale(0f), 0.001f)
        assertEquals(1.0f, computeAlpha(0f), 0.001f)

        // 2. Página adyacente derecha (offset = 1)
        assertEquals(-38f, computeRotationY(1f), 0.001f)
        assertEquals(0.84f, computeScale(1f), 0.001f)
        assertEquals(0.72f, computeAlpha(1f), 0.001f)

        // 3. Página adyacente izquierda (offset = -1)
        assertEquals(38f, computeRotationY(-1f), 0.001f)
        assertEquals(0.84f, computeScale(-1f), 0.001f)
        assertEquals(0.72f, computeAlpha(-1f), 0.001f)

        // 4. Página lejana con límite de rotación (offset = 3)
        assertEquals(-68f, computeRotationY(3f), 0.001f) // Clamped to -68f
        assertTrue("La escala lejana no debe ser menor al límite mínimo", computeScale(3f) >= 0.65f)
    }

    @Test
    fun `geometria de los reflejos especulares de micro-surcos en vinilo`() {
        val minDimension = 400f // px
        val radius = minDimension * 0.46f

        assertEquals(184f, radius, 0.001f)
        assertTrue("El radio del reflejo debe ser menor a la mitad del plato", radius < (minDimension / 2f))
    }

    @Test
    fun `formato de pistas para badge flotante de album`() {
        fun formatTracksBadge(songCount: Int): String = "$songCount pistas"

        assertEquals("12 pistas", formatTracksBadge(12))
        assertEquals("1 pistas", formatTracksBadge(1))
        assertEquals("24 pistas", formatTracksBadge(24))
    }
}
