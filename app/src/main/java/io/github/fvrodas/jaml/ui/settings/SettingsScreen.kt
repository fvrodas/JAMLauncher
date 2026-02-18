package io.github.fvrodas.jaml.ui.settings

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.core.domain.entities.IconConfig
import io.github.fvrodas.jaml.ui.common.interfaces.SettingsActions
import io.github.fvrodas.jaml.ui.common.settings.LauncherPreferences
import io.github.fvrodas.jaml.ui.common.themes.JamlColorScheme
import io.github.fvrodas.jaml.ui.common.themes.JamlTheme
import io.github.fvrodas.jaml.ui.common.themes.colorSchemeByName
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp
import io.github.fvrodas.jaml.ui.common.themes.launcherThemeByName
import io.github.fvrodas.jaml.ui.settings.views.SettingItem
import io.github.fvrodas.jaml.ui.settings.views.SettingOptionsDialog
import io.github.fvrodas.jaml.ui.settings.views.SettingSwitch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    launcherPreferences: LauncherPreferences,
    settingsActions: SettingsActions,
    saveSettings: (LauncherPreferences) -> Unit = {},
    clearIconsAndReload: (IconConfig) -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    val projectUrl = stringResource(id = R.string.about_github_url)

    var selectedLauncherTheme: Int by remember {
        mutableIntStateOf(launcherPreferences.launcherTheme)
    }

    var isDynamicColorEnabled: Boolean by remember {
        mutableStateOf(launcherPreferences.isDynamicColorEnabled)
    }
    var selectedColorScheme: Int by remember {
        mutableIntStateOf(launcherPreferences.launcherColorScheme)
    }

    var shouldHideApplicationIcons: Boolean by remember {
        mutableStateOf(launcherPreferences.shouldHideApplicationIcons)
    }

    var shouldUseThemedIcons: Boolean by remember {
        mutableStateOf(launcherPreferences.shouldUseThemedIcons)
    }

    var showThemeSelection by remember {
        mutableStateOf(false)
    }

    var showColorSchemeSelection by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(
        isDynamicColorEnabled,
        selectedColorScheme,
        selectedLauncherTheme,
        shouldHideApplicationIcons,
        shouldUseThemedIcons
    ) {
        saveSettings(
            LauncherPreferences(
                selectedLauncherTheme,
                isDynamicColorEnabled,
                selectedColorScheme,
                shouldHideApplicationIcons,
                shouldUseThemedIcons
            )
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.settings_activity)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "",
                        )
                    }
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                    subtitleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(it)
                .padding(horizontal = dimen16dp, vertical = dimen16dp),
            verticalArrangement = Arrangement.spacedBy(dimen8dp)
        ) {
            Row {
                Text(
                    text = stringResource(id = R.string.menu_other_settings),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
            if (!settingsActions.isDefaultHome()) {
                SettingItem(
                    title = stringResource(id = R.string.menu_default_launcher),
                    description = stringResource(id = R.string.summary_default_launcher)
                ) {
                    settingsActions.setAsDefaultHome()
                }
            }
            SettingItem(
                title = stringResource(id = R.string.menu_notification_access),
                description = stringResource(id = R.string.summary_notification_access)
            ) {
                settingsActions.enableNotificationAccess()
            }
            Spacer(modifier = Modifier.height(dimen16dp))
            Row {
                Text(
                    text = stringResource(id = R.string.menu_appearance),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
            SettingItem(
                title = stringResource(id = R.string.menu_wallpaper),
                description = stringResource(id = R.string.summary_wallpaper)
            ) {
                settingsActions.setWallpaper()
            }

            SettingItem(
                title = stringResource(id = R.string.menu_theme),
                description = stringResource(selectedLauncherTheme)
            ) {
                showThemeSelection = true
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                SettingSwitch(
                    title = stringResource(id = R.string.menu_dynamic_colors),
                    description = stringResource(id = R.string.summary_dynamic_colors),
                    value = isDynamicColorEnabled
                ) { checked ->
                    isDynamicColorEnabled = checked
                }
            }
            AnimatedVisibility(visible = !isDynamicColorEnabled) {
                SettingItem(
                    title = stringResource(id = R.string.menu_color_scheme),
                    description = stringResource(selectedColorScheme)
                ) {
                    showColorSchemeSelection = true
                }
            }
            SettingSwitch(
                title = stringResource(id = R.string.menu_hide_app_icons),
                description = stringResource(id = R.string.summary_hide_app_icons),
                value = shouldHideApplicationIcons
            ) { checked ->
                shouldHideApplicationIcons = checked
            }
            AnimatedVisibility(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !shouldHideApplicationIcons) {
                SettingSwitch(
                    title = stringResource(id = R.string.menu_themed_icons),
                    description = stringResource(id = R.string.summary_themed_icons),
                    value = shouldUseThemedIcons,
                    badgeContent = stringResource(id = R.string.badge_experimental),

                ) { checked ->
                    shouldUseThemedIcons = checked
                }
            }
            Spacer(modifier = Modifier.height(dimen16dp))
            Row {
                Text(
                    text = stringResource(id = R.string.menu_about),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
            SettingItem(
                title = stringResource(id = R.string.about_neutral),
                description = projectUrl
            ) {
                settingsActions.openWebPage(projectUrl.toUri())
                onBackPressed()
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.display_app_version_name),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                )
            }
            SettingOptionsDialog(
                showIf = showThemeSelection,
                title = stringResource(id = R.string.menu_theme),
                options = launcherThemeByName.entries.toList().map { item -> item.key },
                defaultValue = selectedLauncherTheme,
                onDismiss = { showThemeSelection = false },
            ) { selected ->
                selectedLauncherTheme = selected
            }
            SettingOptionsDialog(
                showIf = showColorSchemeSelection,
                title = stringResource(id = R.string.menu_color_scheme),
                options = colorSchemeByName.entries.toList().map { item -> item.key },
                defaultValue = selectedColorScheme,
                onDismiss = { showColorSchemeSelection = false },
            ) { selected ->
                selectedColorScheme = selected
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    JamlTheme(
        colorScheme = JamlColorScheme.Default,
        isInDarkMode = false,
        isDynamicColorsEnabled = false
    ) {
        SettingsScreen(
            launcherPreferences = LauncherPreferences(),
            settingsActions = object : SettingsActions {
                override fun isDefaultHome(): Boolean = false
                override fun setAsDefaultHome() {
                    /** No - Op **/
                }

                override fun setWallpaper() {
                    /** No - Op **/
                }

                override fun enableNotificationAccess() {
                    /** No - Op **/
                }

                override fun openWebPage(url: android.net.Uri) {
                    /** No - Op **/
                }
            },
            saveSettings = {},
            onBackPressed = {}
        )
    }
}
