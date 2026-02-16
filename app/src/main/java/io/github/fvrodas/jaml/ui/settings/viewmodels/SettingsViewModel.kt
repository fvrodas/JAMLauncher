package io.github.fvrodas.jaml.ui.settings.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.ui.common.settings.LauncherPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class SettingsViewModel(private val prefs: SharedPreferences) : ViewModel() {

    private val _launcherPreferences = MutableStateFlow(
        readFromPrefs()
    )
    val launcherPreferences: StateFlow<LauncherPreferences> = _launcherPreferences
        .onStart {
            retrieveLauncherSettings()
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            readFromPrefs()
        )

    private fun readFromPrefs(): LauncherPreferences {
        return LauncherPreferences(
            prefs.getInt(LauncherPreferences.LAUNCHER_THEME, R.string.theme_light),
            prefs.getBoolean(LauncherPreferences.DYNAMIC_COLOR_ENABLED, false),
            prefs.getInt(LauncherPreferences.SELECTED_COLORSCHEME, R.string.colorscheme_default),
            prefs.getBoolean(LauncherPreferences.SHOULD_HIDE_APPLICATION_ICONS, false),
        )
    }

    private fun retrieveLauncherSettings() {
        _launcherPreferences.value = readFromPrefs()
    }

    fun saveSetting(newSettings: LauncherPreferences) {
        prefs.edit().apply {
            putInt(LauncherPreferences.LAUNCHER_THEME, newSettings.launcherTheme)
            putBoolean(LauncherPreferences.DYNAMIC_COLOR_ENABLED, newSettings.isDynamicColorEnabled)
            putInt(LauncherPreferences.SELECTED_COLORSCHEME, newSettings.launcherColorScheme)
            putBoolean(
                LauncherPreferences.SHOULD_HIDE_APPLICATION_ICONS,
                newSettings.shouldHideApplicationIcons
            )
            commit()
        }
        _launcherPreferences.value = newSettings
    }
}
