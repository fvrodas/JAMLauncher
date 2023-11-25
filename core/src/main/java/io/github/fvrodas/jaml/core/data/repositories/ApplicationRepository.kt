package io.github.fvrodas.jaml.core.data.repositories

import android.app.Application
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
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

                shortcutsUtil.launcherApps.getActivityList(null, Process.myUserHandle()).forEach {
                    if (packageManager.getApplicationInfo(
                            it.applicationInfo.packageName,
                            PackageManager.MATCH_UNINSTALLED_PACKAGES
                        ).enabled
                    ) {
                        apps.add(
                            AppInfo(
                                it.applicationInfo.packageName,
                                it.label.toString(),
                                BitmapUtils.loadIcon(
                                    it.applicationInfo.packageName,
                                    it.getIcon(-1)
                                )
                            )
                        )
                    }
                }

                apps.sortWith { t1, t2 ->
                    t1.label.lowercase().compareTo(t2.label.lowercase())
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
                        shortcutsUtil.launcherApps.getShortcuts(
                            LauncherApps.ShortcutQuery().apply {
                                setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
                                setPackage(packageName)
                            }, Process.myUserHandle()
                        )!!.map {
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

    fun loadIcon(packageName: String, drawable: Drawable): Bitmap {
        if (iconCache[packageName] != null) {
            return iconCache[packageName]!!
        } else {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (drawable is AdaptiveIconDrawable) {
                    iconCache.put(packageName, drawable.toBitmap())
                    drawable.toBitmap()
                } else {
                    val scaled = InsetDrawable(drawable, 0.28f)
                    scaled.bounds = drawable.bounds
                    AdaptiveIconDrawable(ColorDrawable(Color.WHITE), scaled).toBitmap()
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