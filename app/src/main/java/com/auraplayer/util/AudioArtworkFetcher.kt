package com.auraplayer.util

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Fetcher personalizado para Coil 2.x que intercepta solicitudes de imágenes
// asociadas a archivos y URIs de audio en Android.
//
// Intercepta:
// - content://media/external/audio/media/
// - content://media/external/audio/albumart/
// - content://media/external/audio/albums/
// - dave-cover://
// - file:// con extensiones .mp3, .m4a, .flac, .ogg, etc.
//
// Utiliza AudioArtworkResolver para extraer la carátula desde la caché local,
// MediaStore loadThumbnail (Android 10+), tags ID3 APIC incrustados o carátulas
// de carpeta (cover.jpg/folder.jpg), y retorna un DrawableResult directo que Coil
// almacena en su MemoryCache para rendering inmediato a 120 FPS.
class AudioArtworkFetcher(
    private val data: Uri,
    private val options: Options,
    private val context: Context
) : Fetcher {

    override suspend fun fetch(): FetchResult? = withContext(Dispatchers.IO) {
        val targetSize = when (val w = options.size.width) {
            is coil.size.Dimension.Pixels -> w.px
            else -> 512
        }

        val bitmap = AudioArtworkResolver.resolveBitmap(
            context = context,
            uri = data,
            targetSize = targetSize
        ) ?: return@withContext null

        DrawableResult(
            drawable = BitmapDrawable(context.resources, bitmap),
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }

    class Factory(private val context: Context) : Fetcher.Factory<Uri> {
        override fun create(data: Uri, options: Options, imageLoader: ImageLoader): Fetcher? {
            val str = data.toString()
            val scheme = data.scheme

            val isAudioUri = str.contains("audio/media") ||
                    str.contains("audio/albumart") ||
                    str.contains("audio/albums") ||
                    str.startsWith("dave-cover://") ||
                    (scheme == "file" && isAudioFile(str))

            if (!isAudioUri) return null
            return AudioArtworkFetcher(data, options, context)
        }

        private fun isAudioFile(path: String): Boolean {
            val lower = path.lowercase()
            return lower.endsWith(".mp3") || lower.endsWith(".m4a") ||
                    lower.endsWith(".flac") || lower.endsWith(".wav") ||
                    lower.endsWith(".ogg") || lower.endsWith(".opus") ||
                    lower.endsWith(".aac")
        }
    }
}
