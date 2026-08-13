package io.github.fvrodas.jaml.ui.settings.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import io.github.fvrodas.jaml.ui.common.themes.dimen12dp
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
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
                .padding(vertical = dimen8dp)
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
                    Image(
                        imageVector = Icons.Rounded.WarningAmber,
                        contentDescription = Icons.Rounded.WarningAmber.name,
                        colorFilter = ColorFilter.tint(
                            MaterialTheme.colorScheme.onSecondary,
                            BlendMode.SrcAtop
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = dimen4dp)
                            .size(dimen12dp)
                    )
                }
            }
        }
        Switch(
            modifier = Modifier.padding(start = dimen16dp),
            checked = value,
            onCheckedChange = onToggle
        )
    }
}
