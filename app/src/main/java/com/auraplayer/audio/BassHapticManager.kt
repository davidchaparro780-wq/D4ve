package com.auraplayer.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.auraplayer.util.AppLog
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class HapticIntensity(val label: String, val ms: Long, val amplitude: Int) {
    OFF("Apagado", 0L, 0),
    LIGHT("Suave", 14L, 75),
    MEDIUM("Medio", 20L, 160),
    HEAVY("Subwoofer Potente", 28L, 255)
}

class BassHapticManager private constructor() {

    companion object {
        private const val TAG = "BassHapticManager"
        val instance: BassHapticManager by lazy { BassHapticManager() }
    }

    private var vibrator: Vibrator? = null
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        AppLog.e(TAG, "Uncaught error en BassHapticManager", throwable)
    }
    private val scope = CoroutineScope(Dispatchers.Default + exceptionHandler)
    private var hapticJob: Job? = null

    private val _intensity = MutableStateFlow(HapticIntensity.OFF)
    val intensity: StateFlow<HapticIntensity> = _intensity.asStateFlow()

    fun init(context: Context) {
        if (vibrator == null) {
            try {
                val appCtx = context.applicationContext ?: context
                vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vm = appCtx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vm?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    appCtx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }
            } catch (t: Throwable) {
                AppLog.e(TAG, "Error inicializando vibrador", t)
            }
        }
    }

    fun setIntensity(newIntensity: HapticIntensity) {
        _intensity.value = newIntensity
        if (newIntensity == HapticIntensity.OFF) {
            stopLoop()
        } else {
            startLoop()
        }
    }

    fun toggle(context: Context? = null) {
        if (vibrator == null && context != null) {
            init(context)
        }
        val next = when (_intensity.value) {
            HapticIntensity.OFF -> HapticIntensity.MEDIUM
            HapticIntensity.LIGHT -> HapticIntensity.MEDIUM
            HapticIntensity.MEDIUM -> HapticIntensity.HEAVY
            HapticIntensity.HEAVY -> HapticIntensity.OFF
        }
        setIntensity(next)
    }

    private fun startLoop() {
        if (hapticJob?.isActive == true) return

        hapticJob = scope.launch {
            var lastHitTime = 0L
            val vib = vibrator ?: return@launch
            val hasVib = try { vib.hasVibrator() } catch (_: Throwable) { false }
            if (!hasVib) return@launch

            val hasAmp = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) vib.hasAmplitudeControl() else false
            } catch (_: Throwable) { false }

            while (isActive && _intensity.value != HapticIntensity.OFF) {
                try {
                    // Check thread-safe playback status from PlaybackService without calling ExoPlayer on wrong thread
                    val isPlaying = com.auraplayer.service.PlaybackService.isCurrentlyPlaying
                    if (!isPlaying) {
                        delay(150)
                        continue
                    }

                    val current = _intensity.value
                    if (current == HapticIntensity.OFF) break

                    val now = System.currentTimeMillis()
                    val bass = try {
                        RealtimeVisualizerManager.instance.getBassEnergy(true)
                    } catch (_: Throwable) {
                        0.5f
                    }

                    // Trigger punch when sub-bass drops (> 0.62f threshold) and at least 110ms spaced
                    if (bass > 0.62f && (now - lastHitTime) > 115L) {
                        lastHitTime = now
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val effect = if (hasAmp && current.amplitude in 1..255) {
                                    VibrationEffect.createOneShot(current.ms.coerceAtLeast(1L), current.amplitude)
                                } else {
                                    VibrationEffect.createOneShot(current.ms.coerceAtLeast(1L), VibrationEffect.DEFAULT_AMPLITUDE)
                                }
                                vib.vibrate(effect)
                            } else {
                                @Suppress("DEPRECATION")
                                vib.vibrate(current.ms.coerceAtLeast(1L))
                            }
                        } catch (t: Throwable) {
                            AppLog.w(TAG, "Vibración de bajo háptico falló", t)
                        }
                    }
                } catch (t: Throwable) {
                    if (t is CancellationException) throw t
                    AppLog.w(TAG, "Error en ciclo de vibración", t)
                }
                delay(30)
            }
        }
    }

    private fun stopLoop() {
        try {
            hapticJob?.cancel()
            hapticJob = null
            vibrator?.cancel()
        } catch (_: Throwable) {}
    }
}
