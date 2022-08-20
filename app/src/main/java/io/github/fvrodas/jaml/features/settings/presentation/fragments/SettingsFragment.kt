package io.github.fvrodas.jaml.features.settings.presentation.fragments

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.DropDownPreference
import androidx.preference.PreferenceFragmentCompat
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.features.settings.presentation.activities.SettingsActivity

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        findPreference<DropDownPreference>(PREF_THEME)?.let {
            it.setOnPreferenceChangeListener { _, _ ->
                requireActivity().recreate()
                requireActivity().setResult(AppCompatActivity.RESULT_OK, Intent().apply {
                    putExtra(SettingsActivity.EXTRA_THEME_CHANGED, true)
                })
                requireActivity().finish()
                true
            }
        }

    }

    companion object {
        const val PREF_THEME = "launcher_theme"
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}