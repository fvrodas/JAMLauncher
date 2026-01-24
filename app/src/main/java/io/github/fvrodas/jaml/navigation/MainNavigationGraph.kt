package io.github.fvrodas.jaml.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.fvrodas.jaml.framework.LauncherEventBus
import io.github.fvrodas.jaml.framework.LauncherEventListener
import io.github.fvrodas.jaml.ui.common.interfaces.LauncherActions
import io.github.fvrodas.jaml.ui.common.interfaces.SettingsActions
import io.github.fvrodas.jaml.ui.common.settings.LauncherPreferences
import io.github.fvrodas.jaml.ui.launcher.LauncherScreen
import io.github.fvrodas.jaml.ui.launcher.viewmodels.HomeViewModel
import io.github.fvrodas.jaml.ui.settings.SettingsScreen
import org.koin.androidx.compose.koinViewModel

@Suppress("LongParameterList")
@Composable
fun HomeNavigationGraph(
    navHostController: NavHostController,
    launcherSettings: LauncherPreferences,
    launcherActions: LauncherActions,
    settingsActions: SettingsActions,
    onSettingsSaved: (LauncherPreferences) -> Unit,
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
                    hasNotification: Boolean,
                    notificationTitle: String?
                ) {
                    homeViewModel.markNotification(packageName, hasNotification, notificationTitle)
                }
            }

            LaunchedEffect(Unit) {
                if (!settingsActions.isDefaultHome()) {
                    settingsActions.setAsDefaultHome()
                }
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
                    homeViewModel.startShortcut(it)
                },
                openLauncherSettings = {
                    navHostController.navigate(Routes.SETTINGS_SCREEN)
                },
                launcherActions = launcherActions
            )
        }

        composable(
            Routes.SETTINGS_SCREEN,
            enterTransition = { fadeIn() + slideInVertically(initialOffsetY = { it / 2 }) },
            exitTransition = { slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut() }
        ) {
            SettingsScreen(
                launcherSettings,
                settingsActions,
                onSettingsSaved
            ) {
                navHostController.popBackStack(route = Routes.HOME_SCREEN, inclusive = false)
            }
        }
    }
}
