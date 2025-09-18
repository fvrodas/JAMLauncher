package io.github.fvrodas.jaml.ui.launcher.viewmodels

import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.fvrodas.jaml.BuildConfig
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.core.domain.usecases.GetApplicationsListUseCase
import io.github.fvrodas.jaml.core.domain.usecases.GetShortcutsListForApplicationUseCase
import io.github.fvrodas.jaml.core.domain.usecases.LaunchApplicationShortcutUseCase
import io.github.fvrodas.jaml.ui.common.extensions.exclude
import io.github.fvrodas.jaml.ui.common.extensions.simplify
import io.github.fvrodas.jaml.ui.common.extensions.updateAppEntry
import io.github.fvrodas.jaml.ui.settings.viewmodels.LauncherSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class HomeViewModel(
    private val getApplicationsListUseCase: GetApplicationsListUseCase,
    private val getShortcutsListForApplicationUseCase: GetShortcutsListForApplicationUseCase,
    private val launchApplicationShortcutUseCase: LaunchApplicationShortcutUseCase,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private var applicationsListCache: Set<PackageInfo> = emptySet()
    private val pinnedApplications: ArrayList<PackageInfo> = arrayListOf()

    private var _applicationsState: MutableStateFlow<ApplicationSheetState> =
        MutableStateFlow(ApplicationSheetState())
    private var _shortcutList: MutableStateFlow<Pair<PackageInfo, Set<PackageInfo.ShortcutInfo>>?> =
        MutableStateFlow(null)

    val applicationsState: StateFlow<ApplicationSheetState> = _applicationsState
    val shortcutsListState: StateFlow<Pair<PackageInfo, Set<PackageInfo.ShortcutInfo>>?> =
        _shortcutList

    fun retrieveApplicationsList() {
        viewModelScope.launch {
            try {
                val result = getApplicationsListUseCase(null)
                    .filter { it.packageName != BuildConfig.APPLICATION_ID }
                applicationsListCache = result.toSet()

                sharedPreferences.getString(LauncherSettings.PINNED_APPS, null)?.let {
                    pinnedApplications.clear()
                    val packageNames: List<String> = Json.decodeFromString(it)
                    pinnedApplications.addAll(
                        packageNames.mapNotNull { p ->
                            applicationsListCache.firstOrNull { p == it.packageName }
                        }.toList()
                    )
                }

                _applicationsState.value = ApplicationSheetState(
                    pinnedApplications = pinnedApplications.toSet(),
                    applicationsList = applicationsListCache.exclude(pinnedApplications)
                )
            } catch (_: Exception) {
                _applicationsState.value = ApplicationSheetState()
            }
        }
    }

    fun filterApplicationsList(query: String = "") {
        viewModelScope.launch {
            try {
                _applicationsState.value = _applicationsState.value.copy(
                    pinnedApplications = if (query.isEmpty()) pinnedApplications.toSet() else setOf(),
                    applicationsList = applicationsListCache.filter { c ->
                        pinnedApplications.none { c.packageName == it.packageName } &&
                                c.label.simplify().contains(
                                    query,
                                    true
                                )
                    }.toSet()
                )
            } catch (_: Exception) {
                _applicationsState.value = ApplicationSheetState()
            }
        }
    }

    fun toggleAppPinning(packageInfo: PackageInfo) {
        viewModelScope.launch {
            if (pinnedApplications.find { it.packageName == packageInfo.packageName } == null) {
                if (pinnedApplications.size >= 5) return@launch

                pinnedApplications.add(packageInfo)
            } else {
                pinnedApplications.removeAll { it.packageName == packageInfo.packageName }
            }
            sharedPreferences.edit().apply {
                putString(
                    LauncherSettings.PINNED_APPS,
                    Json.encodeToString(pinnedApplications.map { it.packageName })
                )
                commit()
            }
            retrieveApplicationsList()
        }
    }

    fun markNotification(packageName: String?, hasNotification: Boolean) {
        viewModelScope.launch {
            try {
                packageName?.let {
                    _applicationsState.value = _applicationsState.value.copy(
                        pinnedApplications = _applicationsState.value.pinnedApplications.updateAppEntry(
                            packageName,
                            hasNotification
                        ),
                        applicationsList = _applicationsState.value.applicationsList.updateAppEntry(
                            packageName,
                            hasNotification
                        )
                    )
                    applicationsListCache.find { packageName == it.packageName }?.hasNotification =
                        hasNotification
                    pinnedApplications.find { packageName == it.packageName }?.hasNotification =
                        hasNotification
                }
            } catch (_: Exception) {
                _applicationsState.value = ApplicationSheetState()
            }
        }
    }

    fun retrieveShortcuts(packageInfo: PackageInfo) {
        viewModelScope.launch {
            try {
                val applicationInfo =
                    applicationsListCache.first { it.packageName == packageInfo.packageName }
                val shortcuts =
                    getShortcutsListForApplicationUseCase(packageInfo.packageName).take(5).toSet()
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

data class ApplicationSheetState(
    val pinnedApplications: Set<PackageInfo> = setOf(),
    val applicationsList: Set<PackageInfo> = setOf()
) {
    val canPinApps: Boolean get() = pinnedApplications.size < 5
}
