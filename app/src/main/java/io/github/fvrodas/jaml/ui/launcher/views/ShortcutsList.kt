package io.github.fvrodas.jaml.ui.launcher.views

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.github.fvrodas.jaml.core.data.repositories.ACTION_PIN_UNPIN_APP
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.themes.dimen12dp
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen18dp
import io.github.fvrodas.jaml.ui.common.themes.dimen24dp
import io.github.fvrodas.jaml.ui.common.themes.dimen2dp
import io.github.fvrodas.jaml.ui.common.themes.dimen32dp
import io.github.fvrodas.jaml.ui.common.themes.dimen36dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp
import kotlinx.coroutines.launch

@Composable
fun ShortcutsList(
    shortcutsList: Pair<PackageInfo, Set<PackageInfo.ShortcutInfo>>?,
    shouldHideApplicationIcons: Boolean = false,
    shouldLetPinApps: Boolean = true,
    pinningMode: Boolean = true,
    changeShortcutsVisibility: (Boolean) -> Unit,
    startShortcut: (PackageInfo.ShortcutInfo) -> Unit = {},
    pinAppToTop: (PackageInfo) -> Unit = {},
    onApplicationInfoPressed: (PackageInfo) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .padding(horizontal = dimen32dp, vertical = dimen16dp)
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
                                .padding(start = dimen16dp)
                                .size(dimen36dp)
                                .shadow(dimen2dp, shape = RoundedCornerShape(dimen18dp)),
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
                if (shouldLetPinApps || !pinningMode) {
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        shortcutsList?.first?.let {
                            onApplicationInfoPressed(shortcutsList.first)
                            changeShortcutsVisibility(false)
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Info,
                            contentDescription = null,
                            modifier = Modifier.size(dimen24dp)
                        )
                    }
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
                    if (this.packageName == ACTION_PIN_UNPIN_APP) {
                        if (shortcuts.size > 1) {
                            Spacer(modifier = Modifier.height(dimen12dp))
                        }
                        ShortcutItem(
                            label = if (pinningMode) "Pin to Favorites" else "Unpin",
                            icon = icon,
                            shouldHideShortcutIcons = shouldHideApplicationIcons
                        ) {
                            pinAppToTop(shortcutsList.first)
                        }
                    } else {
                        ShortcutItem(
                            label = label,
                            icon = icon,
                            shouldHideShortcutIcons = shouldHideApplicationIcons
                        ) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                coroutineScope.launch {
                                    startShortcut(this@run)
                                    changeShortcutsVisibility(false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}