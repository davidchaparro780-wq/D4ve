package com.auraplayer.data.repository

import android.content.Context
import android.content.SharedPreferences

class FavoritesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("aura_favorites", Context.MODE_PRIVATE)

    private val cache: MutableSet<Long> = try {
        prefs.all.filter { it.key.startsWith("fav_") && it.value == true }
            .mapNotNull { it.key.removePrefix("fav_").toLongOrNull() }
            .toMutableSet()
    } catch (_: Exception) {
        mutableSetOf()
    }

    fun isFavorite(songId: Long): Boolean = synchronized(cache) {
        cache.contains(songId)
    }

    fun toggleFavorite(songId: Long): Boolean = synchronized(cache) {
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

    fun getFavoriteIds(): Set<Long> = synchronized(cache) {
        cache.toSet()
    }

    fun setFavorites(ids: Set<Long>) = synchronized(cache) {
        cache.clear()
        cache.addAll(ids)
        val editor = prefs.edit()
        prefs.all.keys.filter { it.startsWith("fav_") }.forEach { editor.remove(it) }
        ids.forEach { editor.putBoolean("fav_$it", true) }
        editor.apply()
    }
}
