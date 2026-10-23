package com.auraplayer.data.repository

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.auraplayer.util.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class InstagramMediaDetails(
    val id: String,
    val title: String,
    val author: String,
    val coverUrl: String,
    val videoUrl: String,
    val isVideo: Boolean = true
)

object InstagramDownloader {
    private const val TAG = "InstagramDownloader"
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
    private const val FB_EXTERNAL_HIT_UA = "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)"

    fun isInstagramUrl(url: String): Boolean {
        val trimmed = url.trim().lowercase()
        return (trimmed.contains("instagram.com") || trimmed.contains("instagr.am")) &&
                (trimmed.startsWith("http://") || trimmed.startsWith("https://"))
    }

    /**
     * Extrae de forma limpia cualquier enlace de Instagram contenido dentro de un texto compartido.
     * Soporta reels, posts y stories.
     */
    fun extractInstagramUrl(text: String): String? {
        val trimmed = text.trim()
        if (isInstagramUrl(trimmed)) return trimmed

        val urlRegex = Regex("""https?://[^\s<>"'{}|\\^`]+""")
        val matches = urlRegex.findAll(text)
        for (match in matches) {
            val candidate = match.value.trim()
            if (isInstagramUrl(candidate)) {
                return candidate
            }
        }
        return null
    }

    /**
     * Resuelve el enlace limpio del video o reel de Instagram en HD sin inicio de sesión.
     */
    suspend fun resolveInstagram(rawUrl: String): Result<InstagramMediaDetails> = withContext(Dispatchers.IO) {
        val cleanUrl = rawUrl.trim()
        if (!isInstagramUrl(cleanUrl)) {
            return@withContext Result.failure(IllegalArgumentException("El enlace no es un enlace válido de Instagram."))
        }

        // 1. Intento primario: OpenGraph scraper con FacebookExternalHit UA
        try {
            val conn = (URL(cleanUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 12000
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", FB_EXTERNAL_HIT_UA)
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            }

            if (conn.responseCode in 200..299) {
                val html = conn.inputStream.bufferedReader().use { it.readText() }
                val ogVideoRegex = Regex("""<meta\s+property=["']og:video(?::secure_url)?["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                val ogImageRegex = Regex("""<meta\s+property=["']og:image["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                val ogTitleRegex = Regex("""<meta\s+property=["']og:title["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)

                val videoMatch = ogVideoRegex.find(html)?.groupValues?.get(1)
                val imageMatch = ogImageRegex.find(html)?.groupValues?.get(1) ?: ""
                val titleMatch = ogTitleRegex.find(html)?.groupValues?.get(1) ?: "Reel de Instagram"

                if (!videoMatch.isNullOrBlank()) {
                    val decodedVideoUrl = videoMatch.replace("&amp;", "&")
                    val decodedImageUrl = imageMatch.replace("&amp;", "&")
                    AppLog.d(TAG, "Instagram resuelto exitosamente vía OpenGraph Scraper")
                    return@withContext Result.success(
                        InstagramMediaDetails(
                            id = System.currentTimeMillis().toString(),
                            title = titleMatch.take(60),
                            author = "Instagram",
                            coverUrl = decodedImageUrl,
                            videoUrl = decodedVideoUrl,
                            isVideo = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "OpenGraph Scraper falló, probando API de fallback...", e)
        }

        // 2. Intento secundario: Tiklydown IG API
        try {
            val encoded = URLEncoder.encode(cleanUrl, "UTF-8")
            val apiUrl = "https://api.tiklydown.eu.org/api/download/ig?url=$encoded"
            val conn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 12000
                readTimeout = 15000
                requestMethod = "GET"
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "application/json")
            }

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(responseText)
                val resultArr = root.optJSONArray("result")
                val firstObj = resultArr?.optJSONObject(0) ?: root.optJSONObject("result")

                val videoUrl = firstObj?.optString("url", "")
                    ?: firstObj?.optString("video_url", "")
                    ?: firstObj?.optString("download_url", "")
                    ?: ""
                val coverUrl = firstObj?.optString("thumbnail", "") ?: ""

                if (videoUrl.isNotBlank()) {
                    AppLog.d(TAG, "Instagram resuelto exitosamente vía Tiklydown API")
                    return@withContext Result.success(
                        InstagramMediaDetails(
                            id = System.currentTimeMillis().toString(),
                            title = "Instagram Reel",
                            author = "Instagram",
                            coverUrl = coverUrl,
                            videoUrl = videoUrl,
                            isVideo = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Tiklydown IG API falló: ${e.message}", e)
        }

        Result.failure(Exception("No se pudo obtener el video de Instagram. Asegúrate de que la cuenta o reel sea público."))
    }

    /**
     * Descarga el video de Instagram en DCIM/DaVEPlayer y lo indexa de inmediato en MediaStore.
     */
    suspend fun downloadMedia(
        context: Context,
        details: InstagramMediaDetails,
        onProgress: (Int) -> Unit
    ): Result<Uri> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            val fileName = "Instagram_${details.id}.mp4"
            var currentUrl = details.videoUrl
            var redirects = 0

            while (true) {
                val conn = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 20000
                    readTimeout = 25000
                    requestMethod = "GET"
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", USER_AGENT)
                    setRequestProperty("Accept", "*/*")
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
                }
            }

            if (finalUri == null) {
                val baseDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "DaVEPlayer")
                if (!baseDir.exists()) baseDir.mkdirs()
                val targetFile = File(baseDir, fileName)
                FileOutputStream(targetFile).use { output ->
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
                finalUri = Uri.fromFile(targetFile)
                MediaScannerConnection.scanFile(context, arrayOf(targetFile.absolutePath), arrayOf("video/mp4"), null)
            }

            Result.success(finalUri ?: return@withContext Result.failure(Exception("No se pudo guardar")))
        } catch (e: Exception) {
            AppLog.e(TAG, "Error descargando Instagram media", e)
            Result.failure(e)
        } finally {
            try { inputStream?.close() } catch (_: Throwable) {}
            try { connection?.disconnect() } catch (_: Throwable) {}
        }
    }
}
