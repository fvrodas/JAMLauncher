package io.github.fvrodas.jaml.core.api

import io.github.fvrodas.jaml.core.domain.entities.IconConfig
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo

interface CoreLauncherApi {
    suspend fun clearIconAndReload(config: IconConfig)
    suspend fun getApplicationsList(): List<PackageInfo>
    suspend fun getShortcutsListForApplication(packageName: String): List<PackageInfo.ShortcutInfo>
    suspend fun launchApplicationShortcut(shortcutInfo: PackageInfo.ShortcutInfo)
}
