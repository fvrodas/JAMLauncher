package io.github.fvrodas.jaml.features.settings.presentation.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Launcher settings") }) }
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(it)
            .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row {
                Text(text = "General", style = MaterialTheme.typography.headlineSmall)
            }
            SettingItem(title = "Notification Dot", description = "Enable Notification Access") {
                
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Text(text = "Appearance", style = MaterialTheme.typography.headlineSmall)
            }
            SettingItem(title = "Wallpaper", description = "Set wallpaper") {

            }
            Spacer(modifier = Modifier.height(24.dp))
            Row {
                Text(text = "About", style = MaterialTheme.typography.headlineSmall)
            }
            SettingItem(title = "View on Github", description = "Some link") {

            }
        }
    }
}

@Composable
fun SettingItem(title: String, description: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable { }
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Text(text = description, style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.medium)
            ))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}
