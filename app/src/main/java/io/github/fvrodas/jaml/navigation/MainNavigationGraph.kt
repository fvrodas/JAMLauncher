package io.github.fvrodas.jaml.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.ui.launcher.composables.HomeScreen
import io.github.fvrodas.jaml.ui.launcher.viewmodels.HomeViewModel
import io.github.fvrodas.jaml.ui.settings.composables.SettingsScreen
import io.github.fvrodas.jaml.ui.settings.viewmodels.SettingsViewModel

@Composable
fun HomeNavigationGraph(
    navHostController: NavHostController,
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    openApplication: (AppInfo) -> Unit,
    openApplicationInfo: (AppInfo) -> Unit,
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
                homeViewModel = homeViewModel,
                onSettingsPressed = {
                    navHostController.navigate(Routes.SETTINGS_SCREEN)
                },
                onApplicationInfoPressed = openApplicationInfo,
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
                onSettingsSaved,
            ) {
                navHostController.navigateUp()
            }
        }
    }
}