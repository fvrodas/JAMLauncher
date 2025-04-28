package io.github.fvrodas.jaml.ui.launcher.views

import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp
import kotlinx.coroutines.launch

@Composable
fun ShortcutsList(
    shortcutsList: Pair<PackageInfo, Set<PackageInfo.ShortcutInfo>>?,
    changeShortcutsVisibility: (Boolean) -> Unit,
    startShortcut: (PackageInfo.ShortcutInfo) -> Unit = {},
    onApplicationInfoPressed: (PackageInfo) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .padding(dimen16dp)
    ) {
        item {
            Text(
                text = shortcutsList?.first?.label ?: "",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimen8dp)
            )
        }
        shortcutsList?.second?.let { shortcuts ->
            items(shortcuts.size) { i ->
                shortcuts.elementAt(i).run {
                    ShortcutItem(label = label, icon = icon) {
                        if (this.packageName == Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {
                            onApplicationInfoPressed(shortcutsList.first)
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                coroutineScope.launch {
                                    startShortcut(this@run)
                                }
                            }
                        }
                        changeShortcutsVisibility(false)
                    }
                }
            }
        }
    }
}