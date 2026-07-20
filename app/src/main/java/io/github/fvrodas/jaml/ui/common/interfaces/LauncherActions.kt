package io.github.fvrodas.jaml.ui.common.interfaces

import androidx.compose.runtime.Stable
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo

@Stable
interface LauncherActions {
    fun openApplication(packageInfo: PackageInfo)

    fun openApplicationInfo(packageInfo: PackageInfo)

    fun performWebSearch(query: String)
}
