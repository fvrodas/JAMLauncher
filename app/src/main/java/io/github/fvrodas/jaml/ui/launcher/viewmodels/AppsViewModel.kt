package io.github.fvrodas.jaml.ui.launcher.viewmodels

import android.os.Build
import android.os.Process
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import io.github.fvrodas.jaml.BuildConfig
import io.github.fvrodas.jaml.core.data.repositories.ShortcutsUtil
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.core.domain.entities.AppShortcutInfo
import io.github.fvrodas.jaml.core.domain.usecases.GetApplicationsListUseCase
import io.github.fvrodas.jaml.core.domain.usecases.GetShortcutsListForApplicationUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class AppsViewModel(
    val getApplicationsListUseCase: GetApplicationsListUseCase,
    private val getShortcutsListForApplicationUseCase: GetShortcutsListForApplicationUseCase,
    private val shortcutsUtil: ShortcutsUtil,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private var applicationsListCache: Set<AppInfo> = emptySet()
    private var _appsList: MutableStateFlow<Set<AppInfo>> = MutableStateFlow(applicationsListCache)
    private var _shortcutList: MutableStateFlow<Pair<AppInfo, Set<AppShortcutInfo>>?> = MutableStateFlow(null)

    val appsListState: StateFlow<Set<AppInfo>> = _appsList
    val shortcutsListState: StateFlow<Pair<AppInfo, Set<AppShortcutInfo>>?> = _shortcutList

    init {
        retrieveApplicationsList()
    }

    fun retrieveApplicationsList() {
        CoroutineScope(coroutineDispatcher).launch {
            try {
                val result = getApplicationsListUseCase(null).getOrThrow()
                    .filter { it.packageName != BuildConfig.APPLICATION_ID }
                applicationsListCache = result.toSet()
                _appsList.value = applicationsListCache

            } catch (e: Exception) {
                _appsList.value = emptySet()
            }
        }
    }

    fun filterApplicationsList(query: String = "") {
        CoroutineScope(coroutineDispatcher).launch {
            try {
                _appsList.value = applicationsListCache.filter {
                    it.label.contains(
                        query,
                        true
                    )
                }.toSet()
            } catch (e: Exception) {
                _appsList.value = emptySet()
            }
        }
    }

    fun markNotification(packageName: String?, hasNotification: Boolean) {
        CoroutineScope(coroutineDispatcher).launch {
            try {
                val index = applicationsListCache.indexOfFirst {
                    it.packageName.contains(packageName ?: "", ignoreCase = true)
                }
                applicationsListCache.elementAtOrNull(index)?.hasNotification = hasNotification
                _appsList.value = applicationsListCache
            } catch (e: Exception) {
                _appsList.value = emptySet()
            }
        }
    }

    fun retrieveShortcuts(packageName: String) {
        viewModelScope.launch {
            try {
                val applicationInfo = applicationsListCache.first { it.packageName == packageName }
                val shortcuts = getShortcutsListForApplicationUseCase(packageName).getOrThrow().toSet()
                _shortcutList.value = Pair(applicationInfo, shortcuts)
            } catch (e: Exception) {
                _shortcutList.value = null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun startShortcut(shortcut: AppShortcutInfo) {
        shortcutsUtil.launcherApps.startShortcut(
            shortcut.packageName,
            shortcut.id,
            null,
            null,
            Process.myUserHandle()
        )
    }
}
