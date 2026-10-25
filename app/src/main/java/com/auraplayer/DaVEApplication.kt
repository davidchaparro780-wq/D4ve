package com.auraplayer

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.auraplayer.notification.UpdateNotificationHelper
import com.auraplayer.util.AudioArtworkFetcher
import com.auraplayer.worker.UpdateWorker
import java.io.File

class DaVEApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        try {
            // Crear canal de notificación de actualizaciones
            UpdateNotificationHelper.createNotificationChannel(this)
            // Iniciar detector periódico en segundo plano
            UpdateWorker.enqueuePeriodicWork(this)
        } catch (_: Throwable) {}
    }

    override fun newImageLoader(): ImageLoader {
        return try {
            val cacheDirectory = File(cacheDir, "image_cache").apply {
                if (!exists()) mkdirs()
            }
            ImageLoader.Builder(this)
                .components {
                    try {
                        add(AudioArtworkFetcher.Factory(this@DaVEApplication))
                    } catch (_: Throwable) {}
                }
                .memoryCache {
                    MemoryCache.Builder(this)
                        .maxSizePercent(0.20)
                        .strongReferencesEnabled(true)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(cacheDirectory)
                        .maxSizeBytes(60L * 1024L * 1024L) // 60 MB seguro sin llamadas a StatFs que puedan fallar
                        .build()
                }
                .crossfade(true)
                .respectCacheHeaders(false)
                .build()
        } catch (_: Throwable) {
            // Fallback seguro: ImageLoader básico de Coil sin riesgo de cierre
            ImageLoader.Builder(this)
                .crossfade(true)
                .build()
        }
    }
}
