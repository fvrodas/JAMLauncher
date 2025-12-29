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
import io.github.fvrodas.jaml.domain.entity.LauncherItem
import io.github.fvrodas.jaml.domain.usecases.AddApplicationToGroupUseCase
import io.github.fvrodas.jaml.domain.usecases.GetGroupedApplicationsListUseCase
import io.github.fvrodas.jaml.domain.usecases.RemoveApplicationFromGroupUseCase
import io.github.fvrodas.jaml.ui.common.extensions.exclude
import io.github.fvrodas.jaml.ui.common.extensions.simplify
import io.github.fvrodas.jaml.ui.common.extensions.updateAppEntry
import io.github.fvrodas.jaml.ui.launcher.viewmodels.ApplicationSheetState.Companion.MAX_PINNED_APPS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getApplicationsListUseCase: GetApplicationsListUseCase,
    private val getShortcutsListForApplicationUseCase: GetShortcutsListForApplicationUseCase,
    private val launchApplicationShortcutUseCase: LaunchApplicationShortcutUseCase,
    private val getGroupedApplicationsListUseCase: GetGroupedApplicationsListUseCase,
    private val addApplicationToGroupUseCase: AddApplicationToGroupUseCase,
    private val removeApplicationFromGroupUseCase: RemoveApplicationFromGroupUseCase
) : ViewModel() {

    private var applicationsListCache: Set<PackageInfo> = emptySet()
    private val groupedApplications: ArrayList<PackageInfo> = arrayListOf()

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

                with(getGroupedApplicationsListUseCase.invoke(null)) {
                    groupedApplications.clear()
                    groupedApplications.addAll(this)
                }

                _applicationsState.value = ApplicationSheetState(
                    pinnedApplications = groupedApplications.toSet(),
                    applicationsList = applicationsListCache.exclude(groupedApplications)
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
                    pinnedApplications = if (query.isEmpty()) groupedApplications.toSet() else setOf(),
                    applicationsList = applicationsListCache.filter { c ->
                        if (query.isNotEmpty()) {
                            c.label.simplify().contains(
                                query,
                                true
                            )
                        } else {
                            groupedApplications.none { c.packageName == it.packageName } &&
                                    c.label.simplify().contains(
                                        query,
                                        true
                                    )
                        }
                    }.toSet()
                )
            } catch (_: Exception) {
                _applicationsState.value = ApplicationSheetState()
            }
        }
    }

    fun toggleAppPinning(packageInfo: PackageInfo, groupName: String = LauncherItem.Group.HOME_GROUP) {
        viewModelScope.launch {
            if (groupedApplications.find { it.packageName == packageInfo.packageName } == null) {
                if (groupedApplications.size >= MAX_PINNED_APPS) return@launch
                packageInfo.groupName = groupName
                groupedApplications.clear()
                groupedApplications.addAll(addApplicationToGroupUseCase.invoke(packageInfo))
            } else {
                groupedApplications.clear()
                groupedApplications.addAll(removeApplicationFromGroupUseCase.invoke(packageInfo))
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
                    groupedApplications.find { packageName == it.packageName }?.hasNotification =
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
                    getShortcutsListForApplicationUseCase(packageInfo.packageName).toSet()
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
    val canPinApps: Boolean get() = pinnedApplications.size < MAX_PINNED_APPS

    companion object {
        const val MAX_PINNED_APPS = 5
    }
}
