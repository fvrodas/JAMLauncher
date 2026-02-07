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
import io.github.fvrodas.jaml.ui.common.extensions.simplify
import io.github.fvrodas.jaml.ui.common.extensions.updateAppEntry
import io.github.fvrodas.jaml.ui.common.models.LauncherEntry
import io.github.fvrodas.jaml.ui.common.models.toLauncherEntry
import io.github.fvrodas.jaml.ui.common.settings.LauncherPreferences
import io.github.fvrodas.jaml.ui.launcher.viewmodels.ApplicationSheetState.Companion.MAX_PINNED_APPS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.SortedSet

class HomeViewModel(
    private val getApplicationsListUseCase: GetApplicationsListUseCase,
    private val getShortcutsListForApplicationUseCase: GetShortcutsListForApplicationUseCase,
    private val launchApplicationShortcutUseCase: LaunchApplicationShortcutUseCase,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private var applicationsListCache: Set<LauncherEntry> = emptySet()

    private var _applicationsState: MutableStateFlow<ApplicationSheetState> =
        MutableStateFlow(ApplicationSheetState())
    private var _shortcutList: MutableStateFlow<Pair<LauncherEntry, Set<PackageInfo.ShortcutInfo>>?> =
        MutableStateFlow(null)

    val applicationsState: StateFlow<ApplicationSheetState> = _applicationsState.onStart {
        retrieveApplicationsList()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ApplicationSheetState()
    )
    val shortcutsListState: StateFlow<Pair<LauncherEntry, Set<PackageInfo.ShortcutInfo>>?> =
        _shortcutList

    fun retrieveApplicationsList() {
        viewModelScope.launch {
            try {
                val result = getApplicationsListUseCase(null)
                    .filter { it.packageName != BuildConfig.APPLICATION_ID }
                applicationsListCache = result.map { it.toLauncherEntry() }.toSet()

                sharedPreferences.getString(LauncherPreferences.PINNED_APPS, null)?.let {
                    val packageNames: List<Pair<String, Long>> = Json.decodeFromString(it)

                    packageNames.sortedBy { e -> e.second }.forEach { entry ->
                        applicationsListCache.find { e -> e.packageInfo.packageName == entry.first }
                            ?.moveToHomeScreen(entry.second)
                    }
                }

                _applicationsState.value = ApplicationSheetState(
                    pinnedApplications = applicationsListCache.filter { it.movedToHome }
                        .sortedBy { it.order }.toSet(),
                    applicationsList = applicationsListCache.filter { !it.movedToHome }.toSet()
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
                    pinnedApplications = applicationsListCache.filter { it.movedToHome }
                        .sortedBy { it.order }.toSet(),
                    applicationsList = applicationsListCache.filter { c ->
                        if (query.isNotEmpty()) {
                            c.packageInfo.label.simplify().contains(
                                query,
                                true
                            )
                        } else {
                            applicationsListCache.filter { it.movedToHome }
                                .none { c.packageInfo.packageName == it.packageInfo.packageName } &&
                                    c.packageInfo.label.simplify().contains(
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

    fun toggleAppPinning(entry: LauncherEntry) {
        viewModelScope.launch {
            val targetApp =
                applicationsListCache.find { it.packageInfo.packageName == entry.packageInfo.packageName }

            if (targetApp?.movedToHome == false
                && applicationsListCache.count { it.movedToHome } >= MAX_PINNED_APPS
            ) return@launch

            targetApp?.let {
                if (it.movedToHome) it.moveToDrawer()
                else it.moveToHomeScreen()
            }

            val pinnedApplications =
                applicationsListCache.filter { it.movedToHome }
                    .map { it.packageInfo.packageName to it.order }

            sharedPreferences.edit().apply {
                putString(
                    LauncherPreferences.PINNED_APPS,
                    Json.encodeToString(pinnedApplications)
                )
                commit()
            }
            retrieveApplicationsList()
        }
    }

    fun markNotification(
        packageName: String?,
        message: String?
    ) {
        viewModelScope.launch {
            try {
                packageName?.let {
                    _applicationsState.value = _applicationsState.value.copy(
                        pinnedApplications = _applicationsState.value.pinnedApplications.updateAppEntry(
                            packageName,
                            message
                        ),
                        applicationsList = _applicationsState.value.applicationsList.updateAppEntry(
                            packageName,
                            message
                        )
                    )
                    applicationsListCache.find { packageName == it.packageInfo.packageName }?.let {
                        it.notificationTitle = message
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
                    applicationsListCache.first { it.packageInfo.packageName == packageInfo.packageName }
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
    val pinnedApplications: Set<LauncherEntry> = setOf(),
    val applicationsList: Set<LauncherEntry> = setOf()
) {
    val canPinApps: Boolean get() = pinnedApplications.size < MAX_PINNED_APPS

    companion object {
        const val MAX_PINNED_APPS = 5

        val Saver: Saver<ApplicationSheetState, Any> = listSaver(
            save = { listOf(it.pinnedApplications.toList(), it.applicationsList.toList()) },
            restore = {
                ApplicationSheetState(
                    pinnedApplications = it[0].sortedBy { e -> e.order }.toSet(),
                    applicationsList = it[1].toSet()
                )
            }
        )
    }
}
