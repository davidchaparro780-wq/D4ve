package com.auraplayer

import com.auraplayer.data.repository.isVersionNewer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests del comparador de versiones del auto-updater.
 * Si esta lógica falla, los usuarios dejan de recibir actualizaciones.
 */
class VersionLogicTest {

    @Test
    fun `patch mayor es mas nueva`() {
        assertTrue(isVersionNewer("4.9.63", "4.9.62"))
    }

    @Test
    fun `minor mayor es mas nueva`() {
        assertTrue(isVersionNewer("4.10.0", "4.9.99"))
    }

    @Test
    fun `major mayor es mas nueva`() {
        assertTrue(isVersionNewer("5.0.0", "4.9.65"))
    }

    @Test
    fun `misma version no es mas nueva`() {
        assertFalse(isVersionNewer("4.9.65", "4.9.65"))
    }

    @Test
    fun `version anterior no es mas nueva`() {
        assertFalse(isVersionNewer("4.9.55", "4.9.62"))
    }

    @Test
    fun `prefijo v se ignora`() {
        assertTrue(isVersionNewer("v4.9.66", "4.9.65"))
        assertFalse(isVersionNewer("v4.9.65", "v4.9.65"))
    }

    @Test
    fun `mas partes gana por profundidad`() {
        assertTrue(isVersionNewer("4.9.65.1", "4.9.65"))
        assertFalse(isVersionNewer("4.9.65", "4.9.65.1"))
    }

    @Test
    fun `cadenas vacias nunca son mas nuevas`() {
        assertFalse(isVersionNewer("", "4.9.65"))
        assertFalse(isVersionNewer("4.9.65", ""))
    }

    @Test
    fun `sufijos no numericos no rompen la comparacion`() {
        assertTrue(isVersionNewer("4.9.66-beta", "4.9.65"))
        assertFalse(isVersionNewer("4.9.65-rc2", "4.9.65"))
    }
}
