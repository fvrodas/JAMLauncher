package io.github.fvrodas.jaml.navigation

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.framework.LauncherEventBus
import io.github.fvrodas.jaml.framework.LauncherEventListener
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

            val launcherEventListener = object : LauncherEventListener {
                override fun onPackageChanged() {
                    homeViewModel.retrieveApplicationsList()
                }

                override fun onNotificationChanged(
                    packageName: String?,
                    hasNotification: Boolean
                ) {
                    homeViewModel.markNotification(packageName, hasNotification)
                }
            }

            LaunchedEffect(Unit) {
                if (!isDefaultHome()) {
                    requestDefaultHome()
                }
                homeViewModel.retrieveApplicationsList()
                LauncherEventBus.registerListener(launcherEventListener)
            }

            val applicationListState by homeViewModel.applicationsState.collectAsStateWithLifecycle()
            val shortcutsList by homeViewModel.shortcutsListState.collectAsStateWithLifecycle()

            LauncherScreen(
                applicationListState,
                shortcutsList,
                launcherSettings.shouldHideApplicationIcons,
                searchApplications = {
                    homeViewModel.filterApplicationsList(it)
                },
                retrieveShortcuts = {
                    homeViewModel.retrieveShortcuts(it)
                },
                pinToTop = {
                    homeViewModel.toggleAppPinning(it)
                },
                openShortcut = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        homeViewModel.startShortcut(it)
                    }
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
                navHostController.popBackStack()
            }
        }
    }
}