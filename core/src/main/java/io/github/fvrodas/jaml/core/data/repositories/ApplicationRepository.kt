package io.github.fvrodas.jaml.core.data.repositories

import android.app.Application
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.annotation.RequiresApi
import io.github.fvrodas.jaml.core.common.utils.BitmapUtils
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.core.domain.entities.AppShortcutInfo
import io.github.fvrodas.jaml.core.domain.repositories.IApplicationsRepository


class ApplicationRepository(
    app: Application
) : IApplicationsRepository {

    private val _packageManager: PackageManager = app.packageManager
    private val _launcherApps =
        app.baseContext.applicationContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    override suspend fun getApplicationsList(): List<AppInfo> {
        val apps: ArrayList<AppInfo> = ArrayList()

        _launcherApps.getActivityList(null, Process.myUserHandle()).forEach {
            if (_packageManager.getApplicationInfo(
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

        return apps
    }

    override suspend fun getShortcutsListForApplication(packageName: String): List<AppShortcutInfo> {
        val shortcuts = ArrayList<AppShortcutInfo>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcuts.addAll(
                _launcherApps.getShortcuts(
                    LauncherApps.ShortcutQuery().apply {
                        setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
                        setPackage(packageName)
                    }, Process.myUserHandle()
                )!!.map {
                    AppShortcutInfo(
                        it.id,
                        it.`package`,
                        it.shortLabel.toString(),
                        BitmapUtils.loadShortcutIcon(_launcherApps, it)
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
        return shortcuts
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    override suspend fun launchShortcut(shortcut: AppShortcutInfo) {
        _launcherApps.startShortcut(
            shortcut.packageName,
            shortcut.id,
            null,
            null,
            Process.myUserHandle()
        )
    }
}
