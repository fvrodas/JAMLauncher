package io.github.fvrodas.jaml.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.features.launcher.presentation.composables.HomeScreen
import io.github.fvrodas.jaml.features.settings.presentation.composables.SettingsScreen
import io.github.fvrodas.jaml.features.settings.presentation.viewmodels.SettingsViewModel

@Composable
fun HomeNavigationGraph(
    navHostController: NavHostController,
    settingsViewModel: SettingsViewModel,
    openApplication: (AppInfo) -> Unit,
    isDefaultHome: () -> Boolean,
    setAsDefaultHome: () -> Unit,
    setWallpaper: () -> Unit,
    onSettingsSaved: () -> Unit,
    enableNotificationAccess: () -> Unit
) {
    NavHost(
        navController = navHostController,
        startDestination = Routes.HOME_SCREEN
    ) {

        composable(Routes.HOME_SCREEN) {
            LaunchedEffect(Unit) {
                if(!isDefaultHome()) {
                    setAsDefaultHome()
                }
            }

            HomeScreen(
                onSettingsPressed = {
                    navHostController.navigate(Routes.SETTINGS_SCREEN)
                },
                onApplicationPressed = openApplication
            )
        }

        composable(Routes.SETTINGS_SCREEN) {
            SettingsScreen(
                settingsViewModel,
                isDefaultHome,
                setAsDefaultHome,
                setWallpaper,
                enableNotificationAccess,
                onSettingsSaved
            ) {
                navHostController.navigateUp()
            }
        }
    }
}