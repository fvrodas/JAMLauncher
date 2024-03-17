package io.github.fvrodas.jaml.features.settings.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.features.common.themes.dimen16dp
import io.github.fvrodas.jaml.features.common.themes.dimen8dp
import io.github.fvrodas.jaml.features.common.themes.dimenZero
import io.github.fvrodas.jaml.features.settings.presentation.viewmodels.LauncherSettings
import io.github.fvrodas.jaml.features.settings.presentation.viewmodels.SettingsViewModel

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

    DisposableEffect(Unit) {
        onDispose {
            settingsViewModel.saveSetting(LauncherSettings.DYNAMIC_COLOR_ENABLED, dynamicColor)
            onSettingsSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.settings_activity),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "", tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                backgroundColor = MaterialTheme.colorScheme.primary,
                elevation = dimenZero
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
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = stringResource(id = R.string.app_version_name),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium)
                    )
                )
            }
        }
    }
}
