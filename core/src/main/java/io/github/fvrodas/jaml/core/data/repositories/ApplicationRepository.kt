package io.github.fvrodas.jaml.core.data.repositories

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.LruCache
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.core.domain.entities.AppShortcutInfo
import io.github.fvrodas.jaml.core.domain.repositories.IApplicationsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.ArrayList

class ApplicationRepository(
    app: Application,
    private val shortcutsUtil: ShortcutsUtil,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : IApplicationsRepository {

    private val packageManager: PackageManager = app.packageManager

    override suspend fun getApplicationsList(): Result<List<AppInfo>> {
        return withContext(coroutineDispatcher) {
            try {
                val apps: ArrayList<AppInfo> = ArrayList()
                packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }, 0).apply {
                    this.iterator().apply {
                        if (hasNext()) {
                            do {
                                val item = next()
                                apps.add(
                                    AppInfo(
                                        item.activityInfo.packageName,
                                        item.loadLabel(packageManager).toString(),
                                        BitmapUtils.loadIcon(packageManager, item)
                                    )
                                )
                            } while (hasNext())
                        }
                        apps.sortWith { t1, t2 ->
                            t1.label.lowercase()
                                .compareTo(t2.label.lowercase())
                        }
                    }
                }
                return@withContext Result.success(apps)
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    }

    override suspend fun getShortcutsListForApplication(packageName: String): Result<List<AppShortcutInfo>> {
        return withContext(coroutineDispatcher) {
            try {
                val shortcuts = ArrayList<AppShortcutInfo>()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    shortcuts.addAll(
                        shortcutsUtil.launcherApps.getShortcuts(LauncherApps.ShortcutQuery().apply {
                            setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
                            setPackage(packageName)
                        }, Process.myUserHandle())!!.map {
                            AppShortcutInfo(
                                it.id,
                                it.`package`,
                                it.shortLabel.toString(),
                                BitmapUtils.loadShortcutIcon(shortcutsUtil.launcherApps, it)
                            )
                        }.toList()
                    )
                }
                shortcuts.add(
                    AppShortcutInfo(
                        "package:${packageName}",
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        "App Info",
                        null
                    )
                )
                return@withContext Result.success(shortcuts)
            } catch (e: Exception) {
                return@withContext Result.failure(e)
            }
        }
    }
}

object BitmapUtils {
    private val iconCache: LruCache<String, Bitmap> = LruCache(1024 * 1024 * 80)

    fun loadIcon(
        packageManager: PackageManager,
        item: ResolveInfo
    ): Bitmap {
        if (iconCache[item.activityInfo.packageName] != null) {
            return iconCache[item.activityInfo.packageName]!!
        } else {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val drawable = packageManager.getApplicationIcon(item.activityInfo.packageName)
                if (drawable is AdaptiveIconDrawable) {
                    iconCache.put(item.activityInfo.packageName, drawable.toBitmap())
                    drawable.toBitmap()
                } else {
                    val scaled = InsetDrawable(drawable, 0.28f)
                    scaled.bounds = drawable.bounds
                    AdaptiveIconDrawable(ColorDrawable(Color.WHITE), scaled).toBitmap()
                }
            } else {
                item.activityInfo.loadIcon(packageManager).toBitmap().also {
                    iconCache.put(item.activityInfo.packageName, it)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun loadShortcutIcon(
        launcherApps: LauncherApps,
        shortcutInfo: ShortcutInfo,
        densityDpi: Int = -1
    ): Bitmap? {
        return try {
            if (iconCache[shortcutInfo.`package` + shortcutInfo.id] != null) {
                iconCache[shortcutInfo.`package` + shortcutInfo.id]
            } else {
                launcherApps.getShortcutIconDrawable(shortcutInfo, densityDpi)?.toBitmap().also {
                    iconCache.put(shortcutInfo.`package` + shortcutInfo.id, it)
                }
            }
        } catch (e: SecurityException) {
            null
        }
    }
}

class ShortcutsUtil(app: Application) {
    val launcherApps =
        app.baseContext.applicationContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
}