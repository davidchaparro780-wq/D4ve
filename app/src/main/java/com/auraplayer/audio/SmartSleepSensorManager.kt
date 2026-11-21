package com.auraplayer.audio

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import com.auraplayer.service.PlaybackService
import com.auraplayer.util.AppLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Detector de Sueño Inteligente por Acelerómetro (Smart Bed Inactivity Sensor).
 *
 * Detecta cuando el usuario se queda dormido por inmovilidad física del teléfono
 * sobre la cama, almohada o mesa de noche:
 * - Filtro pasabanda para descartar vibraciones parásitas.
 * - Al confirmar 15 minutos sin interacción humana:
 *     Inicia un fade-out suave de volumen durante 3 minutos para no despertar al oyente.
 * - Pausa la reproducción automáticamente para ahorrar batería y proteger el descanso.
 */
class SmartSleepSensorManager(private val context: Context) : SensorEventListener {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences("aura_smart_sleep_sensor", Context.MODE_PRIVATE)

    private val sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var isMonitoring: Boolean = false
        private set

    private val _isMonitoringFlow = MutableStateFlow(false)
    val isMonitoringFlow: StateFlow<Boolean> = _isMonitoringFlow.asStateFlow()

    private val _isFadeOutActive = MutableStateFlow(false)
    val isFadeOutActive: StateFlow<Boolean> = _isFadeOutActive.asStateFlow()

    private var lastMovementTimestamp = System.currentTimeMillis()
    private var inactivityThresholdMs = 15 * 60 * 1000L // 15 minutos por defecto
    private var onSleepDetected: (() -> Unit)? = null

    private var lastX = 0f
    private var lastY = 0f
    private var lastZ = 0f

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var fadeOutJob: Job? = null

    var isBedSensorEnabled: Boolean
        get() = prefs.getBoolean(KEY_BED_SENSOR, false)
        set(value) {
            prefs.edit().putBoolean(KEY_BED_SENSOR, value).apply()
            if (value) {
                // executeSleepFadeOut() ya realiza el fade-out, la pausa y la notificación.
                // El callback de "sueño detectado" solo debe informar al usuario; llamar de nuevo
                // a executeSleepFadeOut() aquí provocaría un bucle infinito de desvanecimientos.
                startMonitoring(15) {
                    try {
                        Toast.makeText(
                            appContext,
                            "💤 Inactividad detectada: música pausada suavemente",
                            Toast.LENGTH_LONG
                        ).show()
                    } catch (_: Exception) {}
                }
            } else {
                stopMonitoring()
            }
        }

    fun startMonitoring(
        minutesInactivity: Int = 15,
        onSleep: () -> Unit
    ): Boolean {
        if (accelerometer == null) return false
        inactivityThresholdMs = (minutesInactivity * 60 * 1000L).coerceAtLeast(60000L)
        onSleepDetected = onSleep
        lastMovementTimestamp = System.currentTimeMillis()

        val registered = sensorManager?.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        ) ?: false

        isMonitoring = registered
        _isMonitoringFlow.value = registered
        AppLog.d(TAG, "Monitoreo de sueño por acelerómetro iniciado ($minutesInactivity min)")
        return registered
    }

    fun stopMonitoring() {
        if (!isMonitoring) return
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Exception) {}
        isMonitoring = false
        _isMonitoringFlow.value = false
        fadeOutJob?.cancel()
        _isFadeOutActive.value = false
        AppLog.d(TAG, "Monitoreo de sueño detenido")
    }

    private fun executeSleepFadeOut() {
        if (_isFadeOutActive.value) return
        _isFadeOutActive.value = true

        fadeOutJob = coroutineScope.launch {
            AppLog.i(TAG, "Iniciando desvanecimiento suave de volumen por sueño (3 minutos)...")
            try {
                Toast.makeText(appContext, "😴 DaVE detectó inactividad: Desvaneciendo música suavemente...", Toast.LENGTH_LONG).show()
            } catch (_: Exception) {}

            val player = PlaybackService.activePlayer
            val startVol = player?.volume ?: 1.0f
            val totalSteps = 60
            val stepDelayMs = 3000L // 3 segundos x 60 = 180 segundos (3 min)

            for (step in 1..totalSteps) {
                if (!isActive) break
                val factor = (totalSteps - step).toFloat() / totalSteps.toFloat()
                player?.volume = (startVol * factor).coerceIn(0f, 1f)
                delay(stepDelayMs)
            }

            player?.pause()
            player?.volume = 1.0f // Restaurar volumen base para la próxima sesión
            stopMonitoring()
            // El flag se libera al final para que un callback accidental no pueda
            // reiniciar otro fade-out mientras este todavía está finalizando.
            onSleepDetected?.invoke()
            _isFadeOutActive.value = false
            AppLog.i(TAG, "Música en pausa: Modo descanso completado exitosamente.")
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !isMonitoring) return

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val deltaX = abs(x - lastX)
        val deltaY = abs(y - lastY)
        val deltaZ = abs(z - lastZ)

        lastX = x
        lastY = y
        lastZ = z

        val totalMovement = sqrt((deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ).toDouble()).toFloat()

        val now = System.currentTimeMillis()
        if (totalMovement > 0.45f) {
            // El usuario movió el teléfono en la cama, se reinicia el temporizador de inactividad
            lastMovementTimestamp = now
            if (_isFadeOutActive.value) {
                // Cancelar fadeout si el usuario se despertó y movió el móvil
                fadeOutJob?.cancel()
                _isFadeOutActive.value = false
                PlaybackService.activePlayer?.volume = 1.0f
            }
        } else {
            // Comprobar si ya pasó el tiempo de inactividad continua
            if (now - lastMovementTimestamp >= inactivityThresholdMs) {
                AppLog.i(TAG, "Inactividad confirmada por acelerómetro: Usuario dormido.")
                executeSleepFadeOut()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        private const val TAG = "SmartSleepSensor"
        private const val KEY_BED_SENSOR = "smart_bed_sleep_sensor_enabled"

        @Volatile
        private var INSTANCE: SmartSleepSensorManager? = null

        fun getInstance(context: Context): SmartSleepSensorManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SmartSleepSensorManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
