package io.github.fvrodas.jaml.features.settings.presentation.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.DropDownPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.google.android.material.color.DynamicColors
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.features.settings.presentation.activities.SettingsActivity

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dynamicColorPrefs = findPreference<SwitchPreference>(PREF_DYNAMIC_COLOR)
        val launcherThemePrefs = findPreference<DropDownPreference>(PREF_THEME)

        val isDynamicColorEnabled = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getBoolean(PREF_DYNAMIC_COLOR, false)
        launcherThemePrefs?.isEnabled = !isDynamicColorEnabled

        launcherThemePrefs?.let {
            it.setOnPreferenceChangeListener { _, _ ->
                requireActivity().recreate()
                requireActivity().setResult(AppCompatActivity.RESULT_OK, Intent().apply {
                    putExtra(SettingsActivity.EXTRA_THEME_CHANGED, true)
                })
                requireActivity().finish()
                true
            }
        }

        dynamicColorPrefs?.let {
            it.setOnPreferenceChangeListener { _, _ ->
                requireActivity().recreate()
                requireActivity().setResult(AppCompatActivity.RESULT_OK, Intent().apply {
                    putExtra(SettingsActivity.EXTRA_THEME_CHANGED, true)
                })
                requireActivity().finish()
                true
            }
        }

        if (Build.VERSION.SDK_INT < 31) {
            dynamicColorPrefs?.isEnabled = false
        }
    }

    companion object {
        const val PREF_THEME = "launcher_theme"
        const val PREF_DYNAMIC_COLOR = "dynamic_color_enabled"
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}