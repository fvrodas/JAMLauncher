package io.github.fvrodas.jaml.ui.settings.viewmodels

import android.content.SharedPreferences
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.fvrodas.jaml.R
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val prefs: SharedPreferences) : ViewModel() {

    private val _launcherSettings = MutableStateFlow(
        LauncherSettings(
            prefs.getBoolean(LauncherSettings.DYNAMIC_COLOR_ENABLED, false),
            prefs.getInt(LauncherSettings.SELECTED_THEME, R.string.theme_default),
            prefs.getBoolean(LauncherSettings.SHOULD_HIDE_APPLICATION_ICONS, false),
        )
    )
    val launcherSettings: StateFlow<LauncherSettings> = _launcherSettings

    init {
        retrieveLauncherSettings()
    }

    private fun retrieveLauncherSettings() {
        viewModelScope.launch {
            _launcherSettings.value = LauncherSettings(
                prefs.getBoolean(LauncherSettings.DYNAMIC_COLOR_ENABLED, false),
                prefs.getInt(LauncherSettings.SELECTED_THEME, R.string.theme_default),
                prefs.getBoolean(LauncherSettings.SHOULD_HIDE_APPLICATION_ICONS, false),
            )
        }
    }

    fun saveSetting(newSettings: LauncherSettings) {
        viewModelScope.launch {
            async {
                prefs.edit().apply {
                    putBoolean(
                        LauncherSettings.DYNAMIC_COLOR_ENABLED,
                        newSettings.isDynamicColorEnabled
                    )
                    putInt(LauncherSettings.SELECTED_THEME, newSettings.selectedThemeName)
                    putBoolean(
                        LauncherSettings.SHOULD_HIDE_APPLICATION_ICONS,
                        newSettings.shouldHideApplicationIcons
                    )
                    apply()
                }
            }
            retrieveLauncherSettings()
        }
    }
}

@Stable
data class LauncherSettings(
    var isDynamicColorEnabled: Boolean = false,
    var selectedThemeName: Int = R.string.theme_default,
    var shouldHideApplicationIcons: Boolean = false,
) {
    companion object {
        const val DYNAMIC_COLOR_ENABLED = "dyncolorenabled"
        const val SELECTED_THEME = "selectedtheme"
        const val SHOULD_HIDE_APPLICATION_ICONS = "shouldhideapplicationicons"
        const val PINNED_APPS = "pinnedapplications"
    }
}
