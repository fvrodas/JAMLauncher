package io.github.fvrodas.jaml.ui.launcher.viewmodels

import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.fvrodas.jaml.AppConfig
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.core.domain.usecases.GetApplicationsListUseCase
import io.github.fvrodas.jaml.core.domain.usecases.GetShortcutsListForApplicationUseCase
import io.github.fvrodas.jaml.core.domain.usecases.LaunchApplicationShortcutUseCase
import io.github.fvrodas.jaml.ui.common.extensions.replaceEntry
import io.github.fvrodas.jaml.ui.common.extensions.simplify
import io.github.fvrodas.jaml.ui.common.extensions.updateAppEntry
import io.github.fvrodas.jaml.ui.common.models.AppGroup
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

class HomeViewModel(
    private val getApplicationsListUseCase: GetApplicationsListUseCase,
    private val getShortcutsListForApplicationUseCase: GetShortcutsListForApplicationUseCase,
    private val launchApplicationShortcutUseCase: LaunchApplicationShortcutUseCase,
    private val appConfig: AppConfig,
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

    // ── Prefs helpers ────────────────────────────────────────────────────────

    private fun loadGroupsFromPrefs(): Pair<List<String>, Map<String, List<String>>> {
        val groupNames: List<String> = sharedPreferences
            .getString(LauncherPreferences.GROUPS_LIST, null)
            ?.let { runCatching { Json.decodeFromString<List<String>>(it) }.getOrNull() }
            ?: emptyList()
        val groupedApps: Map<String, List<String>> = sharedPreferences
            .getString(LauncherPreferences.GROUPED_APPS, null)
            ?.let { runCatching { Json.decodeFromString<Map<String, List<String>>>(it) }.getOrNull() }
            ?: emptyMap()
        return groupNames to groupedApps
    }

    private fun saveGroupsToPrefs(groupNames: List<String>, groupedApps: Map<String, List<String>>) {
        sharedPreferences.edit().apply {
            putString(LauncherPreferences.GROUPS_LIST, Json.encodeToString(groupNames))
            putString(LauncherPreferences.GROUPED_APPS, Json.encodeToString(groupedApps))
            commit()
        }
    }

    private fun savePinnedApps() {
        val pinned = applicationsListCache
            .filter { it.movedToHome }
            .map { it.packageInfo.packageName to it.order }
        sharedPreferences.edit().apply {
            putString(LauncherPreferences.PINNED_APPS, Json.encodeToString(pinned))
            commit()
        }
    }

    private fun buildState(groupNames: List<String>): ApplicationSheetState {
        val groups = groupNames.map { AppGroup(it) }
        val groupedApplications = groupNames.associateWith { gName ->
            applicationsListCache.filter { it.group == gName }
        }
        return ApplicationSheetState(
            pinnedApplications = applicationsListCache
                .filter { it.movedToHome }
                .sortedBy { it.order }
                .toSet(),
            applicationsList = applicationsListCache
                .filter { it.group == null }
                .toSet(),
            groups = groups,
            groupedApplications = groupedApplications,
        )
    }

    // ── Load ─────────────────────────────────────────────────────────────────

    fun retrieveApplicationsList() {
        viewModelScope.launch {
            try {
                val result = getApplicationsListUseCase(null)
                    .filter { it.packageName != appConfig.packageName }
                applicationsListCache = result.map { it.toLauncherEntry() }.toSet()

                sharedPreferences.getString(LauncherPreferences.PINNED_APPS, null)?.let {
                    val packageNames: List<Pair<String, Long>> = Json.decodeFromString(it)
                    packageNames.sortedBy { e -> e.second }.forEach { entry ->
                        applicationsListCache = applicationsListCache.map { e ->
                            if (e.packageInfo.packageName == entry.first) e.moveToHomeScreen(entry.second) else e
                        }.toSet()
                    }
                }

                val (groupNames, groupedApps) = loadGroupsFromPrefs()
                groupedApps.forEach { (groupName, packageNames) ->
                    packageNames.forEach { packageName ->
                        applicationsListCache = applicationsListCache.map { e ->
                            if (e.packageInfo.packageName == packageName) e.moveToGroup(groupName) else e
                        }.toSet()
                    }
                }

                _applicationsState.value = buildState(groupNames)
            } catch (_: Exception) {
                _applicationsState.value = ApplicationSheetState()
            }
        }
    }

    fun filterApplicationsList(query: String = "") {
        viewModelScope.launch {
            try {
                val (groupNames, _) = loadGroupsFromPrefs()
                val filtered = if (query.isNotEmpty()) {
                    applicationsListCache.filter { c ->
                        c.packageInfo.label.simplify().contains(query, true)
                    }
                } else {
                    applicationsListCache.filter { it.group == null }
                }
                _applicationsState.value = _applicationsState.value.copy(
                    pinnedApplications = applicationsListCache
                        .filter { it.movedToHome }
                        .sortedBy { it.order }
                        .toSet(),
                    applicationsList = filtered.toSet(),
                    groups = groupNames.map { AppGroup(it) },
                    groupedApplications = groupNames.associateWith { gName ->
                        applicationsListCache.filter { it.group == gName }
                    },
                )
            } catch (_: Exception) {
                _applicationsState.value = ApplicationSheetState()
            }
        }
    }

    // ── Pinning ──────────────────────────────────────────────────────────────

    fun toggleAppPinning(entry: LauncherEntry) {
        viewModelScope.launch {
            val targetApp =
                applicationsListCache.find { it.packageInfo.packageName == entry.packageInfo.packageName }

            if (targetApp?.movedToHome == false
                && applicationsListCache.count { it.movedToHome } >= MAX_PINNED_APPS
            ) return@launch

            val isPinning = targetApp?.movedToHome == false

            targetApp?.let {
                val updated = if (it.movedToHome) it.moveToDrawer() else it.moveToHomeScreen()
                applicationsListCache = applicationsListCache.replaceEntry(updated)
            }

            savePinnedApps()

            val (groupNames, groupedApps) = loadGroupsFromPrefs()
            if (isPinning) {
                val packageName = entry.packageInfo.packageName
                val updatedApps = groupedApps.mapValues { (_, v) -> v.filter { it != packageName } }
                saveGroupsToPrefs(groupNames, updatedApps)
            }

            _applicationsState.value = buildState(groupNames)
        }
    }

    // ── Groups CRUD ──────────────────────────────────────────────────────────

    fun createGroup(name: String) {
        viewModelScope.launch {
            val (groupNames, groupedApps) = loadGroupsFromPrefs()
            if (name.isBlank() || name in groupNames) return@launch
            val updatedNames = groupNames + name
            val updatedApps = groupedApps + (name to emptyList())
            saveGroupsToPrefs(updatedNames, updatedApps)
            _applicationsState.value = buildState(updatedNames)
        }
    }

    fun renameGroup(oldName: String, newName: String) {
        viewModelScope.launch {
            if (newName.isBlank() || newName == oldName) return@launch
            val (groupNames, groupedApps) = loadGroupsFromPrefs()
            val updatedNames = groupNames.map { if (it == oldName) newName else it }
            val updatedApps = groupedApps.entries.associate { (k, v) ->
                (if (k == oldName) newName else k) to v
            }
            applicationsListCache = applicationsListCache.map { e ->
                if (e.group == oldName) e.moveToGroup(newName) else e
            }.toSet()
            saveGroupsToPrefs(updatedNames, updatedApps)
            _applicationsState.value = buildState(updatedNames)
        }
    }

    fun deleteGroup(name: String) {
        viewModelScope.launch {
            val (groupNames, groupedApps) = loadGroupsFromPrefs()
            val updatedNames = groupNames.filter { it != name }
            val updatedApps = groupedApps.filter { it.key != name }
            applicationsListCache = applicationsListCache.map { e ->
                if (e.group == name) e.moveToDrawer() else e
            }.toSet()
            saveGroupsToPrefs(updatedNames, updatedApps)
            _applicationsState.value = buildState(updatedNames)
        }
    }

    fun addAppToGroup(entry: LauncherEntry, groupName: String) {
        viewModelScope.launch {
            val (groupNames, groupedApps) = loadGroupsFromPrefs()
            if (groupName !in groupNames) return@launch
            val packageName = entry.packageInfo.packageName
            val updatedApps = groupedApps.entries.associate { (k, v) ->
                k to if (k == groupName) (v + packageName).distinct()
                      else v.filter { it != packageName }
            }
            applicationsListCache = applicationsListCache.map { e ->
                if (e.packageInfo.packageName == packageName) e.moveToGroup(groupName) else e
            }.toSet()
            savePinnedApps()
            saveGroupsToPrefs(groupNames, updatedApps)
            _applicationsState.value = buildState(groupNames)
        }
    }

    fun removeAppFromGroup(entry: LauncherEntry) {
        viewModelScope.launch {
            val (groupNames, groupedApps) = loadGroupsFromPrefs()
            val packageName = entry.packageInfo.packageName
            val updatedApps = groupedApps.mapValues { (_, v) -> v.filter { it != packageName } }
            applicationsListCache = applicationsListCache.map { e ->
                if (e.packageInfo.packageName == packageName) e.moveToDrawer() else e
            }.toSet()
            saveGroupsToPrefs(groupNames, updatedApps)
            _applicationsState.value = buildState(groupNames)
        }
    }

    // ── Notifications ─────────────────────────────────────────────────────────

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
                    applicationsListCache = applicationsListCache.updateAppEntry(packageName, message)
                }
            } catch (_: Exception) {
                _applicationsState.value = ApplicationSheetState()
            }
        }
    }

    // ── Shortcuts ─────────────────────────────────────────────────────────────

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

@Immutable
data class ApplicationSheetState(
    val pinnedApplications: Set<LauncherEntry> = setOf(),
    val applicationsList: Set<LauncherEntry> = setOf(),
    val groups: List<AppGroup> = emptyList(),
    val groupedApplications: Map<String, List<LauncherEntry>> = emptyMap(),
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
