package io.github.fvrodas.jaml.ui.launcher.views

import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.core.data.repositories.ACTION_PIN_UNPIN_APP
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen18dp
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
            }
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimen8dp)
            )
        }
        shortcutsList?.second?.let { shortcuts ->
            items(shortcuts.size) { i ->
                shortcuts.elementAt(i).run {
                    when (this.packageName) {
                        ACTION_PIN_UNPIN_APP -> {
                            if (pinningMode && !shouldLetPinApps) return@run
                            Spacer(modifier = Modifier.height(dimen8dp))
                            ShortcutItem(
                                label = if (pinningMode) {
                                    stringResource(id = R.string.shortcut_pin)
                                } else {
                                    stringResource(id = R.string.shortcut_unpin)
                                },
                                bitmapIcon = null,
                                vectorIcon = Icons.Outlined.PushPin,
                                shouldHideShortcutIcons = shouldHideApplicationIcons
                            ) {
                                pinAppToTop(shortcutsList.first)
                            }
                        }

                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS -> {
                            ShortcutItem(
                                label = this.label,
                                bitmapIcon = null,
                                vectorIcon = Icons.Outlined.Info,
                                shouldHideShortcutIcons = shouldHideApplicationIcons
                            ) {
                                onApplicationInfoPressed(shortcutsList.first)
                                changeShortcutsVisibility(false)
                            }
                            Spacer(modifier = Modifier.height(dimen8dp))
                        }

                        else -> {
                            ShortcutItem(
                                label = label,
                                bitmapIcon = icon,
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
                    Spacer(modifier = Modifier.height(dimen8dp))
                }
            }
        }
    }
}