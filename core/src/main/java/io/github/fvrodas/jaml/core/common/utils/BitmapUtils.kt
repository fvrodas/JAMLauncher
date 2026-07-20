package io.github.fvrodas.jaml.core.common.utils

import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
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
    ): Bitmap = iconCache["$packageName${if (themedIcons) ".themed" else ""}"] ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && themedIcons) {
            Log.i(
                "Determine Icon Nature",
                "$packageName = Adaptive Icon: ${drawable is AdaptiveIconDrawable} | monochrome: ${drawable is AdaptiveIconDrawable && drawable.monochrome != null}"
            )
            drawable.toThemedIcon(backgroundColor, foregroundColor).toBitmap()
        } else {
            drawable.forceAdaptiveIconIfNeeded().toBitmap()
        }
    } else {
        drawable.toBitmap()
    }.also {
        iconCache.put("$packageName${if (themedIcons) ".themed" else ""}", it)
    }

    fun loadIconForPackage(packageName: String, shouldDisplayThemedIcon: Boolean = false): Bitmap? =
        iconCache["$packageName${if (shouldDisplayThemedIcon) ".themed" else ""}"]

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
        foregroundColor: Int = Color.BLACK,
    ): AdaptiveIconDrawable {
        if (this is AdaptiveIconDrawable && monochrome != null) {
            val tintedMonochrome = monochrome!!.mutate().apply {
                setTintMode(PorterDuff.Mode.SRC_ATOP)
                setTint(foregroundColor)
            }
            return AdaptiveIconDrawable(backgroundColor.toDrawable(), tintedMonochrome)
        }

        val drawable = this.mutate().applyDualTone(backgroundColor, foregroundColor)

        val foreground = InsetDrawable(drawable, INSET)
        return AdaptiveIconDrawable(backgroundColor.toDrawable(), foreground)
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

    fun Drawable.applyDualTone(
        primaryColor: Int,
        secondaryColor: Int,
        contrast: Float = 1.5f,
    ): Drawable {
        val mutatedDrawable = this.mutate()

        val r1 = Color.red(primaryColor) / 255f
        val g1 = Color.green(primaryColor) / 255f
        val b1 = Color.blue(primaryColor) / 255f

        val r2 = Color.red(secondaryColor) / 255f
        val g2 = Color.green(secondaryColor) / 255f
        val b2 = Color.blue(secondaryColor) / 255f

        val pivot = 128f * (1f - contrast)

        val colorMatrix = ColorMatrix(
            floatArrayOf(
                (r2 - r1) * contrast, 0f, 0f, 0f, r1 * 255f + (r2 - r1) * pivot,
                (g2 - g1) * contrast, 0f, 0f, 0f, g1 * 255f + (g2 - g1) * pivot,
                (b2 - b1) * contrast, 0f, 0f, 0f, b1 * 255f + (b2 - b1) * pivot,
                0f, 0f, 0f, 1f, 0f,
            )
        )

        mutatedDrawable.colorFilter = ColorMatrixColorFilter(colorMatrix)
        return mutatedDrawable
    }
}
