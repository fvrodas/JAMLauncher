package io.github.fvrodas.jaml.core.domain.repositories

import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.core.domain.entities.AppShortcutInfo

interface IApplicationsRepository {
    suspend fun getApplicationsList(): Result<List<AppInfo>>
    suspend fun getShortcutsListForApplication(packageName: String): Result<List<AppShortcutInfo>>
}