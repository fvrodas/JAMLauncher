package io.github.fvrodas.jaml.ui.settings.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp

@Composable
fun SettingOptionsDialog(
    showIf: Boolean,
    title: String,
    options: List<String>,
    defaultValue: String = "",
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit
) {
    if (showIf) {
        Dialog(onDismissRequest = {
            onDismiss()
        }) {
            LazyColumn(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(dimen16dp)
                    .fillMaxWidth(0.9F)
            ) {
                item {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(dimen16dp))
                }
                items(options.size) { index ->
                    val optionName = options[index]
                    Row(
                        modifier = Modifier
                            .height(dimen48dp)
                            .fillMaxWidth()
                            .clickable {
                                onSelected(optionName)
                                onDismiss()
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = optionName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        if (optionName == defaultValue) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(dimen16dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
