package io.github.fvrodas.jaml.domain.entity

import io.github.fvrodas.jaml.core.domain.entities.PackageInfo

sealed class LauncherItem {
    data class Group(val groupName: String) : LauncherItem() {
        companion object {
            const val HOME_GROUP = "Home"
        }
    }
    data class App(val appInfo: PackageInfo) : LauncherItem()
}
