package io.github.fvrodas.jaml.ui.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import io.github.fvrodas.jaml.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}