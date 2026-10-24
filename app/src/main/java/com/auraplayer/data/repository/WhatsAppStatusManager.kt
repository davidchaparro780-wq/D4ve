package com.auraplayer.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.LruCache
import androidx.core.content.FileProvider
import com.auraplayer.util.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class WhatsAppStatusItem(
    val file: File,
    val uri: Uri,
    val isVideo: Boolean,
    val timestamp: Long,
    val size: Long,
    val isRecent24h: Boolean = (System.currentTimeMillis() - timestamp) in 0L..(24L * 60 * 60 * 1000L),
    val path: String = file.absolutePath
) {
    fun getRelativeTimeSpan(): String {
        val diff = System.currentTimeMillis() - timestamp
        if (diff < 0) return "Ahora"
        val minutes = diff / (60 * 1000)
        val hours = diff / (60 * 60 * 1000)
        val days = diff / (24 * 60 * 60 * 1000)
        return when {
            minutes < 1 -> "Hace un momento"
            minutes < 60 -> "Hace ${minutes}m"
            hours < 24 -> "Hace ${hours}h"
            days == 1L -> "Ayer"
            days < 7 -> "Hace ${days}d"
            else -> {
                val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                sdf.format(java.util.Date(timestamp))
            }
        }
    }
}

object WhatsAppStatusManager {
    private const val TAG = "WhatsAppStatusManager"

    private val POTENTIAL_STATUS_DIRS = listOf(
        // WhatsApp estándar en Android 11+
        File(Environment.getExternalStorageDirectory(), "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"),
        // WhatsApp Business en Android 11+
        File(Environment.getExternalStorageDirectory(), "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses"),
        // WhatsApp legado en raíz de almacenamiento
        File(Environment.getExternalStorageDirectory(), "WhatsApp/Media/.Statuses"),
        // WhatsApp Business legado
        File(Environment.getExternalStorageDirectory(), "WhatsApp Business/Media/.Statuses"),
        // GBWhatsApp / Mods comunes
        File(Environment.getExternalStorageDirectory(), "Android/media/com.gbwhatsapp/GBWhatsApp/Media/.Statuses"),
        File(Environment.getExternalStorageDirectory(), "GBWhatsApp/Media/.Statuses"),
        // Clones y Dual App
        File(Environment.getExternalStorageDirectory(), "DualApp/WhatsApp/Media/.Statuses")
    )

    /**
     * Obtiene la lista de todos los estados vistos de WhatsApp en el dispositivo,
     * priorizando de forma inteligente los estados de las últimas 24 horas y resolviendo
     * la fecha real de llegada mediante BasicFileAttributes.
     */
    suspend fun getStatuses(context: Context): List<WhatsAppStatusItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<WhatsAppStatusItem>()
        val now = System.currentTimeMillis()

        for (dir in POTENTIAL_STATUS_DIRS) {
            if (dir.exists() && dir.isDirectory) {
                val files = dir.listFiles() ?: continue
                for (file in files) {
                    if (file.isFile && !file.name.startsWith(".nomedia") && file.length() > 0) {
                        val name = file.name.lowercase()
                        val isVideo = name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".3gp")
                        val isImage = name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")
                        if (isVideo || isImage) {
                            val uri = try {
                                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            } catch (_: Exception) {
                                Uri.fromFile(file)
                            }

                            // Resolver timestamp real de llegada al dispositivo
                            val resolvedTimestamp = try {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val attrs = java.nio.file.Files.readAttributes(
                                        file.toPath(),
                                        java.nio.file.attribute.BasicFileAttributes::class.java
                                    )
                                    val creation = attrs.creationTime().toMillis()
                                    val modified = attrs.lastModifiedTime().toMillis()
                                    val fileMod = file.lastModified()
                                    val candidates = listOf(creation, modified, fileMod).filter { it in 1L..(now + 60_000L) }
                                    candidates.maxOrNull() ?: fileMod
                                } else {
                                    file.lastModified()
                                }
                            } catch (_: Throwable) {
                                file.lastModified()
                            }

                            list.add(
                                WhatsAppStatusItem(
                                    file = file,
                                    uri = uri,
                                    isVideo = isVideo,
                                    timestamp = resolvedTimestamp,
                                    size = file.length()
                                )
                            )
                        }
                    }
                }
            }
        }

        // Ordenar con máxima prioridad para las últimas 24 horas, y dentro de cada grupo por el más reciente
        list.sortWith(
            compareByDescending<WhatsAppStatusItem> { it.isRecent24h }
                .thenByDescending { it.timestamp }
        )
        list
    }

    /**
     * Guarda permanentemente un estado en la galería pública (DCIM/DaVEPlayer/WhatsApp_Statuses).
     */
    suspend fun saveStatusToGallery(context: Context, item: WhatsAppStatusItem): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val extension = if (item.isVideo) ".mp4" else ".jpg"
            val mimeType = if (item.isVideo) "video/mp4" else "image/jpeg"
            val fileName = "Status_${System.currentTimeMillis()}$extension"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val collectionUri = if (item.isVideo) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val relativePath = if (item.isVideo) "DCIM/DaVEPlayer/WhatsApp_Statuses" else "Pictures/DaVEPlayer/WhatsApp_Statuses"

                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                    put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
                }

                val targetUri = context.contentResolver.insert(collectionUri, values)
                if (targetUri != null) {
                    context.contentResolver.openOutputStream(targetUri)?.use { out ->
                        FileInputStream(item.file).use { input ->
                            input.copyTo(out)
                        }
                    }

                    val updateValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.IS_PENDING, 0)
                    }
                    context.contentResolver.update(targetUri, updateValues, null, null)

                    val physicalDir = File(Environment.getExternalStoragePublicDirectory(if (item.isVideo) Environment.DIRECTORY_DCIM else Environment.DIRECTORY_PICTURES), "DaVEPlayer/WhatsApp_Statuses")
                    val physicalFile = File(physicalDir, fileName)
                    MediaScannerConnection.scanFile(context, arrayOf(physicalFile.absolutePath), arrayOf(mimeType), null)
                    return@withContext Result.success(targetUri)
                }
            }

            // Fallback Android 9 o anterior
            val baseDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "DaVEPlayer/WhatsApp_Statuses")
            if (!baseDir.exists()) baseDir.mkdirs()
            val targetFile = File(baseDir, fileName)
            FileInputStream(item.file).use { input ->
                FileOutputStream(targetFile).use { out ->
                    input.copyTo(out)
                }
            }
            val finalUri = Uri.fromFile(targetFile)
            MediaScannerConnection.scanFile(context, arrayOf(targetFile.absolutePath), arrayOf(mimeType), null)
            Result.success(finalUri)
        } catch (e: Exception) {
            AppLog.e(TAG, "Error guardando estado de WhatsApp", e)
            Result.failure(e)
        }
    }

    /**
     * Comparte el estado directamente a cualquier aplicación (WhatsApp, Instagram, etc.).
     */
    fun shareStatus(context: Context, item: WhatsAppStatusItem) {
        try {
            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", item.file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (item.isVideo) "video/mp4" else "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Compartir Estado con...").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            AppLog.e(TAG, "Error compartiendo estado", e)
        }
    }

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8 // 1/8th of app memory for status thumbnails

    private val thumbnailCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return value.byteCount / 1024
        }
    }

    fun getCachedThumbnail(path: String): Bitmap? = thumbnailCache.get(path)

    fun putCachedThumbnail(path: String, bitmap: Bitmap) {
        thumbnailCache.put(path, bitmap)
    }

    /**
     * Extrae de forma eficiente la miniatura de un estado (fotograma de video a 500ms o foto optimizada).
     */
    suspend fun loadThumbnail(context: Context, item: WhatsAppStatusItem): Bitmap? = withContext(Dispatchers.IO) {
        val cached = getCachedThumbnail(item.file.absolutePath)
        if (cached != null) return@withContext cached

        var resultBitmap: Bitmap? = null

        if (item.isVideo) {
            val retriever = MediaMetadataRetriever()
            try {
                if (item.file.exists() && item.file.canRead()) {
                    retriever.setDataSource(item.file.absolutePath)
                } else {
                    retriever.setDataSource(context, item.uri)
                }

                val rawFrame: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    try {
                        retriever.getScaledFrameAtTime(500000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, 360, 480)
                    } catch (_: Throwable) { null }
                        ?: retriever.getFrameAtTime(500000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        ?: retriever.frameAtTime
                } else {
                    retriever.getFrameAtTime(500000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                        ?: retriever.frameAtTime
                }

                resultBitmap = rawFrame
            } catch (e: Exception) {
                AppLog.w(TAG, "Fallo al extraer fotograma de video ${item.file.name}: ${e.message}")
            } finally {
                try { retriever.release() } catch (_: Throwable) {}
            }

            if (resultBitmap == null) {
                try {
                    @Suppress("DEPRECATION")
                    resultBitmap = ThumbnailUtils.createVideoThumbnail(
                        item.file.absolutePath,
                        MediaStore.Images.Thumbnails.MINI_KIND
                    )
                } catch (_: Throwable) {}
            }
        } else {
            try {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                if (item.file.exists() && item.file.canRead()) {
                    BitmapFactory.decodeFile(item.file.absolutePath, options)
                } else {
                    context.contentResolver.openInputStream(item.uri)?.use {
                        BitmapFactory.decodeStream(it, null, options)
                    }
                }

                val reqW = 360
                val reqH = 480
                var inSampleSize = 1
                if (options.outHeight > reqH || options.outWidth > reqW) {
                    val halfH = options.outHeight / 2
                    val halfW = options.outWidth / 2
                    while (halfH / inSampleSize >= reqH && halfW / inSampleSize >= reqW) {
                        inSampleSize *= 2
                    }
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                    inPreferredConfig = Bitmap.Config.RGB_565
                }

                resultBitmap = if (item.file.exists() && item.file.canRead()) {
                    BitmapFactory.decodeFile(item.file.absolutePath, decodeOptions)
                } else {
                    context.contentResolver.openInputStream(item.uri)?.use {
                        BitmapFactory.decodeStream(it, null, decodeOptions)
                    }
                }
            } catch (e: Exception) {
                AppLog.w(TAG, "Fallo al decodificar imagen ${item.file.name}: ${e.message}")
            }
        }

        if (resultBitmap != null) {
            putCachedThumbnail(item.file.absolutePath, resultBitmap)
        }

        resultBitmap
    }
}
