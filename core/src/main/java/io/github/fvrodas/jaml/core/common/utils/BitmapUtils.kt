package io.github.fvrodas.jaml.core.common.utils

import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LightingColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.util.Log
import android.util.LruCache
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable

object BitmapUtils {
    private const val INSET = 0.14f

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8
    private val iconCache: LruCache<String, Bitmap> = LruCache(cacheSize)

    fun loadIcon(
        packageName: String,
        drawable: Drawable,
        themedIcons: Boolean = false,
        backgroundColor: Int = Color.WHITE,
        foregroundColor: Int = Color.BLACK,
    ): Bitmap = iconCache[packageName] ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && themedIcons) {
            drawable.toThemedIcon(backgroundColor, foregroundColor).toBitmap()
        } else {
            drawable.forceAdaptiveIconIfNeeded().toBitmap()
        }
    } else {
        drawable.toBitmap()
    }.also {
        iconCache.put(packageName, it)
    }

    fun loadIconForPackage(packageName: String): Bitmap = iconCache[packageName]

    fun clearCache() {
        iconCache.evictAll()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun Drawable.forceAdaptiveIconIfNeeded(): AdaptiveIconDrawable {
        if (this is AdaptiveIconDrawable) {
            return this
        } else {
            val scaled = InsetDrawable(this, INSET)
            scaled.bounds = this.bounds
            return AdaptiveIconDrawable(Color.WHITE.toDrawable(), scaled)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun Drawable.toThemedIcon(
        backgroundColor: Int = Color.WHITE,
        foregroundColor: Int = Color.BLACK
    ): AdaptiveIconDrawable {
        val (iconMask, needsInset) = if (this is AdaptiveIconDrawable) {
            (this.monochrome ?: this.foreground) to false
        } else {
            this to true
        }

        val themedForeground = if (needsInset) {
            InsetDrawable(iconMask.mutate(), INSET)
        } else {
            iconMask.mutate()
        }.apply {
            setTintMode(PorterDuff.Mode.SRC_IN)
            setTint(foregroundColor)
        }

        return AdaptiveIconDrawable(backgroundColor.toDrawable(), themedForeground)
    }

    @Deprecated("Use Drawable.toThemedIcon instead", ReplaceWith("toThemedIcon(backgroundColor, foregroundColor)"))
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun AdaptiveIconDrawable.forceThemedIcon(
        backgroundColor: Int = Color.WHITE,
        foregroundColor: Int = Color.BLACK,
        tintMode: PorterDuff.Mode = PorterDuff.Mode.SRC_ATOP
    ): AdaptiveIconDrawable {
        return this.toThemedIcon(backgroundColor, foregroundColor)
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun loadShortcutIcon(
        launcherApps: LauncherApps, shortcutInfo: ShortcutInfo, densityDpi: Int = -1
    ): Bitmap? {
        return try {
            iconCache[shortcutInfo.`package` + shortcutInfo.id]
                ?: launcherApps.getShortcutIconDrawable(shortcutInfo, densityDpi)?.toBitmap().also {
                    iconCache.put(shortcutInfo.`package` + shortcutInfo.id, it)
                }
        } catch (e: SecurityException) {
            Log.e(this::class.java.name, e.message ?: e.toString())
            null
        }
    }
}
