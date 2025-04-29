package io.github.fvrodas.jaml.ui.settings.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.fvrodas.jaml.ui.common.themes.JamlColorScheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val prefs: SharedPreferences) : ViewModel() {

    private val _launcherSettings = MutableStateFlow<LauncherSettings>(LauncherSettings())
    val launcherSettings: StateFlow<LauncherSettings> = _launcherSettings

    private fun retrieveLauncherSettings() {
        viewModelScope.launch {
            _launcherSettings.value = LauncherSettings(
                prefs.getBoolean(LauncherSettings.DYNAMIC_COLOR_ENABLED, false),
                prefs.getString(LauncherSettings.SELECTED_THEME, JamlColorScheme.Default.name) ?: "",
                prefs.getBoolean(LauncherSettings.SHOULD_HIDE_APPLICATION_ICONS, false),
            )
        }
    }

    fun saveSetting(newSettings: LauncherSettings) {
        viewModelScope.launch {
            prefs.edit().apply {
                putBoolean(
                    LauncherSettings.DYNAMIC_COLOR_ENABLED,
                    newSettings.isDynamicColorEnabled
                )
                putString(LauncherSettings.SELECTED_THEME, newSettings.selectedThemeName)
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

data class LauncherSettings(
    var isDynamicColorEnabled: Boolean = false,
    var selectedThemeName: String = JamlColorScheme.Default.name,
    var shouldHideApplicationIcons: Boolean = false,
) {
    companion object {
        const val DYNAMIC_COLOR_ENABLED = "dyncolorenabled"
        const val SELECTED_THEME = "selectedtheme"
        const val SHOULD_HIDE_APPLICATION_ICONS = "shouldhideapplicationicons"
    }
}
