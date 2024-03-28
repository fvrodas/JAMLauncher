package io.github.fvrodas.jaml.ui.settings.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import io.github.fvrodas.jaml.ui.common.themes.JamlColorScheme

class SettingsViewModel(private val prefs: SharedPreferences) : ViewModel() {

    val isDynamicColorEnabled
        get() = prefs.getBoolean(
            LauncherSettings.DYNAMIC_COLOR_ENABLED,
            false
        )
    val selectedThemeName
        get() = prefs.getString(
            LauncherSettings.SELECTED_THEME,
            JamlColorScheme.Default.name
        ) ?: JamlColorScheme.Default.name

    fun saveSetting(key: String, value: Any) {
        prefs.edit().apply {
            when (value) {
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
            }
            apply()
        }
    }
}

object LauncherSettings {
    const val DYNAMIC_COLOR_ENABLED = "dyncolorenabled"
    const val SELECTED_THEME = "selectedtheme"
}
