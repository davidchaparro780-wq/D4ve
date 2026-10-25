package com.auraplayer

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.auraplayer.notification.UpdateNotificationHelper
import com.auraplayer.util.AudioArtworkFetcher
import com.auraplayer.worker.UpdateWorker

class DaVEApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        // Crear canal de notificación de actualizaciones
        UpdateNotificationHelper.createNotificationChannel(this)
        // Iniciar detector periódico en segundo plano
        UpdateWorker.enqueuePeriodicWork(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(AudioArtworkFetcher.Factory(this@DaVEApplication))
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .strongReferencesEnabled(true)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.05)
                    .build()
            }
            .crossfade(150)
            .respectCacheHeaders(false)
            .build()
    }
}
