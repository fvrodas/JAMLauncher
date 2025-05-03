package io.github.fvrodas.jaml.ui.launcher.views

import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen24dp
import io.github.fvrodas.jaml.ui.common.themes.dimen2dp
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp
import kotlinx.coroutines.launch

@Composable
fun ShortcutsList(
    shortcutsList: Pair<PackageInfo, Set<PackageInfo.ShortcutInfo>>?,
    shouldHideApplicationIcons: Boolean = false,
    changeShortcutsVisibility: (Boolean) -> Unit,
    startShortcut: (PackageInfo.ShortcutInfo) -> Unit = {},
    pinAppToTop: (PackageInfo) -> Unit = {},
    onApplicationInfoPressed: (PackageInfo) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .padding(dimen16dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!shouldHideApplicationIcons) {
                    shortcutsList?.first?.icon?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentScale = ContentScale.FillBounds,
                            contentDescription = "",
                            modifier = Modifier
                                .size(dimen48dp)
                                .shadow(dimen2dp, shape = RoundedCornerShape(dimen24dp)),
                        )
                        Spacer(modifier = Modifier.width(dimen16dp))
                    }
                }
                Text(
                    text = shortcutsList?.first?.label ?: "",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    shortcutsList?.first?.let {
                        pinAppToTop(it)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Star,
                        contentDescription = null,
                        modifier = Modifier.size(dimen24dp)
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimen8dp)
            )
        }
        shortcutsList?.second?.let { shortcuts ->
            items(shortcuts.size) { i ->
                shortcuts.elementAt(i).run {
                    ShortcutItem(
                        label = label,
                        icon = icon,
                        shouldHideShortcutIcons = shouldHideApplicationIcons
                    ) {
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