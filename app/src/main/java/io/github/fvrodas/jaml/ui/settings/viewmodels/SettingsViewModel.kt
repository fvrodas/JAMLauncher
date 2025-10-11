package io.github.fvrodas.jaml.ui.settings.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.ui.common.themes.LauncherSettings
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val prefs: SharedPreferences) : ViewModel() {

    private val _launcherSettings = MutableStateFlow(
        LauncherSettings(
            prefs.getInt(LauncherSettings.LAUNCHER_THEME, R.string.theme_light),
            prefs.getBoolean(LauncherSettings.DYNAMIC_COLOR_ENABLED, false),
            prefs.getInt(LauncherSettings.SELECTED_COLORSCHEME, R.string.colorscheme_default),
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
                prefs.getInt(LauncherSettings.LAUNCHER_THEME, R.string.theme_light),
                prefs.getBoolean(LauncherSettings.DYNAMIC_COLOR_ENABLED, false),
                prefs.getInt(LauncherSettings.SELECTED_COLORSCHEME, R.string.colorscheme_default),
                prefs.getBoolean(LauncherSettings.SHOULD_HIDE_APPLICATION_ICONS, false),
            )
        }
    }

    fun saveSetting(newSettings: LauncherSettings) {
        viewModelScope.launch {
            async {
                prefs.edit().apply {
                    putInt(
                        LauncherSettings.LAUNCHER_THEME,
                        newSettings.launcherTheme
                    )
                    putBoolean(
                        LauncherSettings.DYNAMIC_COLOR_ENABLED,
                        newSettings.isDynamicColorEnabled
                    )
                    putInt(LauncherSettings.SELECTED_COLORSCHEME, newSettings.launcherColorScheme)
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


