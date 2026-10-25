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
        // Soporta /reel/ID, /reels/ID, /p/ID, /tv/ID, /share/reel/ID, /username/reel/ID, etc.
        val regex = Regex(
            """(?:instagram\.com|instagr\.am)/(?:[A-Za-z0-9_.-]+/)?(?:share/)?(reel|reels|p|tv)/([A-Za-z0-9_-]+)""",
            RegexOption.IGNORE_CASE
        )
        val match = regex.find(clean) ?: return null
        val rawRoute = match.groupValues[1].lowercase()
        val shortcode = match.groupValues[2]
        val canonicalRoute = if (rawRoute.startsWith("p")) "p" else "reel"
        return InstagramRef(canonicalRoute, shortcode)
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
     * Extrae detalles de video (URL mp4, miniatura, título y autor) de cualquier HTML o JSON de Instagram.
     */
    private fun extractMediaFromText(text: String, fallbackShortcode: String?): InstagramMediaDetails? {
        if (text.isBlank()) return null

        // 1. Buscar en array video_versions (máxima calidad de reproducción en Instagram)
        val versionsRegex = Regex(""""video_versions"\s*:\s*\[\s*\{[^}]*?"url"\s*:\s*"(https:(?:\\/\\/|//)[^"]+?)"""")
        var videoUrl = versionsRegex.find(text)?.groupValues?.get(1)

        // 2. Buscar url directa en video_dash_manifest o video_versions anidado
        if (videoUrl.isNullOrBlank()) {
            val urlRegex = Regex(""""url"\s*:\s*"(https:(?:\\/\\/|//)[^"]+?(?:\.mp4|bytestart|/t50\.)[^"]*)"""")
            videoUrl = urlRegex.find(text)?.groupValues?.get(1)
        }

        // 3. Buscar video_url (embed y oEmbed)
        if (videoUrl.isNullOrBlank()) {
            val videoUrlRegex = Regex(""""video_url"\s*:\s*"(https:(?:\\/\\/|//)[^"]+?)"[,\s}]""")
            videoUrl = videoUrlRegex.find(text)?.groupValues?.get(1)
        }

        // 4. Buscar contentUrl
        if (videoUrl.isNullOrBlank()) {
            val contentUrlRegex = Regex(""""contentUrl"\s*:\s*"(https:(?:\\/\\/|//)[^"]+?)"[,\s}]""")
            videoUrl = contentUrlRegex.find(text)?.groupValues?.get(1)
        }

        // 5. Buscar en meta og:video
        if (videoUrl.isNullOrBlank()) {
            val ogVideoRegex = Regex("""<meta\s+property=["']og:video(?::secure_url)?["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
            videoUrl = ogVideoRegex.find(text)?.groupValues?.get(1)
        }

        // 6. Regex directo sobre CDN de Instagram (cdninstagram / fbcdn) para archivos de video
        if (videoUrl.isNullOrBlank()) {
            val directCdnRegex = Regex("""https:(?:\\/\\/|//)[a-zA-Z0-9.-]+\.(?:cdninstagram|fbcdn)\.(?:com|net)(?:\\/|/)[^"'\s<>]*(?:\.mp4|/t50\.|bytestart=)[^"'\s<>]*""")
            videoUrl = directCdnRegex.find(text)?.value
        }

        if (videoUrl.isNullOrBlank()) return null

        val decodedVideoUrl = cleanDecodedUrl(videoUrl)

        // Extraer miniatura
        val displayUrlRegex = Regex(""""display_url"\s*:\s*"(https:(?:\\/\\/|//)[^"]+?)"""")
        val ogImageRegex = Regex("""<meta\s+property=["']og:image["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val rawThumb = displayUrlRegex.find(text)?.groupValues?.get(1)
            ?: ogImageRegex.find(text)?.groupValues?.get(1) ?: ""
        val decodedThumb = if (rawThumb.isNotBlank()) cleanDecodedUrl(rawThumb) else ""

        // Extraer título
        val ogTitleRegex = Regex("""<meta\s+property=["']og:title["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val ogDescRegex = Regex("""<meta\s+property=["']og:description["']\s+content=["']([^"']+)["']""", RegexOption.IGNORE_CASE)
        val rawTitle = ogTitleRegex.find(text)?.groupValues?.get(1)
            ?: ogDescRegex.find(text)?.groupValues?.get(1)
            ?: "Instagram Reel"
        val cleanTitle = cleanDecodedUrl(rawTitle)
            .replace(Regex("""^.+?\s+on\s+Instagram:\s*"""", RegexOption.IGNORE_CASE), "")
            .trimEnd('"')
            .ifBlank { "Instagram Reel" }

        // Extraer autor
        val authorRegex = Regex("""<meta\s+property=["']og:title["']\s+content=["']([^"':]+)\s+on\s+Instagram""", RegexOption.IGNORE_CASE)
        val author = authorRegex.find(text)?.groupValues?.get(1)?.trim() ?: "Instagram"

        return InstagramMediaDetails(
            id = fallbackShortcode ?: System.currentTimeMillis().toString(),
            title = cleanTitle.take(80),
            author = author,
            coverUrl = decodedThumb,
            videoUrl = decodedVideoUrl,
            isVideo = true
        )
    }

    /**
     * Resuelve el enlace directo al video (.mp4) de Instagram en máxima calidad sin inicio de sesión
     * a través de un motor multi-tier resiliente (Googlebot SSR, Mobile Chrome Scraper, OpenGraph, Embed Captioned y Web Info API).
     */
    suspend fun resolveInstagram(rawUrl: String): Result<InstagramMediaDetails> = withContext(Dispatchers.IO) {
        val cleanInput = rawUrl.trim().trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}', '"', '\'')
        if (!isInstagramUrl(cleanInput)) {
            return@withContext Result.failure(IllegalArgumentException("El enlace no es un enlace válido de Instagram."))
        }

        val ref = extractInstagramRef(cleanInput)
        val shortcode = ref?.shortcode ?: run {
            // Seguir redirecciones rápidas si es un enlace corto o compartido
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

        AppLog.d(TAG, "Resolviendo Instagram Reel: raw='$cleanInput' -> targetUrl='$targetUrl' (shortcode=$shortcode)")

        // 1. Motor 1: Googlebot SSR Scraping (Instagram sirve data-sjs con video_versions y CDN .mp4 directos para crawlers)
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

                val extracted = extractMediaFromText(html, shortcode)
                if (extracted != null) {
                    AppLog.d(TAG, "Instagram resuelto con éxito vía Motor 1 (Googlebot SSR)")
                    return@withContext Result.success(extracted)
                }
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Motor 1 (Googlebot SSR) falló: ${e.message}")
        }

        // 2. Motor 2: Mobile Chrome SSR Scraper (Pixel User-Agent con headers de navegación reales)
        try {
            val conn = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 12000
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                setRequestProperty("Accept-Language", "es-ES,es;q=0.9,en;q=0.8")
                setRequestProperty("Sec-Fetch-Mode", "navigate")
                setRequestProperty("Sec-Fetch-Site", "none")
            }

            if (conn.responseCode in 200..299) {
                val html = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()

                val extracted = extractMediaFromText(html, shortcode)
                if (extracted != null) {
                    AppLog.d(TAG, "Instagram resuelto con éxito vía Motor 2 (Mobile Chrome)")
                    return@withContext Result.success(extracted)
                }
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Motor 2 (Mobile Chrome) falló: ${e.message}")
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

                val extracted = extractMediaFromText(html, shortcode)
                if (extracted != null) {
                    AppLog.d(TAG, "Instagram resuelto con éxito vía Motor 3 (FacebookExternalHit)")
                    return@withContext Result.success(extracted)
                }
            } else {
                conn.disconnect()
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Motor 3 (FacebookExternalHit) falló: ${e.message}")
        }

        // 4. Motor 4: Embed Scraper (/embed/ y /embed/captioned/)
        if (shortcode != null) {
            val embedEndpoints = listOf(
                "https://www.instagram.com/reel/$shortcode/embed/",
                "https://www.instagram.com/reel/$shortcode/embed/captioned/",
                "https://www.instagram.com/p/$shortcode/embed/",
                "https://www.instagram.com/p/$shortcode/embed/captioned/"
            )
            for (embedUrl in embedEndpoints) {
                try {
                    val embedConn = (URL(embedUrl).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 8000
                        readTimeout = 10000
                        requestMethod = "GET"
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                        setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    }

                    if (embedConn.responseCode in 200..299) {
                        val embedHtml = embedConn.inputStream.bufferedReader().use { it.readText() }
                        embedConn.disconnect()

                        val extracted = extractMediaFromText(embedHtml, shortcode)
                        if (extracted != null) {
                            AppLog.d(TAG, "Instagram resuelto con éxito vía Motor 4 (Embed $embedUrl)")
                            return@withContext Result.success(extracted)
                        }
                    } else {
                        embedConn.disconnect()
                    }
                } catch (e: Exception) {
                    AppLog.w(TAG, "Motor 4 falló para $embedUrl: ${e.message}")
                }
            }
        }

        // 5. Motor 5: Web Info API & GraphQL con App ID oficial de Instagram Web
        if (shortcode != null) {
            try {
                val apiUrls = listOf(
                    "https://www.instagram.com/graphql/query/?query_hash=b3055c2c9702560e7f346e45508c0c0e&variables=%7B%22shortcode%22%3A%22$shortcode%22%7D",
                    "https://www.instagram.com/api/v1/media/web_info/?shortcode=$shortcode",
                    "https://www.instagram.com/reel/$shortcode/?__a=1&__d=dis"
                )
                for (apiUrl in apiUrls) {
                    val apiConn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 9000
                        readTimeout = 10000
                        requestMethod = "GET"
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", USER_AGENT)
                        setRequestProperty("X-IG-App-ID", "936619743392459")
                        setRequestProperty("X-ASBD-ID", "198387")
                        setRequestProperty("X-Requested-With", "XMLHttpRequest")
                        setRequestProperty("Accept", "application/json, text/plain, */*")
                    }

                    if (apiConn.responseCode in 200..299) {
                        val jsonText = apiConn.inputStream.bufferedReader().use { it.readText() }
                        apiConn.disconnect()

                        val extracted = extractMediaFromText(jsonText, shortcode)
                        if (extracted != null) {
                            AppLog.d(TAG, "Instagram resuelto con éxito vía Motor 5 (Web Info/GraphQL)")
                            return@withContext Result.success(extracted)
                        }
                    } else {
                        apiConn.disconnect()
                    }
                }
            } catch (e: Exception) {
                AppLog.w(TAG, "Motor 5 (Web Info API) falló: ${e.message}")
            }
        }

        Result.failure(Exception("No se pudo obtener el video de este Reel. Comprueba tu conexión a internet o asegúrate de que el enlace sea accesible."))
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
