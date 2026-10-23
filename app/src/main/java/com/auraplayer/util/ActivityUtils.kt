package com.auraplayer.util

import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper

/**
 * Safely unwrap ContextWrapper layers to find the host Activity in Jetpack Compose.
 */
fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

inline fun <reified T : Activity> Context.findActivityAs(): T? {
    return findActivity() as? T
}

/**
 * Safely extracts text from the system clipboard without crashing on empty clip,
 * out-of-bounds, or Android 10+ background restrictions.
 */
fun Context.getSafeClipboardText(): String? {
    return try {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = clipboard?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            clip.getItemAt(0)?.text?.toString()?.trim()
        } else {
            null
        }
    } catch (_: Throwable) {
        null
    }
}
