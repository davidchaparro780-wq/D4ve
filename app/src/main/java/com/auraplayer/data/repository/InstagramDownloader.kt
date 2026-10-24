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

    private const val GOOGLEBOT_UA = "Googlebot/2.1 (+http://www.google.com/bot.html)"

    fun isInstagramUrl(url: String): Boolean {
        val trimmed = url.trim().lowercase()
        return (trimmed.contains("instagram.com") || trimmed.contains("instagr.am")) &&
                (trimmed.startsWith("http://") || trimmed.startsWith("https://"))
    }

    /**
     * Extrae de forma limpia cualquier enlace de Instagram contenido dentro de un texto compartido.
     * Soporta reels, posts, stories y enlaces de compartir nativos (/share/reel/, /share/p/),
     * eliminando cualquier puntuación final accidental y parámetros innecesarios.
     */
    fun extractInstagramUrl(text: String): String? {
        val trimmed = text.trim()
        val urlRegex = Regex("""https?://[^\s<>"'{}|\\^`]+""")
        val matches = urlRegex.findAll(trimmed)
        for (match in matches) {
            val candidate = match.value.trim().trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}', '"', '\'')
            if (isInstagramUrl(candidate)) {
                return candidate
            }
        }
        val cleanTrimmed = trimmed.trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}', '"', '\'')
        if (isInstagramUrl(cleanTrimmed)) return cleanTrimmed
        return null
    }

    private data class InstagramRef(val route: String, val shortcode: String)

    private fun extractInstagramRef(url: String): InstagramRef? {
        val clean = url.trim().trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}', '"', '\'')
        val regex = Regex("""(?:instagram\.com|instagr\.am)/(?:share/)?(reel|reels|p|tv)/([A-Za-z0-9_-]+)""", RegexOption.IGNORE_CASE)
        val match = regex.find(clean) ?: return null
        val rawRoute = match.groupValues[1].lowercase()
        val shortcode = match.groupValues[2]
        val canonicalRoute = if (rawRoute.startsWith("p")) "p" else "reel"
        return InstagramRef(canonicalRoute, shortcode)
    }

    /**
     * Resuelve el enlace directo al video (.mp4) de Instagram en máxima calidad sin inicio de sesión
     * a través de extracción Server-Side Rendering (Googlebot SSR), Embed Captioned y OpenGraph.
     */
    suspend fun resolveInstagram(rawUrl: String): Result<InstagramMediaDetails> = withContext(Dispatchers.IO) {
        val cleanInput = rawUrl.trim().trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}', '"', '\'')
        if (!isInstagramUrl(cleanInput)) {
            return@withContext Result.failure(IllegalArgumentException("El enlace no es un enlace válido de Instagram."))
        }

        val ref = extractInstagramRef(cleanInput)
        val shortcode = ref?.shortcode ?: run {
            // Si no se pudo extraer el shortcode directamente, intentamos seguir redirecciones rápidas
            var current = cleanInput
            try {
                val conn = (URL(current).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 6000
                    readTimeout = 6000
                    instanceFollowRedirects = false
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", USER_AGENT)
                }
                val loc = conn.getHeaderField("Location")
                conn.disconnect()
                if (!loc.isNullOrBlank()) {
                    current = loc
                }
            } catch (_: Exception) {}
            extractInstagramRef(current)?.shortcode
        }

        val targetRoute = ref?.route ?: "reel"
        val targetUrl = if (shortcode != null) {
            "https://www.instagram.com/$targetRoute/$shortcode/"
        } else {
            cleanInput.substringBefore('?')
        }

        AppLog.d(TAG, "Resolviendo Instagram: raw='$cleanInput' -> targetUrl='$targetUrl' (shortcode=$shortcode)")

        // 1. Motor 1 (Principal): Googlebot SSR Scraping (Instagram sirve data-sjs con video_versions y CDN .mp4 directos)
        try {
            val conn = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 12000
                readTimeout = 15000
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", GOOGLEBOT_UA)
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                setRequestProperty("Accept-Language", "en-US,en;q=0.9")
            }

            if (conn.responseCode in 200..299) {
                val html = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                // A) Buscar en script tags data-sjs
                val sjsRegex = Regex("""<script\b[^>]*\bdata-sjs[^>]*>(\{.+?\})</script>""", RegexOption.DOT_MATCHES_ALL)
                for (match in sjsRegex.findAll(html)) {
                    val block = match.groupValues[1]
                    if (block.contains("video_versions") || block.contains(".mp4")) {
                        // Buscar URL de video mp4
                        val videoUrlRegex = Regex(""""url":"(https:[^"\\]+?\.mp4[^\"]*)"""")
                        val videoMatch = videoUrlRegex.find(block)?.groupValues?.get(1)
                        if (!videoMatch.isNullOrBlank()) {
                            val decodedVideoUrl = videoMatch.replace("\\u0026", "&").replace("\\/", "/")
                            
                            // Extraer thumbnail y titulo si estan disponibles
                            val imgRegex = Regex(""""display_url":"(https:[^"\\]+?)"""")
                            val thumbMatch = imgRegex.find(block)?.groupValues?.get(1)
                                ?.replace("\\u0026", "&")?.replace("\\/", "/") ?: ""

                            val titleRegex = Regex("""<meta\s+property=["']og:title["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                            val title = titleRegex.find(html)?.groupValues?.get(1)?.replace("&quot;", "\"") ?: "Instagram Reel"

                            AppLog.d(TAG, "Instagram resuelto con éxito vía Googlebot SSR data-sjs")
                            return@withContext Result.success(
                                InstagramMediaDetails(
                                    id = shortcode ?: System.currentTimeMillis().toString(),
                                    title = title.take(60),
                                    author = "Instagram",
                                    coverUrl = thumbMatch,
                                    videoUrl = decodedVideoUrl,
                                    isVideo = true
                                )
                            )
                        }
                    }
                }

                // B) Regex directo sobre todo el HTML para encontrar URLs de CDN mp4 (cdninstagram / fbcdn)
                val directCdnRegex = Regex("""https:\\/\\/[a-zA-Z0-9.-]+\.(?:cdninstagram|fbcdn)\.(?:com|net)\\/[^\s"'<>\\]+?\.mp4[^\s"'<>\\]*""")
                val directMatch = directCdnRegex.find(html)?.value
                if (!directMatch.isNullOrBlank()) {
                    val decoded = directMatch.replace("\\u0026", "&").replace("\\/", "/")
                    AppLog.d(TAG, "Instagram resuelto con éxito vía CDN Regex en SSR HTML")
                    return@withContext Result.success(
                        InstagramMediaDetails(
                            id = shortcode ?: System.currentTimeMillis().toString(),
                            title = "Instagram Reel",
                            author = "Instagram",
                            coverUrl = "",
                            videoUrl = decoded,
                            isVideo = true
                        )
                    )
                }
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Googlebot SSR fallo: ${e.message}")
        }

        // 2. Motor 2: Embed Captioned scraper
        if (shortcode != null) {
            try {
                val embedUrl = "https://www.instagram.com/p/$shortcode/embed/captioned/"
                val embedConn = (URL(embedUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 12000
                    requestMethod = "GET"
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                    setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                }

                if (embedConn.responseCode in 200..299) {
                    val embedHtml = embedConn.inputStream.bufferedReader().use { it.readText() }
                    embedConn.disconnect()

                    val videoRegex = Regex(""""video_url":"(https:[^"\\]+?)"""")
                    val videoMatch = videoRegex.find(embedHtml)?.groupValues?.get(1)
                    if (!videoMatch.isNullOrBlank()) {
                        val decodedVideoUrl = videoMatch.replace("\\u0026", "&").replace("\\/", "/")
                        AppLog.d(TAG, "Instagram resuelto con éxito vía Embed Captioned")
                        return@withContext Result.success(
                            InstagramMediaDetails(
                                id = shortcode,
                                title = "Instagram Reel",
                                author = "Instagram",
                                coverUrl = "",
                                videoUrl = decodedVideoUrl,
                                isVideo = true
                            )
                        )
                    }
                } else {
                    embedConn.disconnect()
                }
            } catch (e: Exception) {
                AppLog.w(TAG, "Embed Captioned scraper fallo: ${e.message}")
            }
        }

        // 3. Motor 3: OpenGraph Scraper con FacebookExternalHit UA
        try {
            val conn = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 12000
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", FB_EXTERNAL_HIT_UA)
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            }

            if (conn.responseCode in 200..299) {
                val html = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val ogVideoRegex = Regex("""<meta\s+property=["']og:video(?::secure_url)?["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                val ogImageRegex = Regex("""<meta\s+property=["']og:image["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
                val ogTitleRegex = Regex("""<meta\s+property=["']og:title["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)

                val videoMatch = ogVideoRegex.find(html)?.groupValues?.get(1)
                val imageMatch = ogImageRegex.find(html)?.groupValues?.get(1) ?: ""
                val titleMatch = ogTitleRegex.find(html)?.groupValues?.get(1) ?: "Reel de Instagram"

                if (!videoMatch.isNullOrBlank()) {
                    val decodedVideoUrl = videoMatch.replace("&amp;", "&")
                    val decodedImageUrl = imageMatch.replace("&amp;", "&")
                    AppLog.d(TAG, "Instagram resuelto con éxito vía OpenGraph Scraper")
                    return@withContext Result.success(
                        InstagramMediaDetails(
                            id = shortcode ?: System.currentTimeMillis().toString(),
                            title = titleMatch.take(60),
                            author = "Instagram",
                            coverUrl = decodedImageUrl,
                            videoUrl = decodedVideoUrl,
                            isVideo = true
                        )
                    )
                }
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "OpenGraph Scraper fallo: ${e.message}")
        }

        Result.failure(Exception("No se pudo obtener el video de Instagram. Asegúrate de que el enlace sea accesible o que la cuenta sea pública."))
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
                    setRequestProperty("Referer", "https://www.instagram.com/")
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
