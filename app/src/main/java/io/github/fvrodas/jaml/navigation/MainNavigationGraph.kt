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
import io.github.fvrodas.jaml.ui.launcher.LauncherScreen
import io.github.fvrodas.jaml.ui.launcher.viewmodels.HomeViewModel
import io.github.fvrodas.jaml.ui.settings.SettingsScreen
import io.github.fvrodas.jaml.ui.settings.viewmodels.LauncherSettings
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeNavigationGraph(
    navHostController: NavHostController,
    launcherSettings: LauncherSettings,
    openApplication: (PackageInfo) -> Unit,
    openApplicationInfo: (PackageInfo) -> Unit,
    isDefaultHome: () -> Boolean,
    requestDefaultHome: () -> Unit,
    setWallpaper: () -> Unit,
    onSettingsSaved: (LauncherSettings) -> Unit,
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

            LauncherScreen(
                applicationsList,
                shortcutsList,
                launcherSettings.shouldHideApplicationIcons,
                retrieveApplicationsList = {
                    homeViewModel.retrieveApplicationsList()
                },
                searchApplications = {
                    homeViewModel.filterApplicationsList(it)
                },
                retrieveShortcuts = {
                    homeViewModel.retrieveShortcuts(it)
                },
                openShortcut = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        homeViewModel.startShortcut(it)
                    }
                },
                markNotification = { packageName, hasNotification ->
                    homeViewModel.markNotification(packageName, hasNotification)
                },
                openLauncherSettings = {
                    navHostController.navigate(Routes.SETTINGS_SCREEN)
                },
                openApplicationInfo = openApplicationInfo,
                openApplication = openApplication
            )
        }

        composable(Routes.SETTINGS_SCREEN) {
            SettingsScreen(
                launcherSettings,
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