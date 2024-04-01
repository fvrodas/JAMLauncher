package io.github.fvrodas.jaml.ui.launcher.composables

import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Dialog
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.framework.receivers.CommunicationChannel
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen36dp
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.launcher.viewmodels.AppsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appsViewModel: AppsViewModel,
    onSettingsPressed: () -> Unit = {},
    onApplicationInfoPressed: (AppInfo) -> Unit = {},
    onApplicationPressed: (AppInfo) -> Unit = {}
) {

    val coroutineScope = rememberCoroutineScope()

    var displayShortcuts by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        appsViewModel.retrieveApplicationsList()
    }

    CommunicationChannel.onPackageChangedReceived = {
        appsViewModel.retrieveApplicationsList()
    }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            density = LocalDensity.current,
            initialValue = SheetValue.PartiallyExpanded,
            skipPartiallyExpanded = false
        )
    )

    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.launch {
                bottomSheetScaffoldState.bottomSheetState.partialExpand()
            }
        }
    }

    val applicationsList by appsViewModel.appsListState.collectAsState()
    val shortcutsList by appsViewModel.shortcutsListState.collectAsState()

    BottomSheetScaffold(
        containerColor = Color.Transparent,
        scaffoldState = bottomSheetScaffoldState,
        sheetDragHandle = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimen48dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(
                            dimen36dp
                        )
                )
            }
        },
        sheetContent = {
            ApplicationsSheet(
                applicationsList.toList(),
                onSettingsPressed,
                onApplicationPressed = {
                    coroutineScope.launch {
                        onApplicationPressed.invoke(it)
                        bottomSheetScaffoldState.bottomSheetState.partialExpand()
                    }
                },
                onApplicationLongPressed = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        coroutineScope.launch {
                            appsViewModel.retrieveShortcuts(it.packageName)
                            displayShortcuts = true
                        }
                    }
                }
            ) { appsViewModel.filterApplicationsList(it) }
        }, sheetPeekHeight = dimen48dp
    ) {
        AnimatedVisibility(visible = displayShortcuts) {
            Dialog(onDismissRequest = {
                displayShortcuts = false
            }) {
                Card {
                    LazyColumn(
                        modifier = Modifier.padding(dimen16dp)
                    ) {
                        shortcutsList?.second?.let { shortcuts ->
                            items(shortcuts.size) { i ->
                                shortcuts.elementAt(i).run {
                                    ShortcutItem(label = label, icon = icon) {
                                        if (this.packageName == Settings.ACTION_APPLICATION_DETAILS_SETTINGS) {
                                            shortcutsList?.first?.let {
                                                onApplicationInfoPressed(it)
                                            }
                                        } else {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                                coroutineScope.launch {
                                                    appsViewModel.startShortcut(this@run)
                                                }
                                            }
                                        }
                                        displayShortcuts = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}