package io.github.fvrodas.jaml.features.launcher.presentation.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.rememberNavController
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.features.common.themes.JamlColors
import io.github.fvrodas.jaml.features.common.themes.JamlTheme
import io.github.fvrodas.jaml.features.launcher.navigation.HomeNavigationGraph
import io.github.fvrodas.jaml.features.settings.presentation.activities.SettingsActivity

class MainActivity : AppCompatActivity() {

    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER)
        window.setBackgroundDrawable(ColorDrawable(android.R.color.transparent))

        setContent {
            val navHostController = rememberNavController()
            JamlTheme(
                colorScheme = JamlColors.Default,
            ) {
                HomeNavigationGraph(
                    navHostController = navHostController,
                    openApplication = this::openApplication
                )
            }
        }
    }

    private fun openApplication(appInfo: AppInfo) {
        Log.d("AppInfo", appInfo.packageName)
        if (appInfo.packageName == SettingsActivity::class.java.name) {
//            Intent(this, SettingsActivity::class.java).apply {
//                startForResult.launch(this)
//            }
        } else {
            packageManager?.getLaunchIntentForPackage(appInfo.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                startActivity(this)
            }
        }
    }

}