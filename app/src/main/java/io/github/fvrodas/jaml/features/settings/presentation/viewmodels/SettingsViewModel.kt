package io.github.fvrodas.jaml.features.settings.presentation.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel

class SettingsViewModel(private val prefs: SharedPreferences) : ViewModel() {

    val isDynamicColorEnabled get() =  prefs.getBoolean(LauncherSettings.DYNAMIC_COLOR_ENABLED, false)

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
