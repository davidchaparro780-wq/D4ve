package com.auraplayer.data.repository

import android.content.Context
import android.content.SharedPreferences

class FavoritesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("aura_favorites", Context.MODE_PRIVATE)
    private val cache: MutableSet<Long> = mutableSetOf()
    private var isCacheLoaded = false

    private fun ensureCacheLoaded() {
        if (!isCacheLoaded) {
            synchronized(cache) {
                if (!isCacheLoaded) {
                    try {
                        val ids = prefs.all.filter { it.key.startsWith("fav_") && it.value == true }
                            .mapNotNull { it.key.removePrefix("fav_").toLongOrNull() }
                        cache.addAll(ids)
                    } catch (_: Throwable) {}
                    isCacheLoaded = true
                }
            }
        }
    }

    fun isFavorite(songId: Long): Boolean {
        ensureCacheLoaded()
        return synchronized(cache) { cache.contains(songId) }
    }

    fun toggleFavorite(songId: Long): Boolean {
        ensureCacheLoaded()
        return synchronized(cache) {
            val current = cache.contains(songId)
            if (current) {
                cache.remove(songId)
                prefs.edit().remove("fav_$songId").apply()
            } else {
                cache.add(songId)
                prefs.edit().putBoolean("fav_$songId", true).apply()
            }
            !current
        }
    }

    fun getFavoriteIds(): Set<Long> {
        ensureCacheLoaded()
        return synchronized(cache) { cache.toSet() }
    }

    fun setFavorites(ids: Set<Long>) {
        ensureCacheLoaded()
        synchronized(cache) {
            cache.clear()
            cache.addAll(ids)
            val editor = prefs.edit()
            prefs.all.keys.filter { it.startsWith("fav_") }.forEach { editor.remove(it) }
            ids.forEach { editor.putBoolean("fav_$it", true) }
            editor.apply()
        }
    }
}
