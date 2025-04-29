package io.github.fvrodas.jaml.ui.launcher.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.fvrodas.jaml.BuildConfig
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.core.domain.usecases.GetApplicationsListUseCase
import io.github.fvrodas.jaml.core.domain.usecases.GetShortcutsListForApplicationUseCase
import io.github.fvrodas.jaml.core.domain.usecases.LaunchApplicationShortcutUseCase
import io.github.fvrodas.jaml.ui.common.extensions.simplify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getApplicationsListUseCase: GetApplicationsListUseCase,
    private val getShortcutsListForApplicationUseCase: GetShortcutsListForApplicationUseCase,
    private val launchApplicationShortcutUseCase: LaunchApplicationShortcutUseCase,
) : ViewModel() {

    private var applicationsListCache: Set<PackageInfo> = emptySet()
    private var _appsList: MutableStateFlow<Set<PackageInfo>> = MutableStateFlow(applicationsListCache)
    private var _shortcutList: MutableStateFlow<Pair<PackageInfo, Set<PackageInfo.ShortcutInfo>>?> =
        MutableStateFlow(null)

    val appsListState: StateFlow<Set<PackageInfo>> = _appsList
    val shortcutsListState: StateFlow<Pair<PackageInfo, Set<PackageInfo.ShortcutInfo>>?> = _shortcutList

    fun retrieveApplicationsList() {
        viewModelScope.launch {
            try {
                val result = getApplicationsListUseCase(null)
                    .filter { it.packageName != BuildConfig.APPLICATION_ID }
                applicationsListCache = result.toSet()
                _appsList.value = applicationsListCache

            } catch (_: Exception) {
                _appsList.value = emptySet()
            }
        }
    }

    fun filterApplicationsList(query: String = "") {
        viewModelScope.launch {
            try {
                _appsList.value = applicationsListCache.filter {
                    it.label.simplify().contains(
                        query,
                        true
                    )
                }.toSet()
            } catch (_: Exception) {
                _appsList.value = emptySet()
            }
        }
    }

    fun markNotification(packageName: String?, hasNotification: Boolean) {
        viewModelScope.launch {
            try {
                applicationsListCache.find { it.packageName == packageName }?.hasNotification =
                    hasNotification
                _appsList.value = applicationsListCache
            } catch (_: Exception) {
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
            } catch (_: Exception) {
                _shortcutList.value = null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun startShortcut(shortcut: PackageInfo.ShortcutInfo) {
        viewModelScope.launch {
            launchApplicationShortcutUseCase.invoke(shortcut)
        }
    }
}
