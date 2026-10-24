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
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.LruCache
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.auraplayer.util.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class WhatsAppStatusItem(
    val file: File? = null,
    val uri: Uri,
    val isVideo: Boolean,
    val timestamp: Long,
    val size: Long,
    val isRecent24h: Boolean = (System.currentTimeMillis() - timestamp) in 0L..(24L * 60 * 60 * 1000L),
    val path: String = file?.absolutePath ?: uri.toString()
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
    private const val PREFS_NAME = "dave_whatsapp_prefs"
    private const val KEY_SAF_TREE_URIS = "saf_tree_uris"

    private val POTENTIAL_STATUS_DIRS = listOf(
        // WhatsApp estándar en Android 11+
        File(Environment.getExternalStorageDirectory(), "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"),
        File(Environment.getExternalStorageDirectory(), "Android/media/com.whatsapp/WhatsApp/Media/.statuses"),
        // WhatsApp Business en Android 11+
        File(Environment.getExternalStorageDirectory(), "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses"),
        File(Environment.getExternalStorageDirectory(), "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.statuses"),
        // Clones y Dual App
        File(Environment.getExternalStorageDirectory(), "DualApp/WhatsApp/Media/.Statuses"),
        File(File("/storage/emulated/999"), "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"),
        // GBWhatsApp / Mods comunes
        File(Environment.getExternalStorageDirectory(), "Android/media/com.gbwhatsapp/GBWhatsApp/Media/.Statuses"),
        File(Environment.getExternalStorageDirectory(), "GBWhatsApp/Media/.Statuses"),
        // WhatsApp legado en raíz de almacenamiento (Solo útil en Android 10 o inferior)
        File(Environment.getExternalStorageDirectory(), "WhatsApp/Media/.Statuses"),
        File(Environment.getExternalStorageDirectory(), "WhatsApp Business/Media/.Statuses")
    )

    fun saveTreeUri(context: Context, treeUri: Uri) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val current = prefs.getStringSet(KEY_SAF_TREE_URIS, emptySet())?.toMutableSet() ?: mutableSetOf()
            current.add(treeUri.toString())
            prefs.edit().putStringSet(KEY_SAF_TREE_URIS, current).apply()
        } catch (e: Exception) {
            AppLog.e(TAG, "Error guardando SAF Tree Uri", e)
        }
    }

    fun getSavedTreeUris(context: Context): List<Uri> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_SAF_TREE_URIS, emptySet()) ?: emptySet()
        return set.mapNotNull {
            try { Uri.parse(it) } catch (_: Exception) { null }
        }
    }

    fun hasSafAccess(context: Context): Boolean {
        val uris = getSavedTreeUris(context)
        if (uris.isEmpty()) return false
        val persisted = context.contentResolver.persistedUriPermissions
        return persisted.any { it.isReadPermission && uris.any { u -> u == it.uri } }
    }

    /**
     * URI inicial para el selector SAF.
     * Apuntamos a la carpeta visible Media para que el selector del sistema se abra
     * con el botón "USAR ESTA CARPETA" habilitado y accesible en Android 11 a 15.
     */
    fun getStatusesFolderInitialUri(isBusiness: Boolean = false): Uri {
        val pkg = if (isBusiness) "com.whatsapp.w4b" else "com.whatsapp"
        val sub = if (isBusiness) "WhatsApp%20Business" else "WhatsApp"
        val docId = "primary:Android/media/$pkg/$sub/Media"
        return Uri.parse("content://com.android.externalstorage.documents/document/${Uri.encode(docId)}")
    }

    /**
     * Construye un Intent de Storage Access Framework que abre el explorador del sistema
     * directamente dentro de la carpeta Media de WhatsApp en Android 11+.
     */
    fun createOpenStatusesFolderIntent(isBusiness: Boolean = false): Intent {
        val initialUri = getStatusesFolderInitialUri(isBusiness)
        return Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            )
        }
    }

    /**
     * Abre directamente la aplicación de WhatsApp o WhatsApp Business para que el usuario
     * pueda ver los estados de sus contactos y descargarlos al dispositivo.
     */
    fun openWhatsApp(context: Context, isBusiness: Boolean = false): Boolean {
        val pkg = if (isBusiness) "com.whatsapp.w4b" else "com.whatsapp"
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(pkg) ?: Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                setPackage(pkg)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            AppLog.w(TAG, "No se pudo abrir WhatsApp ($pkg): ${e.message}")
            false
        }
    }

    /**
     * Extrae recursivamente los archivos de estados a partir de cualquier carpeta vinculada por SAF
     * (sea que el usuario haya seleccionado .Statuses, Media, WhatsApp, com.whatsapp o la raíz).
     */
    private fun findStatusFilesFromTree(rootDoc: DocumentFile): List<DocumentFile> {
        val results = mutableListOf<DocumentFile>()
        val rootName = rootDoc.name ?: ""

        // 1. Si el usuario seleccionó directamente .Statuses
        if (rootName.equals(".Statuses", ignoreCase = true) || rootName.equals("statuses", ignoreCase = true)) {
            return rootDoc.listFiles().filter { it.isFile && it.length() > 0 }
        }

        // 2. Si la carpeta seleccionada contiene directamente la subcarpeta .Statuses
        val directStatuses = rootDoc.findFile(".Statuses")
            ?: rootDoc.findFile(".statuses")
            ?: try {
                rootDoc.listFiles().firstOrNull { it.isDirectory && it.name?.equals(".Statuses", ignoreCase = true) == true }
            } catch (_: Exception) { null }

        if (directStatuses != null && directStatuses.isDirectory) {
            return directStatuses.listFiles().filter { it.isFile && it.length() > 0 }
        }

        // 3. Búsqueda por cola (BFS) hasta 4 niveles de profundidad para encontrar .Statuses
        val queue = ArrayDeque<Pair<DocumentFile, Int>>()
        queue.add(rootDoc to 0)

        while (queue.isNotEmpty()) {
            val (current, depth) = queue.removeFirst()
            if (depth > 4) continue

            val children = try { current.listFiles() } catch (_: Exception) { emptyArray() }
            for (child in children) {
                val cName = child.name ?: ""
                if (child.isDirectory) {
                    if (cName.equals(".Statuses", ignoreCase = true) || cName.equals("statuses", ignoreCase = true)) {
                        val statusFiles = try { child.listFiles() } catch (_: Exception) { emptyArray() }
                        results.addAll(statusFiles.filter { it.isFile && it.length() > 0 })
                    } else if (depth < 3) {
                        val lower = cName.lowercase()
                        if (lower == "media" || lower == "whatsapp" || lower == "whatsapp business" || lower.contains("whatsapp") || lower == "android") {
                            queue.add(child to depth + 1)
                        }
                    }
                } else if (depth == 0 && child.isFile && child.length() > 0) {
                    // Si el usuario seleccionó una carpeta donde ya residen los archivos
                    results.add(child)
                }
            }
        }
        return results
    }

    /**
     * Obtiene la lista de todos los estados vistos de WhatsApp en el dispositivo.
     * Combina Storage Access Framework (SAF) para Android 11+ y acceso directo a archivos,
     * descartando archivos fósiles obsoletos del almacenamiento legado.
     */
    suspend fun getStatuses(context: Context): List<WhatsAppStatusItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<WhatsAppStatusItem>()
        val seenSignatures = mutableSetOf<String>()
        val now = System.currentTimeMillis()
        val isAndroid11Plus = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

        // 1. Motor SAF (Storage Access Framework) — Máxima fiabilidad en Android 11, 12, 13, 14, 15
        val savedUris = getSavedTreeUris(context)
        for (treeUri in savedUris) {
            try {
                val rootDoc = DocumentFile.fromTreeUri(context, treeUri)
                if (rootDoc != null && rootDoc.exists()) {
                    val statusDocFiles = findStatusFilesFromTree(rootDoc)
                    for (doc in statusDocFiles) {
                        if (doc.isFile && doc.length() > 0) {
                            val name = doc.name?.lowercase() ?: ""
                            if (name.startsWith(".nomedia")) continue
                            val isVideo = name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".3gp")
                            val isImage = name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")
                            if (isVideo || isImage) {
                                val time = doc.lastModified().takeIf { it in 1L..(now + 60_000L) } ?: now
                                val size = doc.length()
                                val signature = "${doc.name}_$size"
                                if (seenSignatures.add(signature)) {
                                    // Todo archivo que esté en .Statuses vía SAF en Android 11+ es un estado activo del día
                                    list.add(
                                        WhatsAppStatusItem(
                                            file = null,
                                            uri = doc.uri,
                                            isVideo = isVideo,
                                            timestamp = time,
                                            size = size,
                                            isRecent24h = true,
                                            path = doc.uri.toString()
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                AppLog.w(TAG, "Error leyendo estados vía SAF DocumentFile: ${e.message}")
            }
        }

        // 2. Motor de Archivos Físicos (File System)
        val externalStorage = Environment.getExternalStorageDirectory()
        val dirsToScan = mutableListOf<File>()

        // Rutas directas estándar
        dirsToScan.addAll(POTENTIAL_STATUS_DIRS)

        // Variaciones de mayúsculas y minúsculas comunes en fabricantes
        dirsToScan.add(File(externalStorage, "Android/media/com.whatsapp/WhatsApp/media/.Statuses"))
        dirsToScan.add(File(externalStorage, "Android/media/com.whatsapp/WhatsApp/media/.statuses"))
        dirsToScan.add(File(externalStorage, "Android/media/com.whatsapp.w4b/WhatsApp Business/media/.Statuses"))
        dirsToScan.add(File(externalStorage, "Android/media/com.whatsapp.w4b/WhatsApp Business/media/.statuses"))

        // Escaneo dinámico profundo en Android/media/com.whatsapp
        try {
            val waMediaRoot = File(externalStorage, "Android/media/com.whatsapp")
            if (waMediaRoot.exists() && waMediaRoot.isDirectory) {
                waMediaRoot.listFiles()?.filter { it.isDirectory }?.forEach { appDir ->
                    appDir.listFiles()?.filter { it.isDirectory && it.name.equals("media", ignoreCase = true) }?.forEach { mDir ->
                        val dotStat = File(mDir, ".Statuses")
                        if (dotStat.exists()) dirsToScan.add(dotStat)
                        val lowerStat = File(mDir, ".statuses")
                        if (lowerStat.exists()) dirsToScan.add(lowerStat)
                    }
                }
            }
        } catch (_: Exception) {}

        for (dir in dirsToScan.distinctBy { it.absolutePath }) {
            if (dir.exists() && dir.isDirectory) {
                val isLegacyDir = !dir.absolutePath.contains("Android/media") && !dir.absolutePath.contains("DualApp")
                val files = try { dir.listFiles() } catch (_: Exception) { null } ?: continue
                for (file in files) {
                    if (file.isFile && !file.name.startsWith(".nomedia") && file.length() > 0) {
                        val name = file.name.lowercase()
                        val isVideo = name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".3gp")
                        val isImage = name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")
                        if (isVideo || isImage) {
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

                            // En Android 11+, si el archivo proviene de una carpeta legada (/WhatsApp/Media/.Statuses)
                            // y tiene más de 48 horas de antigüedad, se descarta por ser un residuo obsoleto de hace años.
                            if (isAndroid11Plus && isLegacyDir && (now - resolvedTimestamp) > 48 * 60 * 60 * 1000L) {
                                continue
                            }

                            // Todo archivo que exista en una carpeta activa de WhatsApp (.Statuses en Android/media)
                            // está presente porque el usuario lo miró en las últimas 24 horas (WhatsApp purga automáticamente lo vencido).
                            val isRecent = !isLegacyDir || ((now - resolvedTimestamp) in -600_000L..(24L * 60 * 60 * 1000L))

                            val signature = "${file.name}_${file.length()}"
                            if (seenSignatures.add(signature)) {
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
                                        timestamp = resolvedTimestamp,
                                        size = file.length(),
                                        isRecent24h = isRecent,
                                        path = file.absolutePath
                                    )
                                )
                            }
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
     * Compatible de forma universal tanto con archivos físicos como con DocumentFile (SAF).
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
                        val inStream = if (item.file != null && item.file.exists() && item.file.canRead()) {
                            FileInputStream(item.file)
                        } else {
                            context.contentResolver.openInputStream(item.uri) ?: throw Exception("No se pudo leer el archivo de origen")
                        }
                        inStream.use { input ->
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
            val inStream = if (item.file != null && item.file.exists() && item.file.canRead()) {
                FileInputStream(item.file)
            } else {
                context.contentResolver.openInputStream(item.uri) ?: throw Exception("No se pudo leer el archivo de origen")
            }
            inStream.use { input ->
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
            val contentUri = if (item.file != null && item.file.exists()) {
                try {
                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", item.file)
                } catch (_: Exception) {
                    item.uri
                }
            } else {
                item.uri
            }

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
    private val cacheSize = maxMemory / 8

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
     * Extrae de forma eficiente la miniatura de un estado (fotograma de video o foto optimizada),
     * compatible con URIs de Storage Access Framework y FileProvider.
     */
    suspend fun loadThumbnail(context: Context, item: WhatsAppStatusItem): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = item.path
        val cached = getCachedThumbnail(cacheKey)
        if (cached != null) return@withContext cached

        var resultBitmap: Bitmap? = null

        if (item.isVideo) {
            val retriever = MediaMetadataRetriever()
            try {
                if (item.file != null && item.file.exists() && item.file.canRead()) {
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
                AppLog.w(TAG, "Fallo al extraer fotograma de video ${item.path}: ${e.message}")
            } finally {
                try { retriever.release() } catch (_: Throwable) {}
            }

            if (resultBitmap == null && item.file != null && item.file.exists()) {
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

                if (item.file != null && item.file.exists() && item.file.canRead()) {
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

                resultBitmap = if (item.file != null && item.file.exists() && item.file.canRead()) {
                    BitmapFactory.decodeFile(item.file.absolutePath, decodeOptions)
                } else {
                    context.contentResolver.openInputStream(item.uri)?.use {
                        BitmapFactory.decodeStream(it, null, decodeOptions)
                    }
                }
            } catch (e: Exception) {
                AppLog.w(TAG, "Fallo al decodificar imagen ${item.path}: ${e.message}")
            }
        }

        if (resultBitmap != null) {
            putCachedThumbnail(cacheKey, resultBitmap)
        }

        resultBitmap
    }
}
