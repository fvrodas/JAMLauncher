package io.github.fvrodas.jaml.features.common

import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.features.launcher.presentation.activities.MainActivity
import io.github.fvrodas.jaml.features.settings.presentation.fragments.SettingsFragment


open class ThemedActivity : AppCompatActivity() {

    private lateinit var defaultPrefs: SharedPreferences

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(
                    applicationContext,
                    resources.getString(R.string.app_name) + " has been set as default launcher.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

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

    fun requestDefaultHome() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!isDefault()) {
                val componentName =
                    ComponentName(this, MainActivity::class.java)
                packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
                )

                val selector = Intent(Intent.ACTION_MAIN)
                selector.addCategory(Intent.CATEGORY_HOME)
                selector.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(selector)

                packageManager.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                    PackageManager.DONT_KILL_APP
                )
            }
        } else {
            val roleManager = getSystemService(Context.ROLE_SERVICE) as RoleManager
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME) && !roleManager.isRoleHeld(
                    RoleManager.ROLE_HOME
                )
            ) {
                val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
                startForResult.launch(intent)
            }
        }
    }

    fun isDefault(): Boolean {
        val localPackageManager = packageManager
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            @Suppress("DEPRECATION")
            val str = localPackageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )!!.activityInfo.packageName
            str == packageName
        } else {
            val str = localPackageManager.resolveActivity(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )!!.activityInfo.packageName
            str == packageName
        }

    }
}