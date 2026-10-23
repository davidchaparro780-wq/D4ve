package com.auraplayer.audio

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import com.auraplayer.util.AppLog
import java.io.File

/**
 * Singleton cache manager for ExoPlayer streaming audio/video.
 * Caches played streams in internal storage with LRU eviction (~250MB limit)
 * to allow 0-buffer instant replays and prevent redundant data usage.
 */
@OptIn(UnstableApi::class)
object AuraMediaCache {
    private const val TAG = "AuraMediaCache"
    private const val MAX_CACHE_BYTES = 250L * 1024 * 1024 // 250 MB disk cache

    @Volatile
    private var instance: SimpleCache? = null

    @Synchronized
    fun getInstance(context: Context): SimpleCache {
        return instance ?: synchronized(this) {
            instance ?: run {
                val cacheDir = File(context.cacheDir, "dave_media_cache").apply {
                    if (!exists()) mkdirs()
                }
                val databaseProvider = StandaloneDatabaseProvider(context.applicationContext)
                val evictor = LeastRecentlyUsedCacheEvictor(MAX_CACHE_BYTES)
                SimpleCache(cacheDir, evictor, databaseProvider).also {
                    instance = it
                    AppLog.d(TAG, "AuraMediaCache inicializado con éxito (250MB LRU en ${cacheDir.absolutePath})")
                }
            }
        }
    }
}
