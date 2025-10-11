package io.github.fvrodas.jaml.ui.common.themes

import androidx.compose.runtime.Stable
import io.github.fvrodas.jaml.R

@Stable
data class LauncherSettings(
    var launcherTheme: Int = R.string.theme_light,
    var isDynamicColorEnabled: Boolean = false,
    var launcherColorScheme: Int = R.string.colorscheme_default,
    var shouldHideApplicationIcons: Boolean = false,
) {
    companion object {
        const val LAUNCHER_THEME = "launchertheme"
        const val DYNAMIC_COLOR_ENABLED = "dyncolorenabled"
        const val SELECTED_COLORSCHEME = "selectedtheme"
        const val SHOULD_HIDE_APPLICATION_ICONS = "shouldhideapplicationicons"
        const val PINNED_APPS = "pinnedapplications"
    }
}

enum class LauncherTheme {
    Light, Dark, System
}