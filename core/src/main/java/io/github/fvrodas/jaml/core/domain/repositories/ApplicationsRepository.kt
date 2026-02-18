package io.github.fvrodas.jaml.core.domain.repositories

import android.graphics.Color
import io.github.fvrodas.jaml.core.data.repositories.MAX_SHORTCUTS_TO_DISPLAY
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo

interface ApplicationsRepository {
    suspend fun getApplicationsList(): List<PackageInfo>
    suspend fun getShortcutsListForApplication(
        packageName: String,
        maxShortcuts: Int = MAX_SHORTCUTS_TO_DISPLAY
    ): List<PackageInfo.ShortcutInfo>

    suspend fun launchShortcut(shortcut: PackageInfo.ShortcutInfo)

    suspend fun clearIconCacheAndLoad(
        loadThemedIcons: Boolean = false,
        backgroundColor: Int = Color.WHITE,
        foregroundColor: Int = Color.BLACK,
    )
}
