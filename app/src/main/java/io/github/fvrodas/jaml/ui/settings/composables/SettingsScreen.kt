package io.github.fvrodas.jaml.ui.settings.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp
import io.github.fvrodas.jaml.ui.common.themes.themesByName
import io.github.fvrodas.jaml.ui.settings.viewmodels.LauncherSettings
import io.github.fvrodas.jaml.ui.settings.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    isDefaultHome: () -> Boolean = { false },
    setAsDefaultHome: () -> Unit = {},
    setWallpaper: () -> Unit = {},
    enableNotificationAccess: () -> Unit = {},
    onSettingsSaved: () -> Unit,
    onBackPressed: () -> Unit = {}
) {
    var dynamicColor by remember {
        mutableStateOf(settingsViewModel.isDynamicColorEnabled)
    }

    var selectedTheme by remember {
        mutableStateOf(settingsViewModel.selectedThemeName)
    }

    var showDialog by remember {
        mutableStateOf(false)
    }

    DisposableEffect(Unit) {
        onDispose {
            settingsViewModel.saveSetting(LauncherSettings.DYNAMIC_COLOR_ENABLED, dynamicColor)
            settingsViewModel.saveSetting(LauncherSettings.SELECTED_THEME, selectedTheme)
            onSettingsSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
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
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
            if (!isDefaultHome()) {
                SettingItem(
                    title = stringResource(id = R.string.menu_default_launcher),
                    description = stringResource(id = R.string.summary_default_launcher)
                ) {
                    setAsDefaultHome()
                }
            }
            SettingItem(
                title = stringResource(id = R.string.menu_notification_access),
                description = stringResource(id = R.string.summary_notification_access)
            ) {
                enableNotificationAccess()
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
                setWallpaper()
            }
            SettingSwitch(
                title = stringResource(id = R.string.menu_dynamic_colors),
                description = stringResource(id = R.string.summary_dynamic_colors),
                value = dynamicColor
            ) { checked ->
                dynamicColor = checked
            }
            AnimatedVisibility(visible = !dynamicColor) {
                SettingItem(
                    title = stringResource(id = R.string.menu_theme),
                    description = selectedTheme
                ) {
                    showDialog = true
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
                title = stringResource(id = R.string.menu_about),
                description = stringResource(id = R.string.about_github_url)
            ) {

            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(id = R.string.app_version_name),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                )
            }
            SettingOptionsDialog(
                showIf = showDialog,
                title = stringResource(id = R.string.menu_theme),
                options = themesByName.entries.toList().map { item -> item.key },
                defaultValue = selectedTheme,
                onDismiss = { showDialog = false },
            ) { selected ->
                selectedTheme = selected
            }
        }
    }
}
