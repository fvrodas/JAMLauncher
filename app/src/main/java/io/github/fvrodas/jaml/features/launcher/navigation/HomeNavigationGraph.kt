package io.github.fvrodas.jaml.features.launcher.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.features.launcher.presentation.composables.HomeScreen

@Composable
fun HomeNavigationGraph(
    navHostController: NavHostController,
    onSettingsPressed: () -> Unit,
    openApplication: (AppInfo) -> Unit,
) {
    NavHost(
        navController = navHostController,
        startDestination = HomeRoutes.HOME_SCREEN
    ) {

        composable(HomeRoutes.HOME_SCREEN) {
            HomeScreen(
                onSettingsPressed = onSettingsPressed,
                onApplicationPressed = openApplication
            )
        }
    }
}