package com.auraplayer.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import com.auraplayer.audio.VideoToAudioExtractor
import com.auraplayer.util.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder

data class FacebookMediaDetails(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val videoHdUrl: String?,
    val videoSdUrl: String,
    val durationSeconds: Int = 0
) {
    val bestVideoUrl: String
        get() = videoHdUrl?.takeIf { it.isNotBlank() } ?: videoSdUrl
}

object FacebookDownloader {
    private const val TAG = "FacebookDownloader"
    private const val USER_AGENT_MOBILE = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
    private const val USER_AGENT_DESKTOP = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
    private const val FB_EXTERNAL_HIT_UA = "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)"
    private const val CHANNEL_ID = "dave_social_downloads"
    private const val NOTIFICATION_ID = 8842

    fun isFacebookUrl(url: String): Boolean {
        val clean = url.trim().lowercase()
        return clean.contains("facebook.com") ||
                clean.contains("fb.watch") ||
                clean.contains("fb.com") ||
                clean.contains("m.facebook.com")
    }

    private fun cleanDecodedUrl(raw: String): String {
        return raw
            .replace("\\u0026", "&")
            .replace("\\u00253D", "%3D")
            .replace("\\u0025", "%")
            .replace("\\/", "/")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#039;", "'")
            .trim()
    }

    /**
     * Resuelve redirecciones cortas comunes (fb.watch/..., facebook.com/share/r/...)
     * para obtener la URL final y el identificador canónico del video o reel.
     */
    private fun resolveRedirects(initialUrl: String): String {
        var current = initialUrl
        var redirects = 0
        while (redirects < 6) {
            try {
                val conn = (URL(current).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 7000
                    readTimeout = 7000
                    instanceFollowRedirects = false
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", USER_AGENT_MOBILE)
                }
                val code = conn.responseCode
                val location = conn.getHeaderField("Location")
                conn.disconnect()
                if ((code in 301..303 || code == 307 || code == 308) && !location.isNullOrBlank()) {
                    current = if (location.startsWith("http")) location else "https://www.facebook.com$location"
                    redirects++
                } else {
                    break
                }
            } catch (_: Exception) {
                break
            }
        }
        return current
    }

    /**
     * Extrae URLs de video (HD y SD), miniatura y metadatos de cualquier HTML de Facebook.
     */
    private fun extractMediaFromHtml(html: String, fallbackId: String): FacebookMediaDetails? {
        if (html.isBlank()) return null

        // 1. Extraer URL HD (browser_native_hd_url, playable_url_quality_hd, hd_src)
        val hdRegexes = listOf(
            Regex(""""browser_native_hd_url"\s*:\s*"(https:(?:\\/\\/|//)[^"]+?)""""),
            Regex(""""playable_url_quality_hd"\s*:\s*"(https:(?:\\/\\/|//)[^"]+?)""""),
            Regex(""""hd_src"\s*:\s*"(https:(?:\\/\\/|//)[^"]+?)""""),
            Regex(""""hd_src_no_ratelimit"\s*:\s*"(https:(?:\\/\\/|//)[^"]+?)"""")
        )
        var hdUrl: String? = null
        for (rgx in hdRegexes) {
            val match = rgx.find(html)?.groupValues?.get(1)
            if (!match.isNullOrBlank()) {
                hdUrl = cleanDecodedUrl(match)
                break
            }
        }

        // 2. Extraer URL SD (browser_native_sd_url, playable_url, sd_src, og:video)
        val sdRegexes = listOf(
            Regex(""""browser_native_sd_url"\s*:\s*"(https:(?:\\/\\/|//)[^"]+?)""""),
            Regex(""""playable_url"\s*:\s*"(https:(?:\\/\\/|//)[^"]+?)""""),
            Regex(""""sd_src"\s*:\s*"(https:(?:\\/\\/|//)[^"]+?)""""),
            Regex(""""sd_src_no_ratelimit"\s*:\s*"(https:(?:\\/\\/|//)[^"]+?)""""),
            Regex("""<meta\s+property=["']og:video(?::secure_url)?["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        )
        var sdUrl: String? = null
        for (rgx in sdRegexes) {
            val match = rgx.find(html)?.groupValues?.get(1)
            if (!match.isNullOrBlank()) {
                sdUrl = cleanDecodedUrl(match)
                break
            }
        }

        // Si no hay SD ni HD, intentar buscar cualquier URL directa de video en CDN de Facebook (fbcdn.net)
        if (sdUrl.isNullOrBlank() && hdUrl.isNullOrBlank()) {
            val directCdnRegex = Regex("""https:(?:\\/\\/|//)[a-zA-Z0-9.-]+\.fbcdn\.net(?:\\/|/)[^"'\s<>]*(?:\.mp4|bytestart=|/t50\.)[^"'\s<>]*""")
            val cdnMatch = directCdnRegex.find(html)?.value
            if (!cdnMatch.isNullOrBlank()) {
                sdUrl = cleanDecodedUrl(cdnMatch)
            }
        }

        val finalSd = sdUrl ?: hdUrl ?: return null

        // Extraer miniatura (og:image o preferred_thumbnail)
        val ogImageRegex = Regex("""<meta\s+property=["']og:image["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val thumbRegex = Regex(""""preferred_thumbnail"\s*:\s*\{"image"\s*:\s*\{"uri"\s*:\s*"(https:(?:\\/\\/|//)[^"]+?)"""")
        val rawThumb = ogImageRegex.find(html)?.groupValues?.get(1)
            ?: thumbRegex.find(html)?.groupValues?.get(1) ?: ""
        val decodedThumb = if (rawThumb.isNotBlank()) cleanDecodedUrl(rawThumb) else ""

        // Extraer título
        val ogTitleRegex = Regex("""<meta\s+property=["']og:title["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val rawTitle = ogTitleRegex.find(html)?.groupValues?.get(1) ?: "Facebook Reel"
        val cleanTitle = cleanDecodedUrl(rawTitle)
            .replace(Regex("""\|\s*Facebook$""", RegexOption.IGNORE_CASE), "")
            .trim()
            .ifBlank { "Facebook Reel" }

        // Extraer autor
        val ogDescRegex = Regex("""<meta\s+property=["']og:description["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val author = cleanDecodedUrl(ogDescRegex.find(html)?.groupValues?.get(1) ?: "Facebook Creator").take(40)

        return FacebookMediaDetails(
            id = fallbackId,
            title = cleanTitle.take(80),
            author = author,
            coverUrl = decodedThumb,
            videoHdUrl = hdUrl,
            videoSdUrl = finalSd
        )
    }

    /**
     * Resuelve el enlace de Facebook (Reels, Watch, videos públicos) sin inicio de sesión.
     */
    suspend fun resolveFacebook(rawUrl: String): Result<FacebookMediaDetails> = withContext(Dispatchers.IO) {
        val cleanInput = rawUrl.trim()
        if (!isFacebookUrl(cleanInput)) {
            return@withContext Result.failure(IllegalArgumentException("El enlace no es un enlace válido de Facebook."))
        }

        val canonicalUrl = resolveRedirects(cleanInput)
        val fallbackId = System.currentTimeMillis().toString()

        AppLog.d(TAG, "Resolviendo Facebook: '$cleanInput' -> '$canonicalUrl'")

        // Intentar Motor 1: Mobile Chrome Scraper
        try {
            val conn = (URL(canonicalUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 12000
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", USER_AGENT_MOBILE)
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                setRequestProperty("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                setRequestProperty("Sec-Fetch-Mode", "navigate")
            }

            if (conn.responseCode in 200..299) {
                val html = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val details = extractMediaFromHtml(html, fallbackId)
                if (details != null) {
                    AppLog.d(TAG, "Facebook resuelto vía Motor 1 (Mobile Chrome)")
                    return@withContext Result.success(details)
                }
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Motor 1 falló: ${e.message}")
        }

        // Intentar Motor 2: Desktop Chrome Scraper (Facebook Desktop a menudo incrusta browser_native_hd_url)
        try {
            val desktopUrl = if (canonicalUrl.contains("m.facebook.com")) {
                canonicalUrl.replace("m.facebook.com", "www.facebook.com")
            } else {
                canonicalUrl
            }
            val conn = (URL(desktopUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 12000
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", USER_AGENT_DESKTOP)
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                setRequestProperty("Accept-Language", "en-US,en;q=0.9")
            }

            if (conn.responseCode in 200..299) {
                val html = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val details = extractMediaFromHtml(html, fallbackId)
                if (details != null) {
                    AppLog.d(TAG, "Facebook resuelto vía Motor 2 (Desktop Chrome)")
                    return@withContext Result.success(details)
                }
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Motor 2 falló: ${e.message}")
        }

        // Intentar Motor 3: Facebook External Hit Crawler
        try {
            val conn = (URL(canonicalUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 10000
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", FB_EXTERNAL_HIT_UA)
                setRequestProperty("Accept", "*/*")
            }

            if (conn.responseCode in 200..299) {
                val html = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val details = extractMediaFromHtml(html, fallbackId)
                if (details != null) {
                    AppLog.d(TAG, "Facebook resuelto vía Motor 3 (FB Crawler)")
                    return@withContext Result.success(details)
                }
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Motor 3 falló: ${e.message}")
        }

        Result.failure(Exception("No se pudo obtener el video de Facebook. Verifica que sea un Reel o video público y que el enlace esté disponible."))
    }

    /**
     * Descarga el video de Facebook en DCIM/DaVEPlayer y lo indexa de inmediato en MediaStore.
     * Si extractAudio es true, además extrae el audio a Music/DaVEPlayer.
     */
    suspend fun downloadMedia(
        context: Context,
        details: FacebookMediaDetails,
        extractAudio: Boolean = false,
        onProgress: (Int) -> Unit
    ): Result<Uri> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            val videoUrl = details.bestVideoUrl
            val fileName = "Facebook_${details.id}.mp4"
            var currentUrl = videoUrl
            var redirects = 0

            while (true) {
                val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 20000
                    readTimeout = 25000
                    requestMethod = "GET"
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", USER_AGENT_MOBILE)
                    setRequestProperty("Accept", "*/*")
                    setRequestProperty("Referer", "https://www.facebook.com/")
                }
                connection = conn
                val code = conn.responseCode
                if ((code in 301..303 || code == 307 || code == 308) && redirects < 5) {
                    val location = conn.getHeaderField("Location")
                    if (!location.isNullOrBlank()) {
                        currentUrl = location
                        redirects++
                        conn.disconnect()
                        connection = null
                        continue
                    }
                }
                break
            }

            val finalConn = connection ?: return@withContext Result.failure(Exception("Error al conectar"))
            if (finalConn.responseCode !in 200..299) {
                return@withContext Result.failure(Exception("HTTP ${finalConn.responseCode}"))
            }

            val contentLength = finalConn.contentLength.toLong()
            inputStream = finalConn.inputStream

            var finalUri: Uri? = null
            val currentTime = System.currentTimeMillis()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/DaVEPlayer")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                    put(MediaStore.MediaColumns.DATE_ADDED, currentTime / 1000)
                    put(MediaStore.Video.Media.TITLE, details.title)
                }

                val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalRead = 0L
                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                            if (contentLength > 0) {
                                onProgress(((totalRead * 100) / contentLength).toInt().coerceIn(0, 100))
                            }
                        }
                    }

                    val updateValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.IS_PENDING, 0)
                    }
                    context.contentResolver.update(uri, updateValues, null, null)
                    finalUri = uri

                    val physicalDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "DaVEPlayer")
                    val physicalFile = File(physicalDir, fileName)
                    MediaScannerConnection.scanFile(context, arrayOf(physicalFile.absolutePath), arrayOf("video/mp4"), null)

                    // Si solicitó extraer audio MP3 / M4A
                    if (extractAudio) {
                        try {
                            val audioName = "Facebook_${details.id}.m4a"
                            VideoToAudioExtractor.extractAudioFromVideo(context, uri, audioName)
                        } catch (e: Exception) {
                            AppLog.w(TAG, "Error extrayendo audio secundario: ${e.message}")
                        }
                    }
                }
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "DaVEPlayer")
                if (!dir.exists()) dir.mkdirs()
                val targetFile = File(dir, fileName)

                targetFile.outputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (contentLength > 0) {
                            onProgress(((totalRead * 100) / contentLength).toInt().coerceIn(0, 100))
                        }
                    }
                }

                MediaScannerConnection.scanFile(context, arrayOf(targetFile.absolutePath), arrayOf("video/mp4"), null)
                finalUri = Uri.fromFile(targetFile)
            }

            if (finalUri != null) {
                Result.success(finalUri)
            } else {
                Result.failure(Exception("No se pudo guardar el archivo en el dispositivo"))
            }
        } catch (e: Exception) {
            AppLog.e(TAG, "Error en downloadMedia", e)
            Result.failure(e)
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
            try { connection?.disconnect() } catch (_: Exception) {}
        }
    }
}
