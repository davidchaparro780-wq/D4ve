package com.auraplayer

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.auraplayer.notification.UpdateNotificationHelper
import com.auraplayer.worker.UpdateWorker

class DaVEApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // Crear canal de notificación de actualizaciones
        UpdateNotificationHelper.createNotificationChannel(this)
        // Iniciar detector periódico en segundo plano
        UpdateWorker.enqueuePeriodicWork(this)
    }

    /**
     * Optimized global Coil ImageLoader:
     * - Fast memory LRU cache (up to 25% heap)
     * - 100MB disk cache for album artwork
     * - Hardware bitmaps enabled for zero CPU decompression penalty
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .allowHardware(true)
            .crossfade(150)
            .build()
    }
}
