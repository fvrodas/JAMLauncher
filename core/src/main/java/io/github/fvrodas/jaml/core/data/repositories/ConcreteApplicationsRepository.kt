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
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.core.domain.repositories.ApplicationsRepository


class ConcreteApplicationsRepository(
    application: Application
) : ApplicationsRepository {

    private val pm: PackageManager = application.packageManager
    private val launcherApps by lazy {
        application.baseContext.applicationContext
            .getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
    }

    override suspend fun getApplicationsList(): List<PackageInfo> {
        val apps: ArrayList<PackageInfo> = ArrayList()

        launcherApps.getActivityList(null, Process.myUserHandle()).forEach {
            if (pm.getApplicationInfo(
                    it.applicationInfo.packageName,
                    PackageManager.MATCH_UNINSTALLED_PACKAGES
                ).enabled
            ) {
                apps.add(
                    PackageInfo(
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

    override suspend fun getShortcutsListForApplication(packageName: String): List<PackageInfo.ShortcutInfo> {
        val shortcuts = ArrayList<PackageInfo.ShortcutInfo>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcuts.addAll(
                launcherApps.getShortcuts(
                    LauncherApps.ShortcutQuery().apply {
                        setQueryFlags(
                            LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST
                                    or LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC
                                    or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
                        )
                        setPackage(packageName)
                    }, Process.myUserHandle()
                )!!.map {
                    PackageInfo.ShortcutInfo(
                        it.id,
                        it.`package`,
                        it.shortLabel.toString(),
                        BitmapUtils.loadShortcutIcon(launcherApps, it)
                    )
                }.toList()
            )
        }
        shortcuts.add(
            PackageInfo.ShortcutInfo(
                "package:${packageName}",
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                "App Info",
                null
            )
        )
        return shortcuts
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    override suspend fun launchShortcut(shortcut: PackageInfo.ShortcutInfo) {
        launcherApps.startShortcut(
            shortcut.packageName,
            shortcut.id,
            null,
            null,
            Process.myUserHandle()
        )
    }
}
