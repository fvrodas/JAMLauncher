package io.github.fvrodas.jaml.features.settings.presentation.activities

import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import androidx.core.content.ContextCompat
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.features.common.ThemedActivity
import io.github.fvrodas.jaml.features.settings.presentation.fragments.SettingsFragment

class SettingsActivity : ThemedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
        window.statusBarColor = ContextCompat.getColor(this, typedValue.resourceId)

        setContentView(R.layout.activity_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment, SettingsFragment.newInstance())
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }

    companion object {
        const val EXTRA_THEME_CHANGED = "theme_changed"
    }

}