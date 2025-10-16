package io.github.fvrodas.jaml.ui.settings.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.ui.common.settings.LauncherPreferences
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val prefs: SharedPreferences) : ViewModel() {

    private val _launcherPreferences = MutableStateFlow(
        LauncherPreferences(
            prefs.getInt(LauncherPreferences.LAUNCHER_THEME, R.string.theme_light),
            prefs.getBoolean(LauncherPreferences.DYNAMIC_COLOR_ENABLED, false),
            prefs.getInt(LauncherPreferences.SELECTED_COLORSCHEME, R.string.colorscheme_default),
            prefs.getBoolean(LauncherPreferences.SHOULD_HIDE_APPLICATION_ICONS, false),
        )
    )
    val launcherPreferences: StateFlow<LauncherPreferences> = _launcherPreferences

    init {
        retrieveLauncherSettings()
    }

    private fun retrieveLauncherSettings() {
        viewModelScope.launch {
            _launcherPreferences.value = LauncherPreferences(
                prefs.getInt(LauncherPreferences.LAUNCHER_THEME, R.string.theme_light),
                prefs.getBoolean(LauncherPreferences.DYNAMIC_COLOR_ENABLED, false),
                prefs.getInt(LauncherPreferences.SELECTED_COLORSCHEME, R.string.colorscheme_default),
                prefs.getBoolean(LauncherPreferences.SHOULD_HIDE_APPLICATION_ICONS, false),
            )
        }
    }

    fun saveSetting(newSettings: LauncherPreferences) {
        viewModelScope.launch {
            async {
                prefs.edit().apply {
                    putInt(
                        LauncherPreferences.LAUNCHER_THEME,
                        newSettings.launcherTheme
                    )
                    putBoolean(
                        LauncherPreferences.DYNAMIC_COLOR_ENABLED,
                        newSettings.isDynamicColorEnabled
                    )
                    putInt(LauncherPreferences.SELECTED_COLORSCHEME, newSettings.launcherColorScheme)
                    putBoolean(
                        LauncherPreferences.SHOULD_HIDE_APPLICATION_ICONS,
                        newSettings.shouldHideApplicationIcons
                    )
                    apply()
                }
            }
            retrieveLauncherSettings()
        }
    }
}


