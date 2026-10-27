package com.auraplayer.data.model

import android.net.Uri

/**
 * Formatea milisegundos como "MM:SS" o "H:MM:SS" si supera la hora.
 * Función pura de nivel superior para poder testearla sin Android.
 */
fun formatDurationMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes % 60, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

/**
 * Insignia de calidad de audio según extensión y bitrate estimado (solo MP3).
 * Función pura de nivel superior para poder testearla sin Android.
 */
fun computeAudioFormatBadge(path: String, size: Long, durationMs: Long): String {
    val ext = path.substringAfterLast('.', "").lowercase()
    return when (ext) {
        "flac" -> "FLAC"
        "wav" -> "WAV"
        "alac" -> "ALAC"
        "dsf", "dff" -> "DSD"
        "m4a", "aac" -> "AAC"
        "ogg", "opus" -> "OPUS"
        "mp3" -> {
            val sec = durationMs / 1000
            if (sec > 0 && size > 0) {
                val kbps = (size * 8 / sec) / 1000
                if (kbps >= 300) "320k" else if (kbps >= 240) "256k" else "MP3"
            } else "MP3"
        }
        else -> if (ext.isNotBlank()) ext.uppercase() else ""
    }
}

data class MediaModel(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val uri: Uri,
    val artworkUri: Uri? = null,
    val isVideo: Boolean = false,
    val folderName: String = "",
    val path: String = "",
    val size: Long = 0L,
    val dateAdded: Long = 0L
) {
    val isRecentlyAdded: Boolean
        get() {
            if (dateAdded <= 0L) return false
            // MediaStore DATE_ADDED is in seconds, ensure millisecond comparison
            val addedMs = if (dateAdded > 100_000_000_000L) dateAdded else dateAdded * 1000L
            val diffMs = System.currentTimeMillis() - addedMs
            return diffMs in 0..(48 * 3600 * 1000L) // Added in last 48 hours
        }

    val formattedDuration: String
        get() = formatDurationMs(duration)

    val audioFormatBadge: String
        get() = computeAudioFormatBadge(path, size, duration)

    val isLossless: Boolean
        get() = audioFormatBadge in listOf("FLAC", "WAV", "ALAC", "DSD")
}

enum class RepeatMode {
    OFF, ALL, ONE
}
