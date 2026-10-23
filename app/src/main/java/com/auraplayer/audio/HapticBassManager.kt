package com.auraplayer.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.auraplayer.util.AppLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val TAG = "HapticBassManager"

/**
 * Subwoofer Háptico en Tiempo Real:
 * Conecta el motor de vibración háptica de precisión con el espectro de graves (< 100 Hz).
 * Vibra de forma proporcional y rítmica al compás de los golpes del bajo (kick/bass),
 * permitiendo sentir físicamente el impacto musical en las manos.
 */
class HapticBassManager(context: Context) {

    private val appContext = context.applicationContext
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vm?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val prefs = appContext.getSharedPreferences("aura_haptic_bass", Context.MODE_PRIVATE)
    private var syncJob: Job? = null
    private val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        AppLog.e(TAG, "Uncaught error en HapticBassManager", throwable)
    }
    private val scope = CoroutineScope(Dispatchers.Default + exceptionHandler)

    var isEnabled: Boolean
        get() = prefs.getBoolean("haptic_bass_enabled", false)
        set(value) {
            prefs.edit().putBoolean("haptic_bass_enabled", value).apply()
            if (value) startRealtimeBassSync() else stopRealtimeBassSync()
        }

    init {
        if (isEnabled) {
            startRealtimeBassSync()
        }
    }

    fun startRealtimeBassSync() {
        if (syncJob?.isActive == true) return
        syncJob = scope.launch {
            var lastPulseTime = 0L
            while (isActive) {
                try {
                    if (!isEnabled) {
                        delay(400)
                        continue
                    }

                    // Solo vibrar si hay reproducción activa de música (thread-safe)
                    val isAudioPlaying = com.auraplayer.service.PlaybackService.isCurrentlyPlaying
                    if (!isAudioPlaying) {
                        delay(250)
                        continue
                    }

                    val now = System.currentTimeMillis()
                    // Obtener energía de graves en tiempo real del motor FFT (0.05f .. 1.0f)
                    val bass = try {
                        RealtimeVisualizerManager.instance.getBassEnergy(isAudioPlaying)
                    } catch (_: Throwable) {
                        0.5f
                    }

                    // Disparo de pulso cuando la energía de graves supera el umbral (punch de bajo)
                    if (bass > 0.65f && (now - lastPulseTime) > 130L) {
                        lastPulseTime = now
                        val intensity = ((bass - 0.5f) * 2f).coerceIn(0.1f, 1.0f)
                        triggerDynamicPulse(intensity)
                    }
                } catch (t: Throwable) {
                    if (t is kotlinx.coroutines.CancellationException) throw t
                    AppLog.w(TAG, "Error en ciclo de vibración HapticBassManager", t)
                }

                delay(40)
            }
        }
    }

    fun stopRealtimeBassSync() {
        syncJob?.cancel()
        syncJob = null
        try {
            vibrator?.cancel()
        } catch (_: Throwable) {}
    }

    private fun triggerDynamicPulse(intensity: Float) {
        val vib = vibrator ?: return
        val hasVib = try { vib.hasVibrator() } catch (_: Throwable) { false }
        if (!hasVib) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val amp = (intensity * 255).toInt().coerceIn(40, 255)
                val duration = (25 + (intensity * 25)).toLong().coerceAtLeast(1L)
                val effect = VibrationEffect.createOneShot(duration, amp)
                vib.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vib.vibrate(28L)
            }
        } catch (t: Throwable) {
            AppLog.w(TAG, "Fallo al emitir el pulso háptico de graves", t)
        }
    }

    fun triggerBassPulse() {
        triggerDynamicPulse(0.85f)
    }

    companion object {
        @Volatile
        private var instance: HapticBassManager? = null

        fun getInstance(context: Context): HapticBassManager {
            return instance ?: synchronized(this) {
                instance ?: HapticBassManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
