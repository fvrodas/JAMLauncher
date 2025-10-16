package io.github.fvrodas.jaml.ui.common.interfaces

import io.github.fvrodas.jaml.core.domain.entities.PackageInfo

interface LauncherActions {
    fun openApplication(packageInfo: PackageInfo)

    fun openApplicationInfo(packageInfo: PackageInfo)

    fun performWebSearch(query: String)
}
