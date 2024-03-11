package io.github.fvrodas.jaml

import android.annotation.SuppressLint
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.navigation.compose.rememberNavController
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.features.common.themes.JamlColorSchemes
import io.github.fvrodas.jaml.features.common.themes.JamlTheme
import io.github.fvrodas.jaml.navigation.HomeNavigationGraph
import io.github.fvrodas.jaml.features.settings.presentation.activities.SettingsActivity

class MainActivity : AppCompatActivity() {

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                this.recreate()
                if (result.data?.getBooleanExtra(
                        SettingsActivity.EXTRA_THEME_CHANGED,
                        false
                    ) != true
                ) {
                    Toast.makeText(
                        applicationContext,
                        resources.getString(R.string.app_name) + " has been set as default launcher.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(ColorDrawable(0x00000000))


        setContent {
            val navHostController = rememberNavController()

            val darkMode = isSystemInDarkTheme()

            JamlTheme(
                colorScheme = JamlColorSchemes.Default,
                isInDarkMode = darkMode
            ) {
                HomeNavigationGraph(
                    navHostController = navHostController,
                    onSettingsPressed = this::onSettingsPressed,
                    openApplication = this::openApplication
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!isDefault()) {
            requestDefaultHome()
        }
    }

    private fun requestDefaultHome() {
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

    private fun isDefault(): Boolean {
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

    private fun onSettingsPressed() {

    }

    private fun openApplication(appInfo: AppInfo) {
        packageManager?.getLaunchIntentForPackage(appInfo.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            startActivity(this)
        }
    }

}