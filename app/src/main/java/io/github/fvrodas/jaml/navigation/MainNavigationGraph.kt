package io.github.fvrodas.jaml.navigation

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.launcher.composables.HomeScreen
import io.github.fvrodas.jaml.ui.launcher.viewmodels.HomeViewModel
import io.github.fvrodas.jaml.ui.settings.composables.SettingsScreen
import io.github.fvrodas.jaml.ui.settings.viewmodels.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeNavigationGraph(
    navHostController: NavHostController,
    settingsViewModel: SettingsViewModel,
    openApplication: (PackageInfo) -> Unit,
    openApplicationInfo: (PackageInfo) -> Unit,
    isDefaultHome: () -> Boolean,
    requestDefaultHome: () -> Unit,
    setWallpaper: () -> Unit,
    onSettingsSaved: () -> Unit,
    enableNotificationAccess: () -> Unit
) {
    NavHost(
        navController = navHostController,
        startDestination = Routes.HOME_SCREEN
    ) {

        composable(Routes.HOME_SCREEN) {
            val homeViewModel: HomeViewModel = koinViewModel()

            LaunchedEffect(Unit) {
                if (!isDefaultHome()) {
                    requestDefaultHome()
                }
                homeViewModel.retrieveApplicationsList()
            }

            val applicationsList by homeViewModel.appsListState.collectAsState()
            val shortcutsList by homeViewModel.shortcutsListState.collectAsState()
            val clockTime by homeViewModel.clockState.collectAsState()

            HomeScreen(
                applicationsList,
                shortcutsList,
                clockTime,
                retrieveApplicationsList = {
                    homeViewModel.retrieveApplicationsList()
                },
                filterApplicationsList = {
                    homeViewModel.filterApplicationsList(it)
                },
                retrieveShortcuts = {
                    homeViewModel.retrieveShortcuts(it)
                },
                startShortcut = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        homeViewModel.startShortcut(it)
                    }
                },
                markNotification = { packageName, hasNotification ->
                    homeViewModel.markNotification(packageName, hasNotification)
                },
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
                requestDefaultHome,
                setWallpaper,
                enableNotificationAccess,
                onSettingsSaved,
            ) {
                navHostController.navigateUp()
            }
        }
    }
}