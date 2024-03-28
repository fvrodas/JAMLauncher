package io.github.fvrodas.jaml.ui

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
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.framework.receivers.NotificationReceiver
import io.github.fvrodas.jaml.framework.services.INotificationEventListener
import io.github.fvrodas.jaml.navigation.HomeNavigationGraph
import io.github.fvrodas.jaml.ui.common.themes.JamlColorScheme
import io.github.fvrodas.jaml.ui.common.themes.JamlTheme
import io.github.fvrodas.jaml.ui.common.themes.themesByName
import io.github.fvrodas.jaml.ui.launcher.viewmodels.AppsViewModel
import io.github.fvrodas.jaml.ui.settings.viewmodels.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : INotificationEventListener, androidx.activity.ComponentActivity() {

    private val appsViewModel: AppsViewModel by viewModel()
    private val settingsViewModel: SettingsViewModel by viewModel()

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                this.recreate()
            }
        }

    private lateinit var notificationReceiver: NotificationReceiver

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(ColorDrawable(0x00000000))

        actionBar?.hide()

        notificationReceiver = NotificationReceiver(this)

        setContent {
            val navHostController = rememberNavController()
            val darkMode = isSystemInDarkTheme()

            var selectedTheme by remember {
                mutableStateOf(settingsViewModel.selectedThemeName)
            }
            var isDynamicColorsEnabled by remember {
                mutableStateOf(settingsViewModel.isDynamicColorEnabled)
            }

            JamlTheme(
                colorScheme = themesByName[selectedTheme] ?: JamlColorScheme.Default,
                isInDarkMode = darkMode,
                isDynamicColorsEnabled = isDynamicColorsEnabled
            ) {
                HomeNavigationGraph(
                    navHostController = navHostController,
                    appsViewModel = appsViewModel,
                    settingsViewModel = settingsViewModel,
                    openApplication = this::openApplication,
                    isDefaultHome = this::isDefault,
                    setAsDefaultHome = this::requestDefaultHome,
                    setWallpaper = this::setWallpaper,
                    onSettingsSaved = {
                        isDynamicColorsEnabled = settingsViewModel.isDynamicColorEnabled
                        selectedTheme = settingsViewModel.selectedThemeName
                    },
                    enableNotificationAccess = this::enableNotificationAccess
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(notificationReceiver, NotificationReceiver.provideIntentFilter(),
                RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(notificationReceiver, NotificationReceiver.provideIntentFilter())
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(notificationReceiver)
    }

    private fun setWallpaper() {
        Intent().apply {
            action = SET_WALLPAPER_ACTION
        }.also {
            startActivity(it)
        }
    }

    private fun enableNotificationAccess() {
        Intent().apply {
            action = ENABLE_NOTIFICATION_ACTION
        }.also {
            startActivity(it)
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

    private fun openApplication(appInfo: AppInfo) {
        packageManager?.getLaunchIntentForPackage(appInfo.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            startActivity(this)
        }
    }

    override fun onNotificationEvent(packageName: String?, hasNotification: Boolean) {
        appsViewModel.markNotification(packageName, hasNotification)
    }
}

internal const val SET_WALLPAPER_ACTION = "android.intent.action.SET_WALLPAPER"
internal const val ENABLE_NOTIFICATION_ACTION =
    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
