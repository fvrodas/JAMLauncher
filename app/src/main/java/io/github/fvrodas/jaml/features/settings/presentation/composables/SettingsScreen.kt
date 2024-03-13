package io.github.fvrodas.jaml.features.settings.presentation.composables

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.features.common.themes.dimen16dp
import io.github.fvrodas.jaml.features.common.themes.dimen8dp
import io.github.fvrodas.jaml.features.common.themes.dimenZero

@Composable
fun SettingsScreen(
    isDefaultHome: () -> Boolean = { false },
    setAsDefaultHome: () -> Unit = {},
    setWallpaper: () -> Unit = {},
    enableNotificationAccess: () -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
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
                    style = MaterialTheme.typography.headlineSmall.copy(
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
                    style = MaterialTheme.typography.headlineSmall.copy(
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
            Spacer(modifier = Modifier.height(dimen16dp))
            Row {
                Text(
                    text = stringResource(id = R.string.menu_about),
                    style = MaterialTheme.typography.headlineSmall.copy(
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

@Composable
fun SettingItem(title: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable {
                onClick()
            }
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.padding(horizontal = dimen8dp, vertical = dimen8dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground
            ))
            Text(
                text = description, style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = ContentAlpha.medium)
                )
            )
        }
    }
}

@Preview(showSystemUi = false)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen {}
}
