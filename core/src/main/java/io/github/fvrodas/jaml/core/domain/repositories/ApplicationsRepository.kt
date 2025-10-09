package io.github.fvrodas.jaml.core.domain.repositories

import io.github.fvrodas.jaml.core.domain.entities.PackageInfo

interface ApplicationsRepository {
    suspend fun getApplicationsList(): List<PackageInfo>
    suspend fun getShortcutsListForApplication(packageName: String): List<PackageInfo.ShortcutInfo>
    suspend fun launchShortcut(shortcut: PackageInfo.ShortcutInfo)
}