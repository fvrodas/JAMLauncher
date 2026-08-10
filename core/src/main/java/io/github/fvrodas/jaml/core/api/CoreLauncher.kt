package io.github.fvrodas.jaml.core.api

import io.github.fvrodas.jaml.core.domain.entities.IconConfig
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.core.domain.usecases.ClearIconsAndReloadUseCase
import io.github.fvrodas.jaml.core.domain.usecases.GetApplicationsListUseCase
import io.github.fvrodas.jaml.core.domain.usecases.GetShortcutsListForApplicationUseCase
import io.github.fvrodas.jaml.core.domain.usecases.LaunchApplicationShortcutUseCase

internal class CoreLauncher(
    private val clearIconsAndReloadUseCase: ClearIconsAndReloadUseCase,
    private val getApplicationsListUseCase: GetApplicationsListUseCase,
    private val getShortcutsListForApplicationUseCase: GetShortcutsListForApplicationUseCase,
    private val launchApplicationShortcutUseCase: LaunchApplicationShortcutUseCase
) : CoreLauncherApi {
    override suspend fun clearIconAndReload(config: IconConfig) = clearIconsAndReloadUseCase(config)

    override suspend fun getApplicationsList(): List<PackageInfo> = getApplicationsListUseCase(null)

    override suspend fun getShortcutsListForApplication(packageName: String): List<PackageInfo.ShortcutInfo> =
        getShortcutsListForApplicationUseCase(packageName)

    override suspend fun launchApplicationShortcut(shortcutInfo: PackageInfo.ShortcutInfo) =
        launchApplicationShortcutUseCase(shortcutInfo)

}
