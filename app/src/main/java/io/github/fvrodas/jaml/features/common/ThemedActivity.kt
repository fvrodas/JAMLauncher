package io.github.fvrodas.jaml.features.common

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.features.settings.presentation.fragments.SettingsFragment

open class ThemedActivity : AppCompatActivity() {

    private lateinit var defaultPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        val themeNames = resources.getStringArray(R.array.theme_names)
        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isDynamicColorEnabled =
            defaultPrefs.getBoolean(SettingsFragment.PREF_DYNAMIC_COLOR, false)
        if (isDynamicColorEnabled) {
            setTheme(R.style.Theme_Dynamic)
            DynamicColors.applyToActivitiesIfAvailable(application)
        } else {

            val themedId: Int =
                when (defaultPrefs.getString(SettingsFragment.PREF_THEME, themeNames[0])) {
                    themeNames[1] -> R.style.Theme_Gruvbox
                    themeNames[2] -> R.style.Theme_Nord
                    else -> R.style.Theme_Default
                }
            setTheme(themedId)
        }
        super.onCreate(savedInstanceState)
    }
}