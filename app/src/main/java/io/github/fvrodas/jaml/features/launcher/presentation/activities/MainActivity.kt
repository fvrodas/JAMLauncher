package io.github.fvrodas.jaml.features.launcher.presentation.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.rememberNavController
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.features.common.ThemedActivity
import io.github.fvrodas.jaml.features.common.themes.JamlColors
import io.github.fvrodas.jaml.features.common.themes.JamlTheme
import io.github.fvrodas.jaml.features.common.viewmodels.ThemeViewModel
import io.github.fvrodas.jaml.features.launcher.navigation.HomeNavigationGraph
import io.github.fvrodas.jaml.features.settings.presentation.activities.SettingsActivity
import org.koin.android.ext.android.getKoin

class MainActivity : ThemedActivity() {

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(ColorDrawable(android.R.color.transparent))

        setContent {
            val currentTheme by themeViewModel.currentTheme.collectAsState()
            val navHostController = rememberNavController()

            JamlTheme(
                colorScheme = currentTheme,
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

    private fun onSettingsPressed() {
        Intent(this, SettingsActivity::class.java).apply {
            this@MainActivity.startForResult.launch(this)
        }
    }

    private fun openApplication(appInfo: AppInfo) {
        packageManager?.getLaunchIntentForPackage(appInfo.packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            startActivity(this)
        }
    }

}