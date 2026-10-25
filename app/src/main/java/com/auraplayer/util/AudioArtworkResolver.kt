package com.auraplayer.util

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Motor central de resolución y extracción profunda de carátulas para DaVE Player.
 * Soporta de forma unificada:
 * 1. Caché persistente local en disco (context.filesDir/covers)
 * 2. Android 10+ (API 29 a 35) ContentResolver.loadThumbnail para MediaStore Audio y Albums
 * 3. Extracción de tags ID3v2 APIC / FLAC METADATA_BLOCK_PICTURE / MP4 covr con MediaMetadataRetriever
 * 4. Detección de imágenes de carátula en la carpeta del álbum (cover.jpg, folder.jpg, front.jpg, etc.)
 * 5. Compatibilidad hacia atrás con URIs heredadas content://media/external/audio/albumart/*
 * 6. Generación de URIs seguras con FileProvider para MediaSession, Notificaciones y Lockscreen
 */
object AudioArtworkResolver {

    private const val TAG = "AudioArtworkResolver"

    private val FOLDER_ART_NAMES = listOf(
        "cover.jpg", "cover.png", "cover.jpeg",
        "folder.jpg", "folder.png", "folder.jpeg",
        "albumart.jpg", "albumart.png",
        "front.jpg", "front.png", "front.jpeg",
        "album.jpg", "album.png",
        "artwork.jpg", "artwork.png"
    )

    /**
     * Resuelve un Bitmap optimizado para la carátula solicitada, aplicando downsampling
     * para no saturar memoria RAM y garantizando 120 FPS fluidos.
     */
    fun resolveBitmap(
        context: Context,
        uri: Uri?,
        path: String? = null,
        targetSize: Int = 512
    ): Bitmap? {
        if (uri == null && path.isNullOrBlank()) return null

        // 1. Si es URI local de archivo (file://...)
        if (uri?.scheme == "file") {
            val filePath = uri.path ?: path
            if (!filePath.isNullOrBlank()) {
                val f = File(filePath)
                if (f.exists() && f.length() > 0) {
                    if (isImageExtension(filePath)) {
                        return decodeSampledBitmapFromFile(f, targetSize)
                    } else if (isAudioExtension(filePath)) {
                        return extractFromAudioFile(context, f, targetSize)
                    }
                }
            }
        }

        // 2. Si se proporcionó una ruta de archivo directa
        if (!path.isNullOrBlank()) {
            val f = File(path)
            if (f.exists() && f.length() > 0) {
                if (isAudioExtension(path)) {
                    val bmp = extractFromAudioFile(context, f, targetSize)
                    if (bmp != null) return bmp
                }
            }
        }

        // 3. Si es una URI de MediaStore
        if (uri != null && uri.scheme == "content") {
            val uriStr = uri.toString()

            // Caso 3A: content://media/external/audio/albumart/<albumId> (Heredado de Android 9 o anterior)
            if (uriStr.contains("audio/albumart")) {
                val albumId = uri.lastPathSegment?.toLongOrNull() ?: 0L
                if (albumId > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        val albumCollectionUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                            albumId
                        )
                        val thumb = context.contentResolver.loadThumbnail(
                            albumCollectionUri,
                            Size(targetSize, targetSize),
                            null
                        )
                        if (thumb != null) return thumb
                    } catch (_: Exception) {}
                }
                // Fallback tradicional para albumart
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        if (bmp != null) return bmp
                    }
                } catch (_: Exception) {}
            }

            // Caso 3B: content://media/external/audio/media/<id> (Pista de audio)
            if (uriStr.contains("audio/media")) {
                val songId = uri.lastPathSegment?.toLongOrNull() ?: 0L

                // A) Verificar si ya fue guardada en la caché local por su ID
                if (songId > 0) {
                    val cached = File(context.filesDir, "covers/$songId.jpg")
                    if (cached.exists() && cached.length() > 0) {
                        val bmp = decodeSampledBitmapFromFile(cached, targetSize)
                        if (bmp != null) return bmp
                    }
                }

                // B) En Android 10+ (API 29+), usar el thumbnail oficial generado por MediaStore
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        val thumb = context.contentResolver.loadThumbnail(
                            uri,
                            Size(targetSize, targetSize),
                            null
                        )
                        if (thumb != null) {
                            if (songId > 0) saveBitmapToCoversDir(context, songId, thumb)
                            return thumb
                        }
                    } catch (_: Exception) {}
                }

                // C) Resolver metadatos de la pista para buscar ID3 tags o folder art
                val songMeta = querySongDetails(context, uri)
                if (songMeta != null) {
                    if (songMeta.path.isNotBlank()) {
                        val f = File(songMeta.path)
                        if (f.exists() && f.canRead()) {
                            val bmp = extractFromAudioFile(context, f, targetSize)
                            if (bmp != null) {
                                if (songId > 0) saveBitmapToCoversDir(context, songId, bmp)
                                return bmp
                            }
                        }
                    }

                    // Intentar thumbnail del álbum asociado si existe
                    if (songMeta.albumId > 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        try {
                            val albumUri = ContentUris.withAppendedId(
                                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                                songMeta.albumId
                            )
                            val thumb = context.contentResolver.loadThumbnail(
                                albumUri,
                                Size(targetSize, targetSize),
                                null
                            )
                            if (thumb != null) {
                                if (songId > 0) saveBitmapToCoversDir(context, songId, thumb)
                                return thumb
                            }
                        } catch (_: Exception) {}
                    }
                }

                // D) Intento directo de extracción con MediaMetadataRetriever vía Content URI
                try {
                    val retriever = MediaMetadataRetriever()
                    try {
                        retriever.setDataSource(context, uri)
                        val pic = retriever.embeddedPicture
                        if (pic != null && pic.isNotEmpty()) {
                            val bmp = decodeSampledBitmapFromByteArray(pic, targetSize)
                            if (bmp != null) {
                                if (songId > 0) saveBitmapToCoversDir(context, songId, bmp)
                                return bmp
                            }
                        }
                    } finally {
                        try { retriever.release() } catch (_: Exception) {}
                    }
                } catch (_: Exception) {}
            }

            // Caso 3C: FileProvider u otro Content Provider
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.setTargetSize(targetSize, targetSize)
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                } else {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        return BitmapFactory.decodeStream(stream)
                    }
                }
            } catch (_: Exception) {}
        }

        return null
    }

    /**
     * Extrae la imagen incrustada de un archivo de audio o una imagen en su carpeta contenedora.
     */
    private fun extractFromAudioFile(context: Context, file: File, targetSize: Int): Bitmap? {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(file.absolutePath)
            val pic = retriever.embeddedPicture
            if (pic != null && pic.isNotEmpty()) {
                val bmp = decodeSampledBitmapFromByteArray(pic, targetSize)
                if (bmp != null) return bmp
            }
        } catch (_: Exception) {
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }

        // Buscar imagen de carátula en la misma carpeta del álbum
        try {
            val parentDir = file.parentFile
            if (parentDir != null && parentDir.exists() && parentDir.isDirectory) {
                for (artName in FOLDER_ART_NAMES) {
                    val candidate = File(parentDir, artName)
                    if (candidate.exists() && candidate.length() > 0) {
                        val bmp = decodeSampledBitmapFromFile(candidate, targetSize)
                        if (bmp != null) return bmp
                    }
                }

                // Si no se encuentra con nombres comunes, buscar cualquier .jpg o .png en la carpeta
                val images = parentDir.listFiles { _, name ->
                    val lower = name.lowercase()
                    (lower.endsWith(".jpg") || lower.endsWith(".png") || lower.endsWith(".jpeg")) && !lower.startsWith(".")
                }
                if (!images.isNullOrEmpty()) {
                    val firstImage = images.firstOrNull { it.length() > 1024L }
                    if (firstImage != null) {
                        val bmp = decodeSampledBitmapFromFile(firstImage, targetSize)
                        if (bmp != null) return bmp
                    }
                }
            }
        } catch (_: Exception) {}

        return null
    }

    /**
     * Extrae los bytes comprimidos (JPEG) para ser incrustados en MediaMetadata de MediaSession.
     */
    fun resolveCoverBytes(context: Context, uri: Uri?, path: String? = null): ByteArray? {
        // Si hay extracción directa de bytes sin decodificar
        if (!path.isNullOrBlank()) {
            val f = File(path)
            if (f.exists() && isAudioExtension(path)) {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(f.absolutePath)
                    val pic = retriever.embeddedPicture
                    if (pic != null && pic.isNotEmpty()) return pic
                } catch (_: Exception) {
                } finally {
                    try { retriever.release() } catch (_: Exception) {}
                }
            }
        }

        val bmp = resolveBitmap(context, uri, path, targetSize = 256) ?: return null
        return try {
            val stream = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 88, stream)
            stream.toByteArray()
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Convierte una URI local 'file://' a una URI 'content://' pública y segura de FileProvider,
     * requerida por Android 7+ para MediaSession, Notificaciones del Sistema y Bluetooth AVRCP.
     */
    fun getSafeArtworkUriForMediaSession(context: Context, uri: Uri?): Uri? {
        if (uri == null) return null
        if (uri.scheme == "file") {
            val path = uri.path
            if (!path.isNullOrBlank()) {
                val f = File(path)
                if (f.exists() && f.length() > 0) {
                    return try {
                        FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            f
                        )
                    } catch (_: Exception) {
                        uri
                    }
                }
            }
        }
        return uri
    }

    private data class SongDetails(val path: String, val albumId: Long)

    private fun querySongDetails(context: Context, uri: Uri): SongDetails? {
        return try {
            val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                    val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                    val path = if (dataCol >= 0) cursor.getString(dataCol) ?: "" else ""
                    val albumId = if (albumIdCol >= 0) cursor.getLong(albumIdCol) else 0L
                    SongDetails(path, albumId)
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun decodeSampledBitmapFromFile(file: File, targetSize: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
            options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (_: Exception) {
            null
        }
    }

    private fun decodeSampledBitmapFromByteArray(data: ByteArray, targetSize: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(data, 0, data.size, options)
            options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            BitmapFactory.decodeByteArray(data, 0, data.size, options)
        } catch (_: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun saveBitmapToCoversDir(context: Context, songId: Long, bitmap: Bitmap) {
        try {
            val coversDir = File(context.filesDir, "covers").apply { if (!exists()) mkdirs() }
            val targetFile = File(coversDir, "$songId.jpg")
            if (!targetFile.exists() || targetFile.length() == 0L) {
                FileOutputStream(targetFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
            }
        } catch (_: Exception) {}
    }

    private fun isImageExtension(path: String): Boolean {
        val lower = path.lowercase()
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") ||
                lower.endsWith(".png") || lower.endsWith(".webp")
    }

    private fun isAudioExtension(path: String): Boolean {
        val lower = path.lowercase()
        return lower.endsWith(".mp3") || lower.endsWith(".m4a") ||
                lower.endsWith(".flac") || lower.endsWith(".wav") ||
                lower.endsWith(".ogg") || lower.endsWith(".opus") ||
                lower.endsWith(".aac")
    }
}
