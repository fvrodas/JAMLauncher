package io.github.fvrodas.jaml.ui.launcher.viewmodels

import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
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
import io.github.fvrodas.jaml.ui.common.settings.LauncherPreferences
import io.github.fvrodas.jaml.ui.launcher.viewmodels.ApplicationSheetState.Companion.MAX_PINNED_APPS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
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

    val applicationsState: StateFlow<ApplicationSheetState> = _applicationsState.onStart {
        retrieveApplicationsList()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ApplicationSheetState()
    )
    val shortcutsListState: StateFlow<Pair<PackageInfo, Set<PackageInfo.ShortcutInfo>>?> =
        _shortcutList

    fun retrieveApplicationsList() {
        viewModelScope.launch {
            try {
                val result = getApplicationsListUseCase(null)
                    .filter { it.packageName != BuildConfig.APPLICATION_ID }
                applicationsListCache = result.toSet()

                sharedPreferences.getString(LauncherPreferences.PINNED_APPS, null)?.let {
                    pinnedApplications.clear()
                    val packageNames: List<String> = Json.decodeFromString(it)
                    pinnedApplications.addAll(
                        packageNames.mapNotNull { p ->
                            applicationsListCache.firstOrNull { e -> p == e.packageName }
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
                        if (query.isNotEmpty()) {
                            c.label.simplify().contains(
                                query,
                                true
                            )
                        } else {
                            pinnedApplications.none { c.packageName == it.packageName } &&
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

    fun toggleAppPinning(packageInfo: PackageInfo) {
        viewModelScope.launch {
            if (pinnedApplications.find { it.packageName == packageInfo.packageName } == null) {
                if (pinnedApplications.size >= MAX_PINNED_APPS) return@launch

                pinnedApplications.add(packageInfo)
            } else {
                pinnedApplications.removeAll { it.packageName == packageInfo.packageName }
            }
            sharedPreferences.edit().apply {
                putString(
                    LauncherPreferences.PINNED_APPS,
                    Json.encodeToString(pinnedApplications.map { it.packageName })
                )
                commit()
            }
            retrieveApplicationsList()
        }
    }

    fun markNotification(packageName: String?, hasNotification: Boolean, notificationTitle: String?) {
        viewModelScope.launch {
            try {
                packageName?.let {
                    _applicationsState.value = _applicationsState.value.copy(
                        pinnedApplications = _applicationsState.value.pinnedApplications.updateAppEntry(
                            packageName,
                            hasNotification,
                            notificationTitle
                        ),
                        applicationsList = _applicationsState.value.applicationsList.updateAppEntry(
                            packageName,
                            hasNotification,
                            notificationTitle
                        )
                    )
                    applicationsListCache.find { packageName == it.packageName }?.let {
                        it.hasNotification = hasNotification
                        it.notificationTitle = notificationTitle
                    }
                    pinnedApplications.find { packageName == it.packageName }?.let {
                        it.hasNotification = hasNotification
                        it.notificationTitle = notificationTitle
                    }
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

        val Saver: Saver<ApplicationSheetState, Any> = listSaver(
            save = { listOf(it.pinnedApplications.toList(), it.applicationsList.toList()) },
            restore = {
                ApplicationSheetState(
                    pinnedApplications = it[0].toSet(),
                    applicationsList = it[1].toSet()
                )
            }
        )
    }
}
