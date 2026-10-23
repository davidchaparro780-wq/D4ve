package com.auraplayer.data.repository

import android.content.Context
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ArtistStat(
    val artistName: String,
    val playMinutes: Int
)

/**
 * Gestor de Estadísticas y Métricas Musicales en Tiempo Real (DaVE Insights).
 * Mide minutos reproducidos diarios, hora pico de escucha y artistas más sonados.
 */
class PlaybackStatsManager(context: Context) {

    private val prefs = context.getSharedPreferences("dave_insights_store", Context.MODE_PRIVATE)

    private fun getTodayKey(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
    }

    private var pendingTodaySecs = 0
    private var pendingTotalSecs = 0
    private var lastRecordedArtist = ""
    private var pendingArtistSecs = 0
    private var lastFlushTime = System.currentTimeMillis()

    /**
     * Registra segundos de reproducción activa en memoria y persiste por lotes cada 30 segundos
     * para eliminar el lag por escrituras continuas en disco.
     */
    @Synchronized
    fun recordListeningTime(artist: String, seconds: Int) {
        if (seconds <= 0) return

        val cleanArtist = artist.trim().ifEmpty { "Desconocido" }
        if (cleanArtist != lastRecordedArtist && lastRecordedArtist.isNotEmpty() && pendingArtistSecs > 0) {
            flush()
        }
        lastRecordedArtist = cleanArtist
        pendingTodaySecs += seconds
        pendingTotalSecs += seconds
        pendingArtistSecs += seconds

        val now = System.currentTimeMillis()
        if (now - lastFlushTime >= 30_000L || pendingTodaySecs >= 30) {
            flush()
        }
    }

    @Synchronized
    fun flush() {
        if (pendingTodaySecs <= 0 && pendingArtistSecs <= 0) return

        try {
            val editor = prefs.edit()
            val todayKey = "secs_${getTodayKey()}"
            val currentTodaySecs = prefs.getInt(todayKey, 0)
            editor.putInt(todayKey, currentTodaySecs + pendingTodaySecs)

            val totalSecs = prefs.getInt("total_secs_all_time", 0)
            editor.putInt("total_secs_all_time", totalSecs + pendingTotalSecs)

            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val hourKey = "hour_$hour"
            val hourSecs = prefs.getInt(hourKey, 0)
            editor.putInt(hourKey, hourSecs + pendingTodaySecs)

            if (lastRecordedArtist.isNotEmpty() && pendingArtistSecs > 0) {
                val artistKey = "artist_$lastRecordedArtist"
                val artistSecs = prefs.getInt(artistKey, 0)
                editor.putInt(artistKey, artistSecs + pendingArtistSecs)
            }

            editor.apply()
        } catch (_: Exception) {}

        pendingTodaySecs = 0
        pendingTotalSecs = 0
        pendingArtistSecs = 0
        lastFlushTime = System.currentTimeMillis()
    }

    fun getTodayListeningMinutes(): Int {
        val todayKey = "secs_${getTodayKey()}"
        return ((prefs.getInt(todayKey, 0) + pendingTodaySecs) / 60)
    }

    fun getTotalListeningHours(): Float {
        return ((prefs.getInt("total_secs_all_time", 0) + pendingTotalSecs) / 3600f)
    }

    /**
     * Retorna la hora pico de escucha más activa del usuario (formato "20:00 - 21:00")
     */
    fun getPeakListeningHour(): String {
        var maxHour = 20
        var maxSecs = -1
        for (h in 0..23) {
            val secs = prefs.getInt("hour_$h", 0)
            if (secs > maxSecs) {
                maxSecs = secs
                maxHour = h
            }
        }
        val endHour = (maxHour + 1) % 24
        return String.format("%02d:00 - %02d:00", maxHour, endHour)
    }

    /**
     * Retorna el top de artistas más escuchados
     */
    fun getTopArtists(limit: Int = 5): List<ArtistStat> {
        val all = prefs.all
        val list = mutableListOf<ArtistStat>()
        all.forEach { (k, v) ->
            if (k.startsWith("artist_") && v is Int) {
                val artistName = k.removePrefix("artist_")
                val minutes = v / 60
                if (minutes > 0) {
                    list.add(ArtistStat(artistName, minutes))
                }
            }
        }
        return list.sortedByDescending { it.playMinutes }.take(limit)
    }

    companion object {
        @Volatile
        private var instance: PlaybackStatsManager? = null

        fun getInstance(context: Context): PlaybackStatsManager {
            return instance ?: synchronized(this) {
                instance ?: PlaybackStatsManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
