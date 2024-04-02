package io.github.fvrodas.jaml.ui.launcher.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.fvrodas.jaml.BuildConfig
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.core.domain.entities.AppShortcutInfo
import io.github.fvrodas.jaml.core.domain.usecases.GetApplicationsListUseCase
import io.github.fvrodas.jaml.core.domain.usecases.GetShortcutsListForApplicationUseCase
import io.github.fvrodas.jaml.core.domain.usecases.LaunchApplicationShortcutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppsViewModel(
    private val getApplicationsListUseCase: GetApplicationsListUseCase,
    private val getShortcutsListForApplicationUseCase: GetShortcutsListForApplicationUseCase,
    private val launchApplicationShortcutUseCase: LaunchApplicationShortcutUseCase,
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
        viewModelScope.launch {
            try {
                val result = getApplicationsListUseCase(null)
                    .filter { it.packageName != BuildConfig.APPLICATION_ID }
                applicationsListCache = result.toSet()
                _appsList.value = applicationsListCache

            } catch (e: Exception) {
                _appsList.value = emptySet()
            }
        }
    }

    fun filterApplicationsList(query: String = "") {
        viewModelScope.launch {
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
        viewModelScope.launch {
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
                val shortcuts = getShortcutsListForApplicationUseCase(packageName).toSet()
                _shortcutList.value = Pair(applicationInfo, shortcuts)
            } catch (e: Exception) {
                _shortcutList.value = null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun startShortcut(shortcut: AppShortcutInfo) {
        viewModelScope.launch {
            launchApplicationShortcutUseCase.invoke(shortcut)
        }
    }
}
