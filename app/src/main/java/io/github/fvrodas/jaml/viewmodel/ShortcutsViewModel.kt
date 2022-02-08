package io.github.fvrodas.jaml.viewmodel

import android.app.Application
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import io.github.fvrodas.jaml.model.AppInfo
import io.github.fvrodas.jaml.model.AppShortcutInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.N_MR1)
class ShortcutsViewModel(
    application: Application,
    packageName: String,
    private val densityDpi: Int
) : AndroidViewModel(application) {

    private var launcherApps: LauncherApps = application.baseContext.applicationContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

    val shortcutsList: MutableLiveData<ArrayList<AppShortcutInfo>> = MutableLiveData()

    init {
        retrieveShortcuts(packageName)
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun retrieveShortcuts(packageName: String) {
        GlobalScope.launch {
            val shortcutQuery = LauncherApps.ShortcutQuery()
            shortcutQuery.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
            shortcutQuery.setPackage(packageName)
            try {
                val shortcuts = ArrayList(launcherApps.getShortcuts(shortcutQuery, Process.myUserHandle())!!.map {
                    AppShortcutInfo(
                        it.id,
                        it.`package`,
                        it.shortLabel.toString(),
                        loadShortcutIcon(it)?.toBitmap()
                    )
                }.toList())
                shortcuts.add(AppShortcutInfo(
                        "app_info",
                        "none",
                        "Application details",
                        null
                ))
                shortcutsList.postValue(shortcuts)
            } catch (e: SecurityException) {
                Collections.emptyList<AppInfo>()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun loadShortcutIcon(shortcutInfo: ShortcutInfo): Drawable? {
        return try {
            val drawable = launcherApps.getShortcutIconDrawable(shortcutInfo, densityDpi)
            drawable
        } catch (e: SecurityException) {
            null
        }
    }

    fun startShortcut(shortcut: AppShortcutInfo) {
        launcherApps.startShortcut(shortcut.packageName, shortcut.id, null, null, Process.myUserHandle())
    }

}
