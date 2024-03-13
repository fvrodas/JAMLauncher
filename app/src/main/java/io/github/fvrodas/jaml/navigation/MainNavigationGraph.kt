package io.github.fvrodas.jaml.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.features.launcher.presentation.composables.HomeScreen
import io.github.fvrodas.jaml.features.settings.presentation.composables.SettingsScreen

@Composable
fun HomeNavigationGraph(
    navHostController: NavHostController,
    openApplication: (AppInfo) -> Unit,
    isDefaultHome: () -> Boolean,
    setAsDefaultHome: () -> Unit,
    setWallpaper: () -> Unit,
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
                isDefaultHome,
                setAsDefaultHome,
                setWallpaper,
                enableNotificationAccess
            ) {
                navHostController.navigateUp()
            }
        }
    }
}