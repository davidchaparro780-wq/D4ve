package com.auraplayer.audio

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.auraplayer.util.AppLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Gestor del Gesto Físico "Voltear para Pausar" (Flip to Pause).
 * Detecta cuando el dispositivo se coloca boca abajo sobre una mesa o superficie plana
 * y pausa la reproducción suavemente con respuesta háptica.
 */
class FlipToPauseManager(
    private val context: Context,
    private val onFlipFaceDown: () -> Unit
) : SensorEventListener {

    companion object {
        private const val TAG = "FlipToPauseManager"
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var isEnabled: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                if (value) start() else stop()
            }
        }

    private var isFaceDown = false
    private var debounceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun start() {
        if (sensor == null) return
        try {
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        } catch (e: Exception) {
            AppLog.w(TAG, "No se pudo registrar sensor para Flip to Pause", e)
        }
    }

    fun stop() {
        debounceJob?.cancel()
        isFaceDown = false
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Exception) {}
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isEnabled || event == null) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Teléfono plano boca abajo: Z fuertemente negativo, X e Y cercanos a cero
        val faceDownNow = z < -7.0f && abs(x) < 4.5f && abs(y) < 4.5f

        if (faceDownNow && !isFaceDown) {
            // Confirmar que se mantiene boca abajo por al menos 350 ms para evitar falsos positivos al mover el teléfono
            if (debounceJob?.isActive != true) {
                debounceJob = scope.launch {
                    delay(350)
                    isFaceDown = true
                    onFlipFaceDown()
                }
            }
        } else if (!faceDownNow) {
            debounceJob?.cancel()
            isFaceDown = false
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
