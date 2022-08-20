package io.github.fvrodas.jaml.features.common

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.features.settings.presentation.fragments.SettingsFragment

open class ThemedActivity : AppCompatActivity() {

    private lateinit var defaultPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        val themeNames = resources.getStringArray(R.array.theme_names)
        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val themedId: Int =
            when (defaultPrefs.getString(SettingsFragment.PREF_THEME, themeNames[0])) {
                themeNames[1] -> R.style.Theme_Gruvbox
                else -> R.style.Theme_Default
            }
        setTheme(themedId)
        super.onCreate(savedInstanceState)
    }
}