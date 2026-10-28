package com.auraplayer.data.repository

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaScannerConnection
import android.media.ThumbnailUtils
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.LruCache
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.auraplayer.util.AppLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class WhatsAppStatusItem(
    val file: File? = null,
    val uri: Uri,
    val isVideo: Boolean,
    val timestamp: Long,
    val size: Long,
    val isRecent24h: Boolean = (System.currentTimeMillis() - timestamp) in -600_000L..(24L * 60 * 60 * 1000L),
    // Freshness HONESTA basada en el mtime real del archivo (sin overrides por carpeta activa).
    // Un archivo en una carpeta congelada puede estar marcado "vigente" pero NO ser genuinamente fresco.
    val genuinelyFresh: Boolean = false,
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

    /**
     * Extrae la fecha real del nombre del archivo si tiene el formato estándar de WhatsApp
     * (por ejemplo: VID-20210315-WA0001.mp4 o IMG-20220810-WA0002.jpg).
     * Esto evita que archivos restaurados de copias de seguridad viejas aparezcan falsamente como recientes.
     */
    fun extractDateFromFileName(name: String): Long? {
        val regexClassic = Regex("(?:VID|IMG)[-_](\\d{4})(\\d{2})(\\d{2})", RegexOption.IGNORE_CASE)
        val matchClassic = regexClassic.find(name)
        if (matchClassic != null) {
            try {
                val (year, month, day) = matchClassic.destructured
                val y = year.toInt()
                if (y in 2015..2035) {
                    val cal = java.util.Calendar.getInstance()
                    cal.set(y, month.toInt() - 1, day.toInt(), 12, 0, 0)
                    return cal.timeInMillis
                }
            } catch (_: Exception) {}
        }
        val regexDash = Regex("(\\d{4})-(\\d{2})-(\\d{2})")
        val matchDash = regexDash.find(name)
        if (matchDash != null) {
            try {
                val (year, month, day) = matchDash.destructured
                val y = year.toInt()
                if (y in 2015..2035) {
                    val cal = java.util.Calendar.getInstance()
                    cal.set(y, month.toInt() - 1, day.toInt(), 12, 0, 0)
                    return cal.timeInMillis
                }
            } catch (_: Exception) {}
        }
        return null
    }

    fun getAllPotentialStatusDirs(context: Context): List<File> {
        val dirs = mutableListOf<File>()
        val externalStorage = Environment.getExternalStorageDirectory()

        // 1. WhatsApp estándar en Android 11 a 15 (Almacenamiento Interno)
        dirs.add(File(externalStorage, "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"))
        dirs.add(File(externalStorage, "Android/media/com.whatsapp/WhatsApp/Media/.statuses"))
        dirs.add(File(externalStorage, "Android/media/com.whatsapp/WhatsApp/media/.Statuses"))
        dirs.add(File(externalStorage, "Android/media/com.whatsapp/WhatsApp/media/.statuses"))

        // WhatsApp Business en Android 11 a 15
        dirs.add(File(externalStorage, "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses"))
        dirs.add(File(externalStorage, "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.statuses"))
        dirs.add(File(externalStorage, "Android/media/com.whatsapp.w4b/WhatsApp Business/media/.Statuses"))
        dirs.add(File(externalStorage, "Android/media/com.whatsapp.w4b/WhatsApp Business/media/.statuses"))

        // Clones, Dual App, Dual Messenger (Samsung, Xiaomi, etc.)
        dirs.add(File(externalStorage, "DualApp/WhatsApp/Media/.Statuses"))
        dirs.add(File(externalStorage, "DualApp/com.whatsapp/WhatsApp/Media/.Statuses"))
        dirs.add(File(externalStorage, "DualApp/WhatsApp Business/Media/.Statuses"))
        dirs.add(File(File("/storage/emulated/999"), "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"))
        dirs.add(File(File("/storage/emulated/10"), "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"))

        // Mods comunes
        dirs.add(File(externalStorage, "Android/media/com.gbwhatsapp/GBWhatsApp/Media/.Statuses"))
        dirs.add(File(externalStorage, "GBWhatsApp/Media/.Statuses"))

        // 2. Tarjetas MicroSD y volúmenes de almacenamiento secundarios
        try {
            val extDirs = context.getExternalFilesDirs(null) ?: emptyArray()
            for (appDir in extDirs) {
                if (appDir != null) {
                    val path = appDir.absolutePath
                    val androidIndex = path.indexOf("/Android/data/")
                    if (androidIndex > 0) {
                        val rootPath = path.substring(0, androidIndex)
                        val sdRoot = File(rootPath)
                        if (sdRoot != externalStorage) {
                            dirs.add(File(sdRoot, "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"))
                            dirs.add(File(sdRoot, "Android/media/com.whatsapp/WhatsApp/media/.Statuses"))
                            dirs.add(File(sdRoot, "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses"))
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // 3. Solo en Android 10 o inferior considerar las rutas obsoletas
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            dirs.add(File(externalStorage, "WhatsApp/Media/.Statuses"))
            dirs.add(File(externalStorage, "WhatsApp Business/Media/.Statuses"))
        }

        // 4. AUTODESCUBRIMIENTO: recorrer Android/media en todos los volúmenes buscando carpetas
        // .Statuses de CUALQUIER paquete/mod (com.whatsapp, w4b, gbwhatsapp, yowatsapp, clones...)
        // Cubre rutas que no existían cuando se escribió la lista fija de arriba.
        try {
            val roots = mutableListOf(externalStorage)
            context.getExternalFilesDirs(null)?.forEach { appDir ->
                val idx = appDir?.absolutePath?.indexOf("/Android/") ?: -1
                if (idx > 0) {
                    val volumeRoot = File(appDir.absolutePath.substring(0, idx))
                    if (!roots.any { it.absolutePath.equals(volumeRoot.absolutePath, ignoreCase = true) }) {
                        roots.add(volumeRoot)
                    }
                }
            }
            for (volumeRoot in roots) {
                val mediaRoot = File(volumeRoot, "Android/media")
                val pkgDirs = mediaRoot.listFiles() ?: continue
                for (pkgDir in pkgDirs) {
                    val appDirs = pkgDir.listFiles() ?: continue
                    for (appDir in appDirs) {
                        if (!appDir.isDirectory) continue
                        // Variante A: <appDir>/.Statuses
                        val direct = File(appDir, ".Statuses")
                        if (direct.isDirectory) dirs.add(direct)
                        // Variante B: <appDir>/<carpeta>/Media/.Statuses (WhatsApp y mods)
                        val subDirs = appDir.listFiles() ?: continue
                        for (sub in subDirs) {
                            if (!sub.isDirectory) continue
                            val statusDir = File(sub, "Media/.Statuses")
                            if (statusDir.isDirectory) dirs.add(statusDir)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Autodescubrimiento de carpetas .Statuses falló: ${e.message}")
        }

        return dirs.distinctBy { it.absolutePath }
    }

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

    fun clearSavedTreeUris(context: Context) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val set = prefs.getStringSet(KEY_SAF_TREE_URIS, emptySet()) ?: emptySet()
            for (uriStr in set) {
                try {
                    val uri = Uri.parse(uriStr)
                    context.contentResolver.releasePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                } catch (_: Exception) {}
            }
            prefs.edit().remove(KEY_SAF_TREE_URIS).apply()
        } catch (e: Exception) {
            AppLog.e(TAG, "Error limpiando URIs de SAF", e)
        }
    }

    fun getSavedTreeUris(context: Context): List<Uri> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val set = prefs.getStringSet(KEY_SAF_TREE_URIS, emptySet()) ?: emptySet()
        val validUris = mutableListOf<Uri>()
        val cleanedSet = mutableSetOf<String>()
        var modified = false

        for (uriStr in set) {
            try {
                val uri = Uri.parse(uriStr)
                val docId = try { DocumentsContract.getTreeDocumentId(uri) } catch (_: Exception) { uriStr }
                val decoded = Uri.decode(docId).lowercase()

                // En Android 11+, si la URI apunta a la carpeta vieja de raíz "primary:WhatsApp" sin "Android/media",
                // es un residuo obsoleto de 2021 que bloquea la detección moderna y contiene solo archivos viejos.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                    (decoded.startsWith("primary:whatsapp") || decoded.startsWith("primary:whatsapp business")) &&
                    !decoded.contains("android/media")
                ) {
                    AppLog.w(TAG, "Purgando SAF Tree URI obsoleto de 2021: $uriStr")
                    try {
                        context.contentResolver.releasePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    } catch (_: Exception) {}
                    modified = true
                    continue
                }

                validUris.add(uri)
                cleanedSet.add(uriStr)
            } catch (_: Exception) {
                modified = true
            }
        }

        if (modified) {
            prefs.edit().putStringSet(KEY_SAF_TREE_URIS, cleanedSet).apply()
        }

        return validUris
    }

    fun hasSafAccess(context: Context): Boolean {
        val uris = getSavedTreeUris(context)
        if (uris.isEmpty()) return false
        val persisted = context.contentResolver.persistedUriPermissions
        return persisted.any { it.isReadPermission && uris.any { u -> u == it.uri } }
    }

    /**
     * URI inicial para el selector SAF.
     * Retorna una Tree Document URI válida que cumple con DocumentsContract.isTreeUri()
     * para que DocumentsUI se abra directamente dentro de la carpeta Media en Android 11 a 15,
     * mostrando el botón "USAR ESTA CARPETA" habilitado.
     */
    fun getStatusesFolderInitialUri(isBusiness: Boolean = false): Uri {
        val pkg = if (isBusiness) "com.whatsapp.w4b" else "com.whatsapp"
        val sub = if (isBusiness) "WhatsApp%20Business" else "WhatsApp"
        val docId = "primary:Android/media/$pkg/$sub/Media"
        return DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", docId)
    }

    /**
     * URI de respaldo apuntando a Android/media para dispositivos donde el fabricante
     * restringe la selección de subcarpetas profundas.
     */
    fun getMediaFolderInitialUri(): Uri {
        return DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", "primary:Android/media")
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
    fun openWhatsApp(context: Context, preferBusiness: Boolean = false): Boolean {
        val pkgs = if (preferBusiness) {
            listOf("com.whatsapp.w4b", "com.whatsapp")
        } else {
            listOf("com.whatsapp", "com.whatsapp.w4b")
        }
        for (pkg in pkgs) {
            try {
                val pm = context.packageManager
                val intent = pm.getLaunchIntentForPackage(pkg) ?: Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    setPackage(pkg)
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return true
            } catch (_: Exception) {}
        }
        AppLog.w(TAG, "No se encontró instalada la app de WhatsApp ni WhatsApp Business")
        return false
    }

    /**
     * Consulta de forma ultra-directa (en 5 milisegundos) los archivos de estados en SAF
     * utilizando ContentResolver y DocumentContract proyecciones directas, eliminando las
     * decenas de llamadas IPC Binder que congelan la interfaz al usar DocumentFile.
     */
    private fun queryStatusesDirectFromTree(context: Context, treeUri: Uri): List<WhatsAppStatusItem> {
        val treeDocId = try {
            DocumentsContract.getTreeDocumentId(treeUri)
        } catch (_: Exception) {
            null
        } ?: return emptyList()

        val results = mutableListOf<WhatsAppStatusItem>()
        val seenSignatures = mutableSetOf<String>()

        // 1. Determinar posibles carpetas de estados según lo que haya seleccionado el usuario
        val candidateFolderDocIds = mutableListOf<String>()

        val lowerTreeDocId = treeDocId.lowercase()
        if (lowerTreeDocId.endsWith("/.statuses") || lowerTreeDocId.endsWith("/statuses") || lowerTreeDocId.endsWith(".statuses")) {
            candidateFolderDocIds.add(treeDocId)
        } else if (lowerTreeDocId.endsWith("/media") || lowerTreeDocId.endsWith("media")) {
            candidateFolderDocIds.add("$treeDocId/.Statuses")
            candidateFolderDocIds.add("$treeDocId/.statuses")
        } else if (lowerTreeDocId.endsWith("/whatsapp") || lowerTreeDocId.endsWith("/whatsapp business")) {
            candidateFolderDocIds.add("$treeDocId/Media/.Statuses")
            candidateFolderDocIds.add("$treeDocId/Media/.statuses")
        } else if (lowerTreeDocId.endsWith("com.whatsapp")) {
            candidateFolderDocIds.add("$treeDocId/WhatsApp/Media/.Statuses")
            candidateFolderDocIds.add("$treeDocId/WhatsApp/Media/.statuses")
        } else if (lowerTreeDocId.endsWith("com.whatsapp.w4b")) {
            candidateFolderDocIds.add("$treeDocId/WhatsApp Business/Media/.Statuses")
            candidateFolderDocIds.add("$treeDocId/WhatsApp Business/Media/.statuses")
        } else if (lowerTreeDocId.endsWith("android/media")) {
            candidateFolderDocIds.add("$treeDocId/com.whatsapp/WhatsApp/Media/.Statuses")
            candidateFolderDocIds.add("$treeDocId/com.whatsapp/WhatsApp/Media/.statuses")
            candidateFolderDocIds.add("$treeDocId/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses")
            candidateFolderDocIds.add("$treeDocId/com.whatsapp.w4b/WhatsApp Business/Media/.statuses")
        }

        // Siempre intentar también relative paths directos
        candidateFolderDocIds.add("$treeDocId/.Statuses")
        candidateFolderDocIds.add("$treeDocId/.statuses")

        for (candidateId in candidateFolderDocIds.distinct()) {
            val items = queryChildDocuments(context, treeUri, candidateId)
            for (item in items) {
                val sig = "${item.path}_${item.size}"
                if (seenSignatures.add(sig)) {
                    results.add(item)
                }
            }
            if (results.isNotEmpty()) {
                return results
            }
        }

        // 2. Si las rutas directas no arrojaron archivos, inspeccionar hijos de treeDocId
        try {
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocId)
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE
            )

            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)

                while (cursor.moveToNext()) {
                    val childId = if (idCol != -1) cursor.getString(idCol) else continue
                    val childName = if (nameCol != -1) cursor.getString(nameCol) ?: "" else ""
                    val mime = if (mimeCol != -1) cursor.getString(mimeCol) ?: "" else ""

                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                        if (childName.equals(".Statuses", ignoreCase = true) || childName.equals("statuses", ignoreCase = true)) {
                            val items = queryChildDocuments(context, treeUri, childId)
                            for (item in items) {
                                val sig = "${item.path}_${item.size}"
                                if (seenSignatures.add(sig)) results.add(item)
                            }
                        } else if (childName.equals("Media", ignoreCase = true)) {
                            val items = queryChildDocuments(context, treeUri, "$childId/.Statuses")
                            for (item in items) {
                                val sig = "${item.path}_${item.size}"
                                if (seenSignatures.add(sig)) results.add(item)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            AppLog.w(TAG, "Error escaneando hijos directos de $treeDocId: ${e.message}")
        }

        return results
    }

    private fun queryChildDocuments(context: Context, treeUri: Uri, folderDocId: String): List<WhatsAppStatusItem> {
        val list = mutableListOf<WhatsAppStatusItem>()
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_SIZE
        )

        val childrenUri = try {
            DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, folderDocId)
        } catch (_: Exception) {
            return emptyList()
        }

        val now = System.currentTimeMillis()

        try {
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)
                val modCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED)
                val sizeCol = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE)

                while (cursor.moveToNext()) {
                    val docId = if (idCol != -1) cursor.getString(idCol) else continue
                    val name = if (nameCol != -1) cursor.getString(nameCol) ?: "" else ""
                    val mime = if (mimeCol != -1) cursor.getString(mimeCol) ?: "" else ""
                    val size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                    val mod = if (modCol != -1) cursor.getLong(modCol) else now

                    if (name.startsWith(".nomedia") || size <= 0L) continue

                    val isVideo = mime.startsWith("video/") || name.endsWith(".mp4", ignoreCase = true) || name.endsWith(".mkv", ignoreCase = true) || name.endsWith(".3gp", ignoreCase = true)
                    val isImage = mime.startsWith("image/") || name.endsWith(".jpg", ignoreCase = true) || name.endsWith(".jpeg", ignoreCase = true) || name.endsWith(".png", ignoreCase = true) || name.endsWith(".webp", ignoreCase = true)

                    if (isVideo || isImage) {
                        val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        val nameDate = extractDateFromFileName(name)
                        val rawTime = if (mod in 1L..(now + 60_000L)) mod else 0L
                        val effectiveTime = when {
                            rawTime <= 0L -> nameDate ?: 0L
                            nameDate != null && nameDate < rawTime -> nameDate
                            else -> rawTime
                        }
                        // Todo archivo hallado dentro de una carpeta .Statuses de la ruta ACTIVA es vigente.
                        // En árboles legacy (WhatsApp/Media/.Statuses raíz) puede haber fósiles: filtro estricto.
                        val docIdActive = folderDocId.contains("android/media", ignoreCase = true)
                        val isRecent = docIdActive || ((now - effectiveTime) in -600_000L..(48L * 60 * 60 * 1000L))
                        val genuinelyFresh = (now - effectiveTime) in -600_000L..(24L * 60 * 60 * 1000L)
                        list.add(
                            WhatsAppStatusItem(
                                file = null,
                                uri = docUri,
                                isVideo = isVideo,
                                timestamp = effectiveTime,
                                size = size,
                                isRecent24h = isRecent,
                                genuinelyFresh = genuinelyFresh,
                                path = docUri.toString()
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Silencioso si la subcarpeta no existe
        }
        return list
    }

    /**
     * Fallback selectivo mediante DocumentFile.
     */
    private fun findStatusFilesFromTree(rootDoc: DocumentFile): List<DocumentFile> {
        val rootName = rootDoc.name ?: ""

        if (rootName.equals(".Statuses", ignoreCase = true) || rootName.equals("statuses", ignoreCase = true)) {
            return try { rootDoc.listFiles().filter { it.isFile && it.length() > 0 } } catch (_: Exception) { emptyList() }
        }

        val directChildren = try { rootDoc.listFiles() } catch (_: Exception) { emptyArray() }
        val directStatus = directChildren.firstOrNull { 
            it.isDirectory && (it.name.equals(".Statuses", ignoreCase = true) || it.name.equals("statuses", ignoreCase = true))
        }
        if (directStatus != null) {
            return try { directStatus.listFiles().filter { it.isFile && it.length() > 0 } } catch (_: Exception) { emptyList() }
        }

        val mediaDir = if (rootName.equals("Media", ignoreCase = true)) {
            rootDoc
        } else {
            directChildren.firstOrNull { it.isDirectory && it.name.equals("Media", ignoreCase = true) }
        }

        if (mediaDir != null) {
            val mediaChildren = try { mediaDir.listFiles() } catch (_: Exception) { emptyArray() }
            val statusDir = mediaChildren.firstOrNull { 
                it.isDirectory && (it.name.equals(".Statuses", ignoreCase = true) || it.name.equals("statuses", ignoreCase = true))
            }
            if (statusDir != null) {
                return try { statusDir.listFiles().filter { it.isFile && it.length() > 0 } } catch (_: Exception) { emptyList() }
            }
        }

        return directChildren.filter { it.isFile && it.length() > 0 }
    }

    /**
     * Obtiene la lista de todos los estados vistos de WhatsApp en el dispositivo.
     * Combina Storage Access Framework (SAF) para Android 11+ y acceso directo a archivos,
     * descartando archivos fósiles obsoletos del almacenamiento legado.
     * [reportSink] opcional: recibe líneas de diagnóstico del escaneo (para el panel del diálogo).
     */
    suspend fun getStatuses(context: Context, reportSink: ((String) -> Unit)? = null): List<WhatsAppStatusItem> = withContext(Dispatchers.IO) {
        val report = mutableListOf<String>()
        fun log(line: String) {
            report.add(line)
            reportSink?.invoke(line)
        }
        val resultList = withTimeoutOrNull(10000L) {
            val list = mutableListOf<WhatsAppStatusItem>()
            val seenSignatures = mutableSetOf<String>()
            val now = System.currentTimeMillis()

            log("═══ Diagnóstico de estados ═══")
            log("Android SDK: ${Build.VERSION.SDK_INT} · AllFiles: ${if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager() else "N/A"} · Árboles SAF: ${getSavedTreeUris(context).size}")
            log("Ahora: ${java.text.SimpleDateFormat("dd/MM HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(now))}")

            // 1. Motor de Archivos Físicos (File System) — Prioridad Absoluta (Método Snaptube)
            // Cuando la app tiene acceso al almacenamiento, la lectura directa de carpetas es instantánea (< 2ms)
            // y 100% precisa para leer Android/media/com.whatsapp/WhatsApp/Media/.Statuses
            val dirsToScan = getAllPotentialStatusDirs(context)

            for (dir in dirsToScan) {
                if (dir.exists() && dir.isDirectory) {
                    // La ruta activa (Android/media/...) la limpia WhatsApp cada 24h: si el archivo existe,
                    // es un estado vigente AUNQUE WhatsApp preserve el lastModified del archivo original
                    // (comportamiento conocido: contactos que re-publican fotos viejas nacen con mtime viejo).
                    val isActiveScoped = dir.absolutePath.replace('\\', '/').contains("/android/media/", ignoreCase = true)
                    val files = try { dir.listFiles() } catch (_: Exception) { null } ?: continue
                    val newestMod = files.maxOfOrNull { it.lastModified() } ?: 0L
                    val sdf = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
                    log("${if (isActiveScoped) "ACTIVA" else "LEGACY "} · ${files.size} archivos · más nuevo: ${if (newestMod > 0) sdf.format(java.util.Date(newestMod)) else "n/a"} · ${dir.absolutePath.takeLast(60)}")
                    for (file in files) {
                        if (file.isFile && !file.name.startsWith(".nomedia") && file.length() > 0) {
                            val name = file.name.lowercase()
                            val isVideo = name.endsWith(".mp4") || name.endsWith(".mkv") || name.endsWith(".3gp")
                            val isImage = name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp")
                            if (isVideo || isImage) {
                                val fileMod = file.lastModified()
                                val effectiveTime = if (fileMod in 1L..(now + 60_000L)) fileMod else now
                                // Fósiles: solo posibles en la ruta legado (raíz del almacenamiento), que WhatsApp ya no limpia
                                val isLegacyFossil = !isActiveScoped && (now - effectiveTime) > (48L * 60 * 60 * 1000L)
                                if (isLegacyFossil) continue
                                val isRecent = isActiveScoped || ((now - effectiveTime) in -600_000L..(48L * 60 * 60 * 1000L))
                                val genuinelyFresh = (now - effectiveTime) in -600_000L..(24L * 60 * 60 * 1000L)
                                // Timestamp HONESTO (mtime real): los fósiles se recortan al final del escaneo,
                                // no se les maquilla la fecha (maquillarlos los ponía arriba como si fueran de hoy)

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
                                            timestamp = effectiveTime,
                                            size = file.length(),
                                            isRecent24h = isRecent,
                                            genuinelyFresh = genuinelyFresh,
                                            path = file.absolutePath
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Motor SAF (Storage Access Framework) — Solo si el escaneo físico no encontró nada
            // (por ejemplo, si el usuario aún no otorgó acceso a todos los archivos y usa SAF como respaldo)
            if (list.isEmpty()) {
                val savedUris = getSavedTreeUris(context)
                for (treeUri in savedUris) {
                    try {
                        val directItems = queryStatusesDirectFromTree(context, treeUri)
                        for (item in directItems) {
                            val signature = "${item.path}_${item.size}"
                            if (seenSignatures.add(signature)) {
                                list.add(item)
                            }
                        }

                        if (directItems.isEmpty()) {
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
                                            val rawTime = doc.lastModified().takeIf { it in 1L..(now + 60_000L) } ?: now
                                            val isRecent = (now - rawTime) in -600_000L..(48L * 60 * 60 * 1000L)
                                            val size = doc.length()
                                            val signature = "${doc.name}_$size"
                                            if (seenSignatures.add(signature)) {
                                                list.add(
                                                    WhatsAppStatusItem(
                                                        file = null,
                                                        uri = doc.uri,
                                                        isVideo = isVideo,
                                                        timestamp = rawTime,
                                                        size = size,
                                                        isRecent24h = isRecent,
                                                        path = doc.uri.toString()
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        AppLog.w(TAG, "Error leyendo estados vía SAF: ${e.message}")
                    }
                }
            }

            // 2.5 MOTOR MEDIASTORE: estados frescos de las últimas 24h indexados por el sistema,
            // sin importar en qué ruta los esté escribiendo la versión/mod de WhatsApp del usuario.
            // Se activa cuando NINGÚN archivo tiene mtime genuinamente fresco (carpeta congelada).
            if (list.none { it.genuinelyFresh } && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                log("⚠️ Ninguna carpeta contiene archivos modificados en las últimas 24h (carpeta congelada). Activando respaldo MediaStore...")
                try {
                    val cutoffSec = ((now - (24L * 60 * 60 * 1000L)) / 1000L).toString()
                    val msProjection = arrayOf(
                        MediaStore.MediaColumns._ID,
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.DATA,
                        MediaStore.MediaColumns.DATE_MODIFIED,
                        MediaStore.MediaColumns.SIZE,
                        "media_type"
                    )
                    val msUri = MediaStore.Files.getContentUri("external")
                    context.contentResolver.query(
                        msUri,
                        msProjection,
                        "(media_type=? OR media_type=?) AND date_modified >= ?",
                        arrayOf("1", "3", cutoffSec),
                        "date_modified DESC"
                    )?.use { cursor ->
                        val idCol = cursor.getColumnIndex(MediaStore.MediaColumns._ID)
                        val nameCol = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                        val dataCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                        val modCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)
                        val sizeCol = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                        while (cursor.moveToNext()) {
                            val data = if (dataCol != -1) cursor.getString(dataCol) ?: "" else ""
                            val normalizedPath = data.replace('\\', '/')
                            // Pasada 1: rutas .Statuses (cualquier paquete). Pasada 2: cualquier carpeta de WhatsApp.
                            val isStatusesPath = normalizedPath.contains("/.statuses/", ignoreCase = true)
                            val isWhatsappPath = normalizedPath.contains("/whatsapp", ignoreCase = true)
                            // Excluir carpetas de chats/medios conocidas: no son estados y solo añaden ruido
                            val isNoiseFolder = listOf(
                                "/whatsapp images", "/whatsapp video", "/whatsapp audio",
                                "/whatsapp voice notes", "/voice notes", "/backups", "/stickers",
                                "/wallpaper", "/databases", "/whatsapp profile photos", "/media viewer"
                            ).any { normalizedPath.lowercase().contains(it) }
                            if (!isStatusesPath && (!isWhatsappPath || isNoiseFolder)) continue
                            val id = if (idCol != -1) cursor.getLong(idCol) else continue
                            val name = if (nameCol != -1) cursor.getString(nameCol) ?: "" else ""
                            val size = if (sizeCol != -1) cursor.getLong(sizeCol) else 0L
                            val modSec = if (modCol != -1) cursor.getLong(modCol) else 0L
                            val isVideo = name.endsWith(".mp4", ignoreCase = true) || name.endsWith(".mkv", ignoreCase = true) || name.endsWith(".3gp", ignoreCase = true)
                            if (!isVideo && !name.endsWith(".jpg", ignoreCase = true) && !name.endsWith(".jpeg", ignoreCase = true) && !name.endsWith(".png", ignoreCase = true) && !name.endsWith(".webp", ignoreCase = true)) continue
                            val contentUri = if (isVideo) {
                                ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                            } else {
                                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                            }
                            val signature = "${name}_$size"
                            if (seenSignatures.add(signature)) {
                                log("MediaStore · vigente (${if (isStatusesPath) ".statuses" else "whatsapp"}) hallado en: ${data.takeLast(60)}")
                                list.add(
                                    WhatsAppStatusItem(
                                        file = null,
                                        uri = contentUri,
                                        isVideo = isVideo,
                                        timestamp = modSec * 1000L,
                                        size = size,
                                        isRecent24h = true,
                                        genuinelyFresh = true,
                                        path = data
                                    )
                                )
                            }
                        }
                    }
                    if (list.none { it.genuinelyFresh }) {
                        log("⚠️ CONFIRMADO: ninguna ruta accesible contiene estados de las últimas 24h. Tu versión de WhatsApp está guardando los estados en almacenamiento privado (ningún visor de terceros puede leerlos). Opción: abre WhatsApp > Estados, mantén pulsado un estado > Compartir > DaVE Player.")
                    }
                } catch (e: Exception) {
                    AppLog.w(TAG, "Error en el respaldo MediaStore de estados: ${e.message}")
                }
            }

            // Recorte final: los estados son efímeros por naturaleza. Fuera todo lo que tenga
            // más de 7 días (los 641 fósiles de una carpeta congelada no aportan nada y ensucian)
            val antesDelRecorte = list.size
            list.removeAll { (now - it.timestamp) > (7L * 24 * 60 * 60 * 1000L) }
            if (antesDelRecorte > list.size) {
                log("🧹 Recortados por antigüedad (>7 días): ${antesDelRecorte - list.size}")
            }

            log("Total estados: ${list.size} · Vigentes: ${list.count { it.isRecent24h }} · Frescos de verdad: ${list.count { it.genuinelyFresh }}")

            // Ordenar: primero los estados recientes, y dentro de cada grupo por el más reciente
            list.sortWith(
                compareByDescending<WhatsAppStatusItem> { it.isRecent24h }
                    .thenByDescending { it.timestamp }
            )
            list
        }

        if (resultList == null) {
            AppLog.w(TAG, "getStatuses excedió el límite de tiempo de 10s")
        }
        resultList ?: emptyList()
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
                    var fdSet = false
                    try {
                        context.contentResolver.openFileDescriptor(item.uri, "r")?.use { pfd ->
                            retriever.setDataSource(pfd.fileDescriptor)
                            fdSet = true
                        }
                    } catch (_: Throwable) {}

                    if (!fdSet) {
                        try {
                            retriever.setDataSource(context, item.uri)
                        } catch (e: Exception) {
                            AppLog.w(TAG, "setDataSource directo falló para ${item.path}: ${e.message}")
                        }
                    }
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
