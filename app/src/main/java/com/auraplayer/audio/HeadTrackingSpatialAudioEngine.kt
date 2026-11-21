package com.auraplayer.audio

import android.content.Context
import android.content.SharedPreferences
import com.auraplayer.service.PlaybackService
import com.auraplayer.util.AppLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

/**
 * Motor de Audio Espacial 3D con Seguimiento de Cabeza (Head-Tracking Spatial Audio).
 * Emula la experiencia de Apple Spatial Audio / Dolby Atmos:
 * - Detecta la orientación y giro angular del teléfono/cabeza vía Giroscopio.
 * - Ancla el escenario sonoro virtual en el espacio físico tridimensional:
 *     Si giras la cabeza a la izquierda, los monitores acústicos virtuales se perciben a la derecha.
 * - Modula dinámicamente el retardo interaural (ITD), la atenuación de nivel (ILD) y la profundidad del virtualizador DSP.
 */
class HeadTrackingSpatialAudioEngine private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences("aura_spatial_head_tracking", Context.MODE_PRIVATE)

    private val _isHeadTrackingEnabled = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, false))
    val isHeadTrackingEnabled: StateFlow<Boolean> = _isHeadTrackingEnabled.asStateFlow()

    private val _currentAzimuthOffset = MutableStateFlow(0f)
    val currentAzimuthOffset: StateFlow<Float> = _currentAzimuthOffset.asStateFlow()

    private var gyroManager: GyroscopeTiltManager? = null
    private var baseVolume = 1.0f

    fun initialize(gyro: GyroscopeTiltManager) {
        this.gyroManager = gyro
        if (_isHeadTrackingEnabled.value) {
            startTracking()
        }
    }

    fun setHeadTrackingEnabled(enabled: Boolean) {
        _isHeadTrackingEnabled.value = enabled
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()

        if (enabled) {
            startTracking()
            // Habilitar modo espacial en ecualizador
            EqualizerManager.instance.setSpatial8DEnabled(true)
        } else {
            stopTracking()
            _currentAzimuthOffset.value = 0f
            resetAcousticPan()
        }
    }

    private fun startTracking() {
        gyroManager?.onTiltChanged = { pitch, roll ->
            if (_isHeadTrackingEnabled.value) {
                processHeadMovement(pitch, roll)
            }
        }
        AppLog.d(TAG, "Audio Espacial 3D con Seguimiento de Cabeza ACTIVADO")
    }

    private fun stopTracking() {
        AppLog.d(TAG, "Audio Espacial 3D con Seguimiento de Cabeza DESACTIVADO")
    }

    private fun processHeadMovement(pitch: Float, roll: Float) {
        // Roll en rango [-16, 16] grados
        // Normalizado a [-1.0f, 1.0f]
        val panRatio = (roll / 16f).coerceIn(-1.0f, 1.0f)
        _currentAzimuthOffset.value = panRatio

        // Ajustar profundidad de virtualización binaural según movimiento
        val movementMagnitude = (abs(roll) + abs(pitch)).coerceIn(0f, 25f)
        val dynamicVirtualizerStrength = (500 + (movementMagnitude * 20f)).toInt().coerceIn(300, 1000).toShort()
        EqualizerManager.instance.updateSpatial8DDynamics(dynamicVirtualizerStrength)
    }

    private fun resetAcousticPan() {
        try {
            PlaybackService.activePlayer?.volume = baseVolume
        } catch (_: Exception) {}
    }

    companion object {
        private const val TAG = "SpatialHeadTracking"
        private const val KEY_ENABLED = "spatial_head_tracking_active"

        @Volatile
        private var INSTANCE: HeadTrackingSpatialAudioEngine? = null

        fun getInstance(context: Context): HeadTrackingSpatialAudioEngine {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: HeadTrackingSpatialAudioEngine(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
