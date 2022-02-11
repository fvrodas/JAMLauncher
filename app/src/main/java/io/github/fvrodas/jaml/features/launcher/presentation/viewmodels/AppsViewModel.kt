package io.github.fvrodas.jaml.features.launcher.presentation.viewmodels

import android.app.Application
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class AppsViewModel(
    app: Application,
    val getApplicationsListUseCase: GetApplicationsListUseCase,
    private val getShortcutsListForApplicationUseCase: GetShortcutsListForApplicationUseCase,
    private val shortcutsUtil: ShortcutsUtil,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AndroidViewModel(app) {

    private val applicationsListCache: ArrayList<AppInfo> = ArrayList()

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

                result.filterNot { applicationsListCache.contains(it) }
                    .takeIf { it.isNotEmpty() }?.let {
                        applicationsListCache.clear()
                        applicationsListCache.addAll(result)
                        appsUiState.value =
                            ApplicationsListUiState.Success(applicationsListCache.toList())
                    }


            } catch (e: Exception) {
                appsUiState.value = ApplicationsListUiState.Failure(e.message ?: "")
            }
        }
    }

    fun filterApplicationsList(query: String = "") {
        appsUiState.value = ApplicationsListUiState.Success(applicationsListCache.filter {
            it.label.contains(
                query,
                true
            )
        }.toList())
    }

    fun markNotification(packageName: String?, hasNotification: Boolean) {
        val item = applicationsListCache.firstOrNull {
            it.packageName.contains(packageName ?: "", ignoreCase = true)
        }
        item?.let {
            it.hasNotification = hasNotification
            appsUiState.value =
                ApplicationsListUiState.Success(applicationsListCache.toList())
        }
    }


    @RequiresApi(Build.VERSION_CODES.N_MR1)
    fun retrieveShortcuts(packageName: String) = flow {
        try {
            val result = getShortcutsListForApplicationUseCase(packageName)
            emit(result.getOrThrow())
        } catch (e: Exception) {
            appsUiState.value = ApplicationsListUiState.Failure(e.message ?: "")
        }
    }.flowOn(coroutineDispatcher)

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

