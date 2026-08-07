package io.github.fvrodas.jaml.ui.launcher.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.ui.common.themes.dimen12dp
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen2dp
import io.github.fvrodas.jaml.ui.common.themes.dimen32dp

@Composable
fun GroupPickerDialog(
    groups: List<String>,
    onGroupSelected: (String) -> Unit,
    onCreateNew: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = dimen32dp, vertical = dimen32dp)
    ) {
        Text(
            text = stringResource(R.string.group_select_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
            modifier = Modifier.padding(bottom = dimen12dp),
        )
        Column(
            modifier = Modifier.clip(RoundedCornerShape(dimen16dp)),
            verticalArrangement = Arrangement.spacedBy(dimen2dp),
        ) {
            groups.forEach { group ->
                ShortcutItem(
                    label = group,
                    bitmapIcon = null,
                    vectorIcon = Icons.Default.Folder,
                ) { onGroupSelected(group) }
            }
            ShortcutItem(
                label = stringResource(R.string.group_create_new),
                bitmapIcon = null,
                vectorIcon = Icons.Default.Add,
            ) { onCreateNew() }
        }
    }
}
