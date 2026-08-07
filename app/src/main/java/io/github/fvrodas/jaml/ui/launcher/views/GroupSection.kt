package io.github.fvrodas.jaml.ui.launcher.views

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.core.common.utils.BitmapUtils
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.models.AppGroup
import io.github.fvrodas.jaml.ui.common.models.LauncherEntry
import io.github.fvrodas.jaml.ui.common.themes.dimen1dp
import io.github.fvrodas.jaml.ui.common.themes.dimen12dp
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen24dp
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.common.themes.dimen4dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupSection(
    group: AppGroup,
    apps: List<LauncherEntry>,
    isExpanded: Boolean,
    shouldHideApplicationIcons: Boolean,
    shouldDisplayThemeIcons: Boolean,
    onToggle: () -> Unit,
    onAppPressed: (PackageInfo) -> Unit,
    onAppLongPressed: (PackageInfo) -> Unit,
    onRenameGroup: (String) -> Unit,
    onDeleteGroup: () -> Unit,
) {
    var showContextMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimen48dp)
                .combinedClickable(
                    onClick = onToggle,
                    onLongClick = { showContextMenu = true },
                    indication = ripple(color = MaterialTheme.colorScheme.primary),
                    interactionSource = remember { MutableInteractionSource() },
                )
                .padding(horizontal = dimen16dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.FolderOpen
                              else Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimen24dp),
            )
            Spacer(modifier = Modifier.width(dimen8dp))
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                ),
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = apps.size.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                ),
            )
        }

        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.group_rename)) },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null)
                },
                onClick = {
                    showContextMenu = false
                    showRenameDialog = true
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.group_delete)) },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null)
                },
                onClick = {
                    showContextMenu = false
                    onDeleteGroup()
                },
            )
        }
    }

    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically(
            animationSpec = tween(220),
            expandFrom = Alignment.Top,
        ) + fadeIn(animationSpec = tween(220)),
        exit = shrinkVertically(
            animationSpec = tween(180),
            shrinkTowards = Alignment.Top,
        ) + fadeOut(animationSpec = tween(180)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            // Vertical guideline positioned under the folder icon centre (16dp + 12dp = 28dp)
            Spacer(modifier = Modifier.width(dimen16dp + dimen12dp))
            Box(
                modifier = Modifier
                    .width(dimen1dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
            )
            // Apps indented to align with the group name (gap = 48dp − 28dp − 1dp ≈ 20dp)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = dimen12dp + dimen8dp),
                verticalArrangement = Arrangement.spacedBy(dimen4dp),
            ) {
                apps.forEach { item ->
                    ApplicationItem(
                        label = item.packageInfo.label,
                        iconBitmap = if (shouldHideApplicationIcons) null
                                     else BitmapUtils.loadIconForPackage(
                                         item.packageInfo.packageName,
                                         shouldDisplayThemeIcons,
                                     ),
                        hasNotification = item.hasNotification,
                        notificationText = item.notificationTitle,
                        onApplicationLongPressed = { _ ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                coroutineScope.launch {
                                    onAppLongPressed(item.packageInfo)
                                }
                            }
                        },
                        onApplicationPressed = {
                            coroutineScope.launch {
                                onAppPressed(item.packageInfo)
                            }
                        },
                    )
                }
            }
        }
    }

    if (showRenameDialog) {
        GroupNameDialog(
            title = stringResource(R.string.group_rename_title),
            initialName = group.name,
            onConfirm = { newName ->
                onRenameGroup(newName)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false },
        )
    }
}
