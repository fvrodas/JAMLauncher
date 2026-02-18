package io.github.fvrodas.jaml.ui.settings.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.fvrodas.jaml.ui.common.themes.dimen4dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp

@Composable
fun SettingSwitch(
    title: String,
    description: String,
    value: Boolean = false,
    badgeContent: String? = null,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = dimen8dp, vertical = dimen8dp)
                .weight(1f)
        ) {
            Text(
                text = title, style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = description, style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f)
                )
            )
            badgeContent?.let {
                Badge(
                    modifier = Modifier.padding(top = dimen4dp),
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Text(text = it)
                }
            }
        }
        Switch(checked = value, onCheckedChange = onToggle)
    }
}
