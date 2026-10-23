package com.auraplayer.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
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
    val size: Long
)

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
        File(Environment.getExternalStorageDirectory(), "WhatsApp Business/Media/.Statuses")
    )

    /**
     * Obtiene la lista de todos los estados vistos de WhatsApp en el dispositivo.
     */
    suspend fun getStatuses(context: Context): List<WhatsAppStatusItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<WhatsAppStatusItem>()
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
                            list.add(
                                WhatsAppStatusItem(
                                    file = file,
                                    uri = uri,
                                    isVideo = isVideo,
                                    timestamp = file.lastModified(),
                                    size = file.length()
                                )
                            )
                        }
                    }
                }
            }
        }
        // Ordenar del más reciente al más antiguo
        list.sortedByDescending { it.timestamp }
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
}
