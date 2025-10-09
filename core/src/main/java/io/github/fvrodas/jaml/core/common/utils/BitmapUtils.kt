package io.github.fvrodas.jaml.core.common.utils

import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.util.LruCache
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable

object BitmapUtils {
    private val iconCache: LruCache<String, Bitmap> = LruCache(1024 * 1024 * 80)

    fun loadIcon(packageName: String, drawable: Drawable): Bitmap {
        if (iconCache[packageName] != null) {
            return iconCache[packageName]!!
        } else {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (drawable is AdaptiveIconDrawable) {
                    iconCache.put(packageName, drawable.toBitmap())
                    drawable.toBitmap()
                } else {
                    val scaled = InsetDrawable(drawable, 0.24f)
                    scaled.bounds = drawable.bounds
                    AdaptiveIconDrawable(Color.WHITE.toDrawable(), scaled).toBitmap()
                }
            } else {
                drawable.toBitmap().also {
                    iconCache.put(packageName, it)
                }
            }
        }
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
            e.printStackTrace()
            null
        }
    }
}
