package io.github.fvrodas.jaml.features.settings.presentation.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.features.common.ThemedActivity
import io.github.fvrodas.jaml.features.settings.presentation.activities.SettingsActivity

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dynamicColorPrefs = findPreference<SwitchPreference>(PREF_DYNAMIC_COLOR)
        val launcherThemePrefs = findPreference<DropDownPreference>(PREF_THEME)
        val setDefaultHome = findPreference<Preference>(PREF_DEFAULT_HOME)

        val isDefaultHome =  (requireActivity() as ThemedActivity).isDefault()

        val isDynamicColorEnabled = PreferenceManager.getDefaultSharedPreferences(requireContext())
            .getBoolean(PREF_DYNAMIC_COLOR, false)
        launcherThemePrefs?.isVisible = !isDynamicColorEnabled

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

        setDefaultHome?.isVisible = !isDefaultHome

        setDefaultHome?.setOnPreferenceClickListener {
            (requireActivity() as ThemedActivity).requestDefaultHome()
            true
        }

        if (Build.VERSION.SDK_INT < 31) {
            dynamicColorPrefs?.isVisible = false
        }
    }

    companion object {
        const val PREF_THEME = "launcher_theme"
        const val PREF_DYNAMIC_COLOR = "dynamic_color_enabled"
        const val PREF_DEFAULT_HOME = "set_default_launcher"
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}