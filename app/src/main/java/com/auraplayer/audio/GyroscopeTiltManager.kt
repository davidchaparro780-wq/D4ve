package com.auraplayer.audio

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.auraplayer.util.AppLog

private const val TAG = "GyroscopeTiltManager"

/**
 * Gestor de Sensores de Inclinación 3D (Giroscopio & Acelerómetro).
 * Captura la inclinación física del dispositivo (Pitch y Roll en grados)
 * con filtro pasabajo para proporcionar un movimiento tridimensional
 * realista, orgánico y sin temblores al chasis del tocadiscos y carátulas.
 */
class GyroscopeTiltManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var smoothPitch = 0f
    private var smoothRoll = 0f
    private var lastEmittedPitch = 0f
    private var lastEmittedRoll = 0f
    private val alpha = 0.15f // Factor de suavizado pasabajo
    private val deadband = 0.10f // Zona muerta para eliminar micro-vibraciones en reposo

    // Matrices reusables para eliminar asignaciones en cada evento del sensor
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    var onTiltChanged: ((pitch: Float, roll: Float) -> Unit)? = null

    fun startListening() {
        val sm = sensorManager ?: return
        val sensor = rotationSensor ?: return
        try {
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        } catch (e: Exception) {
            AppLog.d(TAG, "No se pudo registrar el listener del giroscopio", e)
        }
    }

    fun stopListening() {
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Exception) {}
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientation)

            // Convertir radianes a grados y acotar a [-16, 16]
            val rawPitch = Math.toDegrees(orientation[1].toDouble()).toFloat().coerceIn(-16f, 16f)
            val rawRoll = Math.toDegrees(orientation[2].toDouble()).toFloat().coerceIn(-16f, 16f)

            smoothPitch = smoothPitch + alpha * (rawPitch - smoothPitch)
            smoothRoll = smoothRoll + alpha * (rawRoll - smoothRoll)

            if (kotlin.math.abs(smoothPitch - lastEmittedPitch) >= deadband ||
                kotlin.math.abs(smoothRoll - lastEmittedRoll) >= deadband) {
                lastEmittedPitch = smoothPitch
                lastEmittedRoll = smoothRoll
                onTiltChanged?.invoke(smoothPitch, smoothRoll)
            }
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]

            val rawRoll = (x * 1.5f).coerceIn(-16f, 16f)
            val rawPitch = ((y - 4.5f) * 1.5f).coerceIn(-16f, 16f)

            smoothPitch = smoothPitch + alpha * (rawPitch - smoothPitch)
            smoothRoll = smoothRoll + alpha * (rawRoll - smoothRoll)

            if (kotlin.math.abs(smoothPitch - lastEmittedPitch) >= deadband ||
                kotlin.math.abs(smoothRoll - lastEmittedRoll) >= deadband) {
                lastEmittedPitch = smoothPitch
                lastEmittedRoll = smoothRoll
                onTiltChanged?.invoke(smoothPitch, smoothRoll)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}

/**
 * Hook Composable que expone los ángulos de inclinación (Pitch y Roll en grados).
 */
@Composable
fun rememberGyroscopeTilt(isEnabled: Boolean = true): State<Pair<Float, Float>> {
    val context = LocalContext.current
    val tiltState = remember { mutableStateOf(Pair(0f, 0f)) }

    DisposableEffect(isEnabled) {
        if (!isEnabled) {
            tiltState.value = Pair(0f, 0f)
            return@DisposableEffect onDispose {}
        }

        val manager = GyroscopeTiltManager(context)
        manager.onTiltChanged = { pitch, roll ->
            tiltState.value = Pair(pitch, roll)
        }
        manager.startListening()

        onDispose {
            manager.stopListening()
        }
    }

    return tiltState
}
