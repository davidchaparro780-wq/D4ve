package com.auraplayer.audio

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.auraplayer.util.AppLog

private const val TAG = "FlashlightBeatManager"

class FlashlightBeatManager(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val exceptionHandler = kotlinx.coroutines.CoroutineExceptionHandler { _, throwable ->
        AppLog.e(TAG, "Uncaught error en FlashlightBeatManager", throwable)
    }
    private val scope = CoroutineScope(Dispatchers.Default + exceptionHandler)

    var isEnabled: Boolean = false
        private set

    private fun getCameraIdWithFlash(): String? {
        val cm = cameraManager ?: return null
        return try {
            cm.cameraIdList.firstOrNull { id ->
                val chars = cm.getCameraCharacteristics(id)
                val hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                val facing = chars.get(CameraCharacteristics.LENS_FACING)
                hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK
            } ?: cm.cameraIdList.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    fun start() {
        if (isEnabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val camId = getCameraIdWithFlash() ?: return
        isEnabled = true

        strobeJob = scope.launch {
            var lastFlashTime = 0L
            try {
                while (isActive && isEnabled) {
                    val isPlaying = com.auraplayer.service.PlaybackService.isCurrentlyPlaying
                    if (!isPlaying) {
                        try { cameraManager?.setTorchMode(camId, false) } catch (_: Exception) {}
                        delay(200)
                        continue
                    }

                    val now = System.currentTimeMillis()
                    val bass = try {
                        RealtimeVisualizerManager.instance.getBassEnergy(true)
                    } catch (_: Throwable) {
                        0.5f
                    }

                    // Destello reactivo al impacto de bombo/bajo (< 100 Hz)
                    if (bass > 0.68f && (now - lastFlashTime) > 120L) {
                        lastFlashTime = now
                        try { cameraManager?.setTorchMode(camId, true) } catch (_: Exception) {}
                        delay(45)
                        try { cameraManager?.setTorchMode(camId, false) } catch (_: Exception) {}
                    }

                    delay(30)
                }
            } catch (e: Exception) {
                AppLog.w(TAG, "El estrobo de linterna se detuvo inesperadamente", e)
            } finally {
                try {
                    cameraManager?.setTorchMode(camId, false)
                } catch (e: Exception) {
                    AppLog.d(TAG, "No se pudo apagar la linterna tras el estrobo", e)
                }
            }
        }
    }

    fun stop() {
        if (!isEnabled) return
        isEnabled = false
        strobeJob?.cancel()
        strobeJob = null
        val camId = getCameraIdWithFlash()
        if (camId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cameraManager?.setTorchMode(camId, false)
            } catch (e: Exception) {
                AppLog.d(TAG, "No se pudo apagar la linterna al detener el beat", e)
            }
        }
    }

    fun toggle(): Boolean {
        if (isEnabled) stop() else start()
        return isEnabled
    }
}
