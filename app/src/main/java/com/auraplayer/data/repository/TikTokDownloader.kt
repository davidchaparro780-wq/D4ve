package com.auraplayer.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
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
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class TikTokVideoDetails(
    val id: String,
    val title: String,
    val author: String,
    val username: String,
    val coverUrl: String,
    val videoNoWatermarkUrl: String,
    val videoHdUrl: String?,
    val audioUrl: String?,
    val durationSeconds: Int = 0
)

sealed class TikTokDownloadProgress {
    object Resolving : TikTokDownloadProgress()
    data class Downloading(val progress: Int) : TikTokDownloadProgress()
    data class Completed(val fileUri: Uri, val filePath: String?, val isVideo: Boolean) : TikTokDownloadProgress()
    data class Error(val message: String) : TikTokDownloadProgress()
}

object TikTokDownloader {
    private const val TAG = "TikTokDownloader"
    private const val USER_AGENT = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
    private const val CHANNEL_ID = "dave_social_downloads"
    private const val NOTIFICATION_ID = 8841

    fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Descargas de Redes Sociales",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Progreso de descarga de videos de TikTok e Instagram"
                setShowBadge(false)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(channel)
        }
    }

    fun showProgressNotification(context: Context, title: String, author: String, progress: Int) {
        try {
            ensureNotificationChannel(context)
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val notif = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle("📥 Descargando TikTok sin marca ($progress%)")
                .setContentText("@$author: ${title.take(45)}")
                .setProgress(100, progress, progress == 0)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
            nm.notify(NOTIFICATION_ID, notif)
        } catch (_: Exception) {}
    }

    fun showCompletedNotification(context: Context, title: String, author: String, isVideo: Boolean, uri: Uri) {
        try {
            ensureNotificationChannel(context)
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            nm.cancel(NOTIFICATION_ID)

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, if (isVideo) "video/*" else "audio/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val pi = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val notif = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("🎬 TikTok Descargado sin marca")
                .setContentText("@$author: ${title.take(45)} • Toca para reproducir o enviar a WhatsApp")
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            nm.notify((System.currentTimeMillis() % 10000).toInt() + 1000, notif)
        } catch (_: Exception) {}
    }

    fun cancelNotification(context: Context) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.cancel(NOTIFICATION_ID)
        } catch (_: Exception) {}
    }

    fun isTikTokUrl(url: String): Boolean {
        val trimmed = url.trim().lowercase()
        return (trimmed.contains("tiktok.com") || trimmed.contains("douyin.com") || trimmed.contains("tikwm.com") || trimmed.contains("tiktokv.com")) &&
                (trimmed.startsWith("http://") || trimmed.startsWith("https://"))
    }

    /**
     * Extrae de forma limpia cualquier enlace de TikTok contenido dentro de un texto compartido.
     * Soporta enlaces cortos (vm.tiktok.com, vt.tiktok.com, tiktok.com/t/) y texto con comentarios o hashtags,
     * eliminando cualquier puntuación final accidental.
     */
    fun extractTikTokUrl(text: String): String? {
        val trimmed = text.trim()
        val urlRegex = Regex("""https?://[^\s<>"'{}|\\^`]+""")
        val matches = urlRegex.findAll(trimmed)
        for (match in matches) {
            val candidate = match.value.trim().trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}', '"', '\'')
            if (isTikTokUrl(candidate)) {
                return candidate
            }
        }
        val cleanTrimmed = trimmed.trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}', '"', '\'')
        if (isTikTokUrl(cleanTrimmed)) return cleanTrimmed
        return null
    }

    /**
     * Resuelve redirecciones 301/302 de enlaces cortos (vm.tiktok.com, vt.tiktok.com, tiktok.com/t/)
     * y elimina parámetros de tracking (?_t=..., ?_r=..., etc.) para obtener la URL canónica del video.
     */
    suspend fun unshortenAndCleanTikTokUrl(rawUrl: String): String = withContext(Dispatchers.IO) {
        var currentUrl = rawUrl.trim().trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}', '"', '\'')

        // Si ya es un enlace directo a video con query params, limpiamos los parámetros de tracking
        if (currentUrl.contains("/video/")) {
            val qIdx = currentUrl.indexOf('?')
            if (qIdx != -1) {
                currentUrl = currentUrl.substring(0, qIdx)
            }
            return@withContext currentUrl
        }

        try {
            var redirects = 0
            while (redirects < 6) {
                val urlObj = URL(currentUrl)
                val conn = (urlObj.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 8000
                    readTimeout = 8000
                    instanceFollowRedirects = false
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", USER_AGENT)
                    setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                }
                val code = conn.responseCode
                if (code in 301..308) {
                    val location = conn.getHeaderField("Location")
                    conn.disconnect()
                    if (!location.isNullOrBlank()) {
                        currentUrl = if (location.startsWith("http://") || location.startsWith("https://")) {
                            location
                        } else {
                            URL(urlObj, location).toString()
                        }
                        redirects++
                        if (currentUrl.contains("/video/")) {
                            val qIdx = currentUrl.indexOf('?')
                            if (qIdx != -1) {
                                currentUrl = currentUrl.substring(0, qIdx)
                            }
                            break
                        }
                        continue
                    }
                } else {
                    conn.disconnect()
                }
                break
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Error resolviendo redirecciones para $rawUrl: ${e.message}")
        }

        val qIdx = currentUrl.indexOf('?')
        if (qIdx != -1 && currentUrl.contains("/video/")) {
            currentUrl = currentUrl.substring(0, qIdx)
        }
        currentUrl
    }

    /**
     * Resuelve los enlaces limpios sin marca de agua del video de TikTok a través de múltiples motores resilientes.
     */
    suspend fun resolveTikTok(rawUrl: String): Result<TikTokVideoDetails> = withContext(Dispatchers.IO) {
        val cleanInput = rawUrl.trim().trimEnd('.', ',', ';', ':', '!', '?', ')', ']', '}', '"', '\'')
        if (!isTikTokUrl(cleanInput)) {
            return@withContext Result.failure(IllegalArgumentException("El enlace no parece ser un enlace válido de TikTok."))
        }

        // Desreferenciar URLs cortas y limpiar tracking
        val canonicalUrl = unshortenAndCleanTikTokUrl(cleanInput)
        AppLog.d(TAG, "Resolviendo TikTok: original='$cleanInput' -> canonica='$canonicalUrl'")

        // Helper para consultar TikWM
        fun queryTikWm(targetUrl: String): TikTokVideoDetails? {
            var conn: HttpURLConnection? = null
            try {
                val encoded = URLEncoder.encode(targetUrl, "UTF-8")
                val tikWmApi = "https://www.tikwm.com/api/?url=$encoded&hd=1"
                conn = (URL(tikWmApi).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 12000
                    readTimeout = 15000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", USER_AGENT)
                    setRequestProperty("Accept", "application/json")
                }

                if (conn.responseCode == 200) {
                    val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                    val root = JSONObject(responseText)
                    if (root.optInt("code", -1) == 0) {
                        val data = root.optJSONObject("data")
                        if (data != null) {
                            val videoUrl = data.optString("play", "")
                            val hdVideoUrl = data.optString("hdplay", "").takeIf { it.isNotBlank() }
                            val audioUrl = data.optString("music", "").takeIf { it.isNotBlank() }
                            val cover = data.optString("cover", "")
                            val title = data.optString("title", "Video de TikTok").trim().ifBlank { "Video de TikTok" }
                            val authorObj = data.optJSONObject("author")
                            val authorName = authorObj?.optString("nickname", "TikTok Creator") ?: "TikTok Creator"
                            val username = authorObj?.optString("unique_id", "tiktok") ?: "tiktok"
                            val id = data.optString("id", System.currentTimeMillis().toString())
                            val duration = data.optInt("duration", 0)

                            val finalVideoUrl = videoUrl.ifBlank { hdVideoUrl ?: "" }
                            if (finalVideoUrl.isNotBlank()) {
                                return TikTokVideoDetails(
                                    id = id,
                                    title = title,
                                    author = authorName,
                                    username = username,
                                    coverUrl = cover,
                                    videoNoWatermarkUrl = finalVideoUrl,
                                    videoHdUrl = hdVideoUrl,
                                    audioUrl = audioUrl,
                                    durationSeconds = duration
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                AppLog.w(TAG, "TikWM fallo con URL $targetUrl: ${e.message}")
            } finally {
                try { conn?.disconnect() } catch (_: Throwable) {}
            }
            return null
        }

        // 1. Motor 1: TikWM con la URL canónica limpia
        queryTikWm(canonicalUrl)?.let {
            AppLog.d(TAG, "TikTok resuelto exitosamente via TikWM con URL canónica: ${it.id}")
            return@withContext Result.success(it)
        }

        // 2. Motor 2: TikTok oficial oEmbed -> extrae embed_product_id -> consulta TikWM con ID directo
        try {
            val encodedCanonical = URLEncoder.encode(canonicalUrl, "UTF-8")
            val oembedUrl = "https://www.tiktok.com/oembed?url=$encodedCanonical"
            val oembedConn = (URL(oembedUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "application/json")
            }
            if (oembedConn.responseCode == 200) {
                val oembedText = oembedConn.inputStream.bufferedReader().use { it.readText() }
                oembedConn.disconnect()
                val oembedJson = JSONObject(oembedText)
                val videoId = oembedJson.optString("embed_product_id", "")
                if (videoId.isNotBlank()) {
                    AppLog.d(TAG, "ID de TikTok resuelto via oEmbed: $videoId")
                    queryTikWm("https://www.tiktok.com/@tiktok/video/$videoId")?.let {
                        return@withContext Result.success(it)
                    }
                }
            } else {
                oembedConn.disconnect()
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Fallo TikTok oEmbed: ${e.message}")
        }

        // 3. Motor 3: TikWM con la URL de entrada original por si acaso
        if (cleanInput != canonicalUrl) {
            queryTikWm(cleanInput)?.let {
                AppLog.d(TAG, "TikTok resuelto con URL original: ${it.id}")
                return@withContext Result.success(it)
            }
        }

        // 4. Motor 4: Scraping directo de HTML para playAddr
        try {
            val scrapeConn = (URL(canonicalUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = 10000
                readTimeout = 12000
                requestMethod = "GET"
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            }
            if (scrapeConn.responseCode in 200..299) {
                val html = scrapeConn.inputStream.bufferedReader().use { it.readText() }
                scrapeConn.disconnect()

                val playAddrRegex = Regex(""""(?:playAddr|downloadAddr)":"(https:[^"]+)"""")
                val match = playAddrRegex.find(html)?.groupValues?.get(1)
                if (!match.isNullOrBlank()) {
                    val decodedUrl = match.replace("\\u0026", "&").replace("\\/", "/")
                    val titleRegex = Regex("""<title>([^<]+)</title>""")
                    val rawTitle = titleRegex.find(html)?.groupValues?.get(1) ?: "Video de TikTok"

                    return@withContext Result.success(
                        TikTokVideoDetails(
                            id = System.currentTimeMillis().toString(),
                            title = rawTitle.replace(" | TikTok", "").trim(),
                            author = "TikTok Creator",
                            username = "tiktok",
                            coverUrl = "",
                            videoNoWatermarkUrl = decodedUrl,
                            videoHdUrl = null,
                            audioUrl = null,
                            durationSeconds = 0
                        )
                    )
                }
            } else {
                scrapeConn.disconnect()
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Direct HTML scraping fallo: ${e.message}")
        }

        Result.failure(Exception("No se pudo obtener el video sin marca de agua de TikTok. Verifica que el enlace sea accesible e intenta nuevamente."))
    }

    /**
     * Descarga el archivo de video (MP4) o audio (MP3) en el almacenamiento público del dispositivo
     * y notifica a MediaStore y MediaScannerConnection para que aparezca al instante en la app, galería y WhatsApp.
     */
    suspend fun downloadMedia(
        context: Context,
        downloadUrl: String,
        details: TikTokVideoDetails,
        asAudioOnly: Boolean = false,
        onProgress: (Int) -> Unit
    ): Result<Uri> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        var inputStream: java.io.InputStream? = null
        try {
            val isVideo = !asAudioOnly
            val extension = if (isVideo) ".mp4" else ".mp3"
            val mimeType = if (isVideo) "video/mp4" else "audio/mpeg"

            val safeTitle = sanitizeFileName(details.title).take(50).ifBlank { "TikTok_${details.id}" }
            val safeAuthor = sanitizeFileName(details.author).take(30).ifBlank { details.username }
            val fileName = "TikTok - ${safeAuthor}_$safeTitle$extension"

            showProgressNotification(context, details.title, details.author, 0)

            var currentUrl = downloadUrl
            var redirects = 0

            while (true) {
                val urlObj = URL(currentUrl)
                val conn = (urlObj.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 20000
                    readTimeout = 25000
                    requestMethod = "GET"
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", USER_AGENT)
                    setRequestProperty("Accept", "*/*")
                    setRequestProperty("Connection", "keep-alive")
                }
                connection = conn
                val code = conn.responseCode
                if ((code in 301..303 || code == 307 || code == 308) && redirects < 6) {
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

            val finalConn = connection ?: return@withContext Result.failure(Exception("Fallo al conectar con el servidor"))
            if (finalConn.responseCode !in 200..299) {
                return@withContext Result.failure(Exception("Error del servidor: HTTP ${finalConn.responseCode}"))
            }

            val contentLength = finalConn.getHeaderField("Content-Length")?.toLongOrNull() ?: finalConn.contentLength.toLong()
            val stream = finalConn.inputStream
            inputStream = stream

            var finalUri: Uri? = null
            var finalPath: String? = null
            val currentTime = System.currentTimeMillis()

            // Almacenamiento Scoped Storage (Android 10+)
            // Guardamos videos en DCIM/DaVEPlayer para indexación inmediata en Galería, WhatsApp y Estados
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                var pendingMediaUri: Uri? = null
                try {
                    val collectionUri = if (isVideo) {
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    } else {
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }

                    val relativePath = if (isVideo) "DCIM/DaVEPlayer" else "Music/DaVEPlayer"

                    val values = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                        put(MediaStore.MediaColumns.DATE_ADDED, currentTime / 1000)
                        put(MediaStore.MediaColumns.DATE_MODIFIED, currentTime / 1000)
                        if (!isVideo) {
                            put(MediaStore.Audio.Media.TITLE, details.title)
                            put(MediaStore.Audio.Media.ARTIST, details.author)
                        } else {
                            put(MediaStore.Video.Media.TITLE, details.title)
                            put(MediaStore.Video.Media.DATE_TAKEN, currentTime)
                            if (details.durationSeconds > 0) {
                                put(MediaStore.Video.Media.DURATION, details.durationSeconds * 1000L)
                            }
                        }
                    }

                    val uri = context.contentResolver.insert(collectionUri, values)
                    pendingMediaUri = uri
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            copyStreamWithProgress(inputStream, output, contentLength) { pct ->
                                onProgress(pct)
                                if (pct % 5 == 0 || pct == 100) {
                                    showProgressNotification(context, details.title, details.author, pct)
                                }
                            }
                        }

                        // Extraer metadatos técnicos (duración, dimensiones) para que WhatsApp no rechace el archivo
                        var realDuration = if (details.durationSeconds > 0) details.durationSeconds * 1000L else 15000L
                        var videoWidth = 720
                        var videoHeight = 1280
                        val retriever = android.media.MediaMetadataRetriever()
                        try {
                            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                                retriever.setDataSource(pfd.fileDescriptor)
                                val durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
                                val wStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                                val hStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                                durStr?.toLongOrNull()?.let { if (it > 0) realDuration = it }
                                wStr?.toIntOrNull()?.let { if (it > 0) videoWidth = it }
                                hStr?.toIntOrNull()?.let { if (it > 0) videoHeight = it }
                            }
                        } catch (e: Exception) {
                            AppLog.w(TAG, "No se pudieron extraer metadatos con retriever", e)
                        } finally {
                            try { retriever.release() } catch (_: Exception) {}
                        }

                        val updateValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.IS_PENDING, 0)
                            if (isVideo) {
                                put(MediaStore.Video.Media.DURATION, realDuration)
                                put(MediaStore.Video.Media.WIDTH, videoWidth)
                                put(MediaStore.Video.Media.HEIGHT, videoHeight)
                            }
                        }
                        context.contentResolver.update(uri, updateValues, null, null)
                        finalUri = uri

                        // Forzar escaneo en MediaScanner con la ruta real en disco para indexación inmediata en WhatsApp
                        try {
                            var resolvedPath: String? = null
                            context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)?.use { cursor ->
                                if (cursor.moveToFirst()) {
                                    resolvedPath = cursor.getString(0)
                                }
                            }
                            val physicalDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "DaVEPlayer")
                            val physicalFile = File(physicalDir, fileName)
                            val pathToScan = resolvedPath ?: physicalFile.absolutePath
                            MediaScannerConnection.scanFile(context, arrayOf(pathToScan), arrayOf(mimeType)) { path, scannedUri ->
                                AppLog.d(TAG, "MediaScanner indexó archivo para WhatsApp: $path -> $scannedUri")
                            }
                        } catch (e: Exception) {
                            AppLog.w(TAG, "Error escaneando ruta en MediaScanner", e)
                        }
                    }
                } catch (e: Exception) {
                    AppLog.w(TAG, "Fallo al guardar vía MediaStore", e)
                    pendingMediaUri?.let {
                        try { context.contentResolver.delete(it, null, null) } catch (_: Exception) {}
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        return@withContext Result.failure(e)
                    }
                }
            }

            // Fallback para Android 9 o si falló MediaStore
            if (finalUri == null) {
                val baseDir = if (isVideo) {
                    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "DaVEPlayer")
                } else {
                    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "DaVEPlayer")
                }
                if (!baseDir.exists()) baseDir.mkdirs()

                val targetFile = File(baseDir, fileName)
                FileOutputStream(targetFile).use { output ->
                    copyStreamWithProgress(inputStream, output, contentLength) { pct ->
                        onProgress(pct)
                        if (pct % 5 == 0 || pct == 100) {
                            showProgressNotification(context, details.title, details.author, pct)
                        }
                    }
                }
                finalPath = targetFile.absolutePath
                finalUri = try {
                    androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", targetFile)
                } catch (e: Exception) {
                    Uri.fromFile(targetFile)
                }

                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(targetFile.absolutePath),
                    arrayOf(mimeType),
                    null
                )
            }

            val resultUri = finalUri ?: return@withContext Result.failure(Exception("No se pudo obtener el URI del archivo guardado"))
            AppLog.i(TAG, "Descarga completada con compatibilidad WhatsApp: $fileName ($resultUri)")
            showCompletedNotification(context, details.title, details.author, isVideo, resultUri)
            Result.success(resultUri)
        } catch (e: Exception) {
            AppLog.e(TAG, "Error durante la descarga de TikTok", e)
            cancelNotification(context)
            Result.failure(e)
        } finally {
            try { inputStream?.close() } catch (_: Throwable) {}
            try { connection?.disconnect() } catch (_: Throwable) {}
        }
    }

    /**
     * Resuelve un URI compatible con FileProvider para que aplicaciones externas (como WhatsApp)
     * puedan acceder y transmitir el archivo sin restricciones de Scoped Storage ni SecurityException.
     */
    fun getShareableUri(context: Context, uri: Uri, isVideo: Boolean): Uri {
        try {
            if (uri.authority?.contains("fileprovider") == true) {
                return uri
            }
            var resolvedFile: File? = null
            try {
                context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.DATA), null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val path = cursor.getString(0)
                        if (!path.isNullOrBlank()) {
                            val f = File(path)
                            if (f.exists()) resolvedFile = f
                        }
                    }
                }
            } catch (_: Exception) {}

            if (resolvedFile == null) {
                val dir = if (isVideo) {
                    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "DaVEPlayer")
                } else {
                    File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "DaVEPlayer")
                }
                if (dir.exists()) {
                    val files = dir.listFiles()?.filter { it.isFile && (if (isVideo) it.name.endsWith(".mp4") else it.name.endsWith(".mp3")) }
                        ?.sortedByDescending { it.lastModified() }
                    resolvedFile = files?.firstOrNull()
                }
            }

            if (resolvedFile != null && resolvedFile.exists()) {
                return androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    resolvedFile
                )
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Error resolviendo FileProvider Uri para compartir", e)
        }
        return uri
    }

    /**
     * Comparte directamente a WhatsApp, WhatsApp Business o al selector general de aplicaciones.
     */
    fun shareMedia(context: Context, uri: Uri, isVideo: Boolean, specificPackage: String? = null) {
        try {
            val mimeType = if (isVideo) "video/mp4" else "audio/mpeg"
            val shareUri = getShareableUri(context, uri, isVideo)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, shareUri)
                clipData = ClipData.newRawUri("", shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (!specificPackage.isNullOrBlank()) {
                    setPackage(specificPackage)
                }
            }

            // Conceder permisos explícitos de lectura de URI a WhatsApp y WhatsApp Business
            try {
                val targets = listOfNotNull(specificPackage, "com.whatsapp", "com.whatsapp.w4b")
                for (pkg in targets) {
                    context.grantUriPermission(pkg, shareUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            } catch (_: Exception) {}

            val pm = context.packageManager
            if (!specificPackage.isNullOrBlank()) {
                val resolves = intent.resolveActivity(pm)
                if (resolves != null) {
                    context.startActivity(intent)
                    return
                } else if (specificPackage == "com.whatsapp") {
                    intent.setPackage("com.whatsapp.w4b")
                    if (intent.resolveActivity(pm) != null) {
                        context.startActivity(intent)
                        return
                    }
                }
            }
            intent.setPackage(null)
            val title = if (isVideo) "Compartir Video en WhatsApp y Redes" else "Compartir Audio"
            val chooser = Intent.createChooser(intent, title).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            AppLog.e(TAG, "Error al compartir archivo", e)
            android.widget.Toast.makeText(context, "No se pudo compartir: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyStreamWithProgress(
        input: InputStream,
        output: OutputStream,
        totalBytes: Long,
        onProgress: (Int) -> Unit
    ) {
        val buffer = ByteArray(16 * 1024)
        var bytesRead: Int
        var totalRead = 0L
        var lastReported = -1

        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
            totalRead += bytesRead
            if (totalBytes > 0) {
                val progress = ((totalRead * 100) / totalBytes).toInt().coerceIn(0, 100)
                if (progress != lastReported) {
                    lastReported = progress
                    onProgress(progress)
                }
            } else {
                // Si el servidor no envió Content-Length, simular un progreso razonable
                val estimated = (totalRead / (100 * 1024)).toInt().coerceIn(10, 90)
                if (estimated != lastReported) {
                    lastReported = estimated
                    onProgress(estimated)
                }
            }
        }
        output.flush()
        onProgress(100)
    }

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[\\\\/:*?\"<>|\\r\\n]"), "_")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
