package io.github.fvrodas.jaml.ui

import android.annotation.SuppressLint
import android.app.SearchManager
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.graphics.drawable.toDrawable
import androidx.navigation.compose.rememberNavController
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.framework.receivers.PackageChangedReceiver
import io.github.fvrodas.jaml.framework.services.JAMLNotificationService
import io.github.fvrodas.jaml.navigation.HomeNavigationGraph
import io.github.fvrodas.jaml.ui.common.themes.JamlColorScheme
import io.github.fvrodas.jaml.ui.common.themes.JamlTheme
import io.github.fvrodas.jaml.ui.common.themes.LauncherSettings
import io.github.fvrodas.jaml.ui.common.themes.LauncherTheme
import io.github.fvrodas.jaml.ui.common.themes.colorSchemeByName
import io.github.fvrodas.jaml.ui.common.themes.launcherThemeByName
import io.github.fvrodas.jaml.ui.settings.viewmodels.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : androidx.activity.ComponentActivity() {

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                JAMLNotificationService.tryReEnableNotificationListener(this)
            }
        }

    private lateinit var packageReceiver: PackageChangedReceiver

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(
                0x00000000,
                0x00000000
            ) { true }
        )

        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(0x00000000.toDrawable())

        packageReceiver = PackageChangedReceiver()

        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()

            val navHostController = rememberNavController()

            val launcherSettings: LauncherSettings by settingsViewModel.launcherSettings.collectAsState()

            var theme: Int by remember {
                mutableIntStateOf(launcherSettings.launcherTheme)
            }

            var colorScheme: Int by remember {
                mutableIntStateOf(launcherSettings.launcherColorScheme)
            }

            var dynamicColorEnabled: Boolean by remember {
                mutableStateOf(launcherSettings.isDynamicColorEnabled)
            }

            JamlTheme(
                colorScheme = colorSchemeByName[colorScheme] ?: JamlColorScheme.Default,
                isInDarkMode = when (launcherThemeByName[theme]) {
                    LauncherTheme.Light -> false
                    LauncherTheme.Dark -> true
                    else -> isSystemInDarkTheme()
                },
                isDynamicColorsEnabled = dynamicColorEnabled
            ) {
                HomeNavigationGraph(
                    navHostController = navHostController,
                    launcherSettings = launcherSettings,
                    openApplication = this::openApplication,
                    openApplicationInfo = this::openApplicationInfo,
                    isDefaultHome = this::isDefault,
                    requestDefaultHome = this::requestDefaultHome,
                    setWallpaper = this::setWallpaper,
                    onSettingsSaved = {
                        settingsViewModel.saveSetting(it)
                        colorScheme = it.launcherColorScheme
                        dynamicColorEnabled = it.isDynamicColorEnabled
                        theme = it.launcherTheme
                    },
                    enableNotificationAccess = this::enableNotificationAccess,
                    performWebSearch = this::performWebSearch,
                    openWebPage = this::openWebPage
                )
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                packageReceiver, PackageChangedReceiver.provideIntentFilter(),
                RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(packageReceiver, PackageChangedReceiver.provideIntentFilter())
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(packageReceiver)
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
            val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
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

    private fun openApplication(packageInfo: PackageInfo) {
        packageManager?.getLaunchIntentForPackage(packageInfo.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            startActivity(this)
        }
    }

    private fun openApplicationInfo(packageInfo: PackageInfo) {
        Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageInfo.packageName, null)
        }.also {
            startActivity(it)
        }
    }

    private fun performWebSearch(query: String) {
        Intent().apply {
            action = Intent.ACTION_WEB_SEARCH
            putExtra(SearchManager.QUERY, query)
        }.also { intent ->
            startActivity(intent)
        }
    }

    private fun openWebPage(uri: Uri) {
        Intent().apply {
            action = Intent.ACTION_VIEW
            data = uri
        }.also { intent ->
            startActivity(intent)
        }
    }
}

internal const val SET_WALLPAPER_ACTION = "android.intent.action.SET_WALLPAPER"
internal const val ENABLE_NOTIFICATION_ACTION =
    "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
