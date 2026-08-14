package io.github.fvrodas.jaml.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.core.common.utils.BitmapUtils
import io.github.fvrodas.jaml.ui.common.models.LauncherEntry
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen18dp
import io.github.fvrodas.jaml.ui.common.themes.dimen2dp
import io.github.fvrodas.jaml.ui.common.themes.dimen36dp
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp
import io.github.fvrodas.jaml.ui.launcher.viewmodels.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePanelAppsOrderScreen(
    homeViewModel: HomeViewModel,
    onBackPressed: () -> Unit,
) {
    val appState by homeViewModel.applicationsState.collectAsStateWithLifecycle()
    var localPinnedApps by remember { mutableStateOf<List<LauncherEntry>>(emptyList()) }

    LaunchedEffect(Unit) {
        homeViewModel.retrieveApplicationsList()
    }

    LaunchedEffect(appState.pinnedApplications) {
        if (localPinnedApps.isEmpty()) {
            localPinnedApps = appState.pinnedApplications.toList()
        }
    }

    fun move(from: Int, to: Int) {
        if (to < 0 || to > localPinnedApps.lastIndex) return
        localPinnedApps = localPinnedApps.toMutableList().apply {
            val item = removeAt(from)
            add(to, item)
        }
        homeViewModel.reorderPinnedApps(localPinnedApps.map { it.packageInfo.packageName })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.pinned_order_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                colors = TopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                    subtitleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) { paddingValues ->
        if (localPinnedApps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(dimen16dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.pinned_order_empty),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = dimen16dp, vertical = dimen8dp),
                verticalArrangement = Arrangement.spacedBy(dimen8dp)
            ) {
                itemsIndexed(localPinnedApps, key = { _, e -> e.packageInfo.packageName }) { index, entry ->
                    HomeItemOrderRow(
                        entry = entry,
                        canMoveUp = index > 0,
                        canMoveDown = index < localPinnedApps.lastIndex,
                        onMoveUp = { move(index, index - 1) },
                        onMoveDown = { move(index, index + 1) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeItemOrderRow(
    entry: LauncherEntry,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
) {
    val icon = remember(entry.packageInfo.packageName) {
        BitmapUtils.loadIconForPackage(entry.packageInfo.packageName)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimen16dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = dimen16dp)
            .padding(vertical = dimen8dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = entry.packageInfo.label,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(dimen36dp)
                    .shadow(dimen2dp, RoundedCornerShape(dimen18dp))
            )
            Spacer(modifier = Modifier.width(dimen16dp))
        }
        Text(
            text = entry.packageInfo.label,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        IconButton(
            onClick = onMoveUp,
            enabled = canMoveUp,
            modifier = Modifier.size(dimen48dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowUp,
                contentDescription = null,
                tint = if (canMoveUp) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
        IconButton(
            onClick = onMoveDown,
            enabled = canMoveDown,
            modifier = Modifier.size(dimen48dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = if (canMoveDown) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}
