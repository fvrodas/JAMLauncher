package io.github.fvrodas.jaml.ui.common.settings

import android.graphics.Color
import androidx.compose.runtime.Stable
import io.github.fvrodas.jaml.R

@Stable
data class LauncherPreferences(
    val launcherTheme: Int = R.string.theme_light,
    val isDynamicColorEnabled: Boolean = false,
    val launcherColorScheme: Int = R.string.colorscheme_default,
    val shouldHideApplicationIcons: Boolean = false,
    val shouldUseThemedIcons: Boolean = false,
    val themedIconForegroundColor: Int = Color.BLACK,
    val themedIconBackgroundColor: Int = Color.WHITE
) {
    companion object Companion {
        const val LAUNCHER_THEME = "launchertheme"
        const val DYNAMIC_COLOR_ENABLED = "dyncolorenabled"
        const val SELECTED_COLORSCHEME = "selectedtheme"
        const val SHOULD_HIDE_APPLICATION_ICONS = "shouldhideapplicationicons"
        const val SHOULD_USE_THEMED_ICONS = "shouldusethemedicons"

        const val THEMED_ICON_FOREGROUND_COLOR = "themediconforegroundcolor"
        const val THEMED_ICON_BACKGROUND_COLOR = "themediconbackgroundcolor"

        const val PINNED_APPS = "pinnedapplications"
        const val GROUPS_LIST = "groupslist"
        const val GROUPED_APPS = "groupedapps"
    }
}

enum class LauncherTheme {
    Light, Dark, System
}
