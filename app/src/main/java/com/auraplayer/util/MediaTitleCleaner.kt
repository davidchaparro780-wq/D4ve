package com.auraplayer.util

data class CleanedMediaTags(
    val title: String,
    val artist: String?,
    val wasCleaned: Boolean
)

/**
 * Motor Inteligente de Limpieza de Etiquetas para canciones descargadas de YouTube, Snaptube, y2mate, etc.
 * Remueve automáticamente coletillas de video, calidad, resoluciones, sitios web de descarga y separa Artista - Título.
 */
object MediaTitleCleaner {

    private val JUNK_PATTERNS = listOf(
        // Parentesis o corchetes con etiquetas de video/audio
        Regex("""(?i)[\(\[\{]\s*(?:official\s*(?:music\s*)?video|video\s*oficial|audio\s*oficial|official\s*audio|official\s*lyric\s*video|lyric\s*video|lyrics?|letra\s*(?:y\s*acordes)?|letra|videoclip(?:\s*oficial)?|clip\s*officiel|video\s*clip|visualizer|video|audio|hq|hd|4k|1080p(?:\s*60fps)?|720p|remastered(?:\s*\d{4})?|full\s*album|cover\s*audio|en\s*vivo|live\s*version|live)\s*[\)\]\}]"""),
        // Etiquetas independientes comunes al final
        Regex("""(?i)\b(?:official\s*(?:music\s*)?video|video\s*oficial|audio\s*oficial|official\s*audio|lyric\s*video|videoclip)\b"""),
        // Sitios de descarga y ripeadores comunes
        Regex("""(?i)^(?:y2mate(?:[.\-_]is|[.\-_]com|[.\-_]app)?|y2meta(?:[.\-_]com|[.\-_]app)?|snaptube|tubidy|savefrom|ssyoutube|mp3juice|flvto|spotidownloader|spotify-downloader\.com)[\s\-_:]+"""),
        Regex("""(?i)[\s\-_:]+(?:y2mate(?:[.\-_]is|[.\-_]com|[.\-_]app)?|snaptube|tubidy|savefrom)$"""),
        // Extensiones de archivo residuales
        Regex("""(?i)\.(mp3|m4a|aac|opus|flac|wav|ogg|webm|mp4|wma)$"""),
        // Bitrate tags
        Regex("""(?i)[\(\[]\s*\d{2,3}\s*kbps\s*[\)\]]"""),
        // Corchetes como [FREE], [PROD BY ...], [EXCLUSIVE]
        Regex("""(?i)\[(?:free|exclusive)\]""")
    )

    fun clean(rawTitle: String, currentArtist: String? = null): CleanedMediaTags {
        var cleanTitle = rawTitle.trim()

        // Reemplazar guiones bajos por espacios si parece un nombre de archivo
        if (cleanTitle.contains('_') && !cleanTitle.contains(' ')) {
            cleanTitle = cleanTitle.replace('_', ' ')
        }

        val originalTrimmed = cleanTitle

        // Aplicar patrones de basura
        for (pattern in JUNK_PATTERNS) {
            cleanTitle = pattern.replace(cleanTitle, "").trim()
        }

        // Limpiar comillas dobles y comillas en los extremos (preservando apóstrofes internos como "Don't")
        cleanTitle = cleanTitle.replace("\"", "").replace("“", "").replace("”", "").trim('\'', '‘', '’')

        // Limpiar espacios múltiples
        cleanTitle = cleanTitle.replace(Regex("""\s+"""), " ").trim()

        // Detectar si el título tiene formato "Artista - Canción"
        var detectedArtist: String? = null
        val separators = listOf(" - ", " – ", " — ")
        for (sep in separators) {
            if (cleanTitle.contains(sep)) {
                val parts = cleanTitle.split(sep, limit = 2)
                if (parts.size == 2) {
                    val possibleArtist = parts[0].trim()
                    val possibleTitle = parts[1].trim()
                    if (possibleArtist.isNotBlank() && possibleTitle.isNotBlank()) {
                        val isCurrentArtistGeneric = currentArtist.isNullOrBlank() ||
                                currentArtist.equals("Unknown", ignoreCase = true) ||
                                currentArtist.equals("<unknown>", ignoreCase = true) ||
                                currentArtist.equals("Desconocido", ignoreCase = true) ||
                                currentArtist.equals("Artista Desconocido", ignoreCase = true) ||
                                currentArtist.equals("Snaptube", ignoreCase = true) ||
                                currentArtist.equals("YouTube", ignoreCase = true) ||
                                currentArtist.equals(rawTitle, ignoreCase = true)

                        if (isCurrentArtistGeneric) {
                            detectedArtist = possibleArtist
                            cleanTitle = possibleTitle
                        } else if (currentArtist.equals(possibleArtist, ignoreCase = true)) {
                            // Si el artista ya coincide con la izquierda, el título debe ser solo la derecha
                            cleanTitle = possibleTitle
                        }
                    }
                }
                break
            }
        }

        // Limpiar puntuación sobrante en bordes
        cleanTitle = cleanTitle.trimEnd('-', '–', '—', '|', '/', ':', ' ', '\'', '"', '‘', '’', '“', '”')
        cleanTitle = cleanTitle.trimStart('-', '–', '—', '|', '/', ':', ' ', '\'', '"', '‘', '’', '“', '”')

        // Capitalizar primera letra si empieza en minúscula
        if (cleanTitle.isNotEmpty() && cleanTitle[0].isLowerCase()) {
            cleanTitle = cleanTitle.replaceFirstChar { it.uppercase() }
        }

        val finalTitle = if (cleanTitle.isNotBlank()) cleanTitle else rawTitle.trim()

        return CleanedMediaTags(
            title = finalTitle,
            artist = detectedArtist,
            wasCleaned = (finalTitle != originalTrimmed) || (detectedArtist != null)
        )
    }
}
