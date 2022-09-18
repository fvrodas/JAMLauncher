package io.github.fvrodas.jaml.features.launcher.presentation.viewmodels

import android.os.Build
import android.os.Process
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import io.github.fvrodas.jaml.BuildConfig
import io.github.fvrodas.jaml.core.data.repositories.ShortcutsUtil
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.core.domain.entities.AppShortcutInfo
import io.github.fvrodas.jaml.core.domain.usecases.GetApplicationsListUseCase
import io.github.fvrodas.jaml.core.domain.usecases.GetShortcutsListForApplicationUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.Exception


class AppsViewModel(
    val getApplicationsListUseCase: GetApplicationsListUseCase,
    private val getShortcutsListForApplicationUseCase: GetShortcutsListForApplicationUseCase,
    private val shortcutsUtil: ShortcutsUtil,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private var applicationsListCache: Set<AppInfo> = emptySet()

    val appsUiState: MutableStateFlow<ApplicationsListUiState> =
        MutableStateFlow(ApplicationsListUiState.Success(applicationsListCache.toList()))

    init {
        retrieveApplicationsList()
    }

    fun retrieveApplicationsList() {
        CoroutineScope(coroutineDispatcher).launch {
            try {
                val result = getApplicationsListUseCase(null).getOrThrow()
                    .filter { it.packageName != BuildConfig.APPLICATION_ID }

                val diff = result.minus(applicationsListCache)
                    .count()

                if (diff > 0) {
                    applicationsListCache = result.toSet()
                    appsUiState.value =
                        ApplicationsListUiState.Success(applicationsListCache.toList())
                }

            } catch (e: Exception) {
                appsUiState.value = ApplicationsListUiState.Failure(e.message ?: "")
            }
        }
    }

    fun filterApplicationsList(query: String = "") {
        CoroutineScope(coroutineDispatcher).launch {
            try {
                appsUiState.value = ApplicationsListUiState.Success(applicationsListCache.filter {
                    it.label.contains(
                        query,
                        true
                    )
                }.toList())
            } catch (e: Exception) {
                appsUiState.value = ApplicationsListUiState.Failure(e.message ?: "")
            }
        }
    }

    fun markNotification(packageName: String?, hasNotification: Boolean) {
        CoroutineScope(coroutineDispatcher).launch {
            try {
                val item = applicationsListCache.firstOrNull {
                    it.packageName.contains(packageName ?: "", ignoreCase = true)
                }
                item?.let {
                    it.hasNotification = hasNotification
                    appsUiState.value =
                        ApplicationsListUiState.Success(applicationsListCache.toList())
                }
            } catch (e: Exception) {
                appsUiState.value = ApplicationsListUiState.Failure(e.message ?: "")
            }
        }
    }

    fun retrieveShortcuts(packageName: String) = flow {
        emit(getShortcutsListForApplicationUseCase(packageName).getOrThrow())
    }.flowOn(coroutineDispatcher)
        .catch { e ->
            appsUiState.value = ApplicationsListUiState.Failure(e.message ?: "")
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

sealed class ApplicationsListUiState {
    data class Success(val apps: List<AppInfo>) : ApplicationsListUiState()
    data class Failure(val errorMessage: String) : ApplicationsListUiState()
}

