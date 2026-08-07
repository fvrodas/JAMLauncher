package io.github.fvrodas.jaml.ui.launcher.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.ui.common.models.AppGroup
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp

@Composable
fun GroupPickerDialog(
    groups: List<AppGroup>,
    onGroupSelected: (String) -> Unit,
    onCreateNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.group_select_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                groups.forEach { group ->
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onGroupSelected(group.name) },
                    ) {
                        Text(
                            text = group.name,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onCreateNew,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(dimen8dp))
                        Text(stringResource(R.string.group_create_new))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.group_cancel))
            }
        },
    )
}
