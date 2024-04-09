package io.github.fvrodas.jaml.ui.launcher.composables

import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Dialog
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.framework.receivers.CommunicationChannel
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen200dp
import io.github.fvrodas.jaml.ui.common.themes.dimen36dp
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.common.themes.dimen56dp
import io.github.fvrodas.jaml.ui.common.themes.dimen64dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp
import io.github.fvrodas.jaml.ui.launcher.viewmodels.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onSettingsPressed: () -> Unit = {},
    onApplicationInfoPressed: (AppInfo) -> Unit = {},
    onApplicationPressed: (AppInfo) -> Unit = {}
) {

    val coroutineScope = rememberCoroutineScope()

    var displayShortcuts by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        homeViewModel.retrieveApplicationsList()
    }

    CommunicationChannel.onPackageChangedReceived = {
        homeViewModel.retrieveApplicationsList()
    }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            density = LocalDensity.current,
            initialValue = SheetValue.PartiallyExpanded,
            skipPartiallyExpanded = false,
            confirmValueChange = { stateValue ->
                stateValue != SheetValue.Hidden
            }
        )
    )

    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.launch {
                bottomSheetScaffoldState.bottomSheetState.partialExpand()
            }
        }
    }

    val applicationsList by homeViewModel.appsListState.collectAsState()
    val shortcutsList by homeViewModel.shortcutsListState.collectAsState()
    val clockTime by homeViewModel.clockState.collectAsState()

    BottomSheetScaffold(
        containerColor = Color.Transparent,
        scaffoldState = bottomSheetScaffoldState,
        sheetDragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .height(dimen56dp)
                    .fillMaxWidth()
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
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        },
        sheetContent = {
            ApplicationsSheet(
                bottomSheetScaffoldState.bottomSheetState.currentValue,
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
                            homeViewModel.retrieveShortcuts(it.packageName)
                            displayShortcuts = true
                        }
                    }
                }
            ) { homeViewModel.filterApplicationsList(it) }
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimen200dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.background,
                                Color.Transparent
                            )
                        )
                    )
            ) {
                Text(
                    modifier = Modifier.padding(vertical = dimen36dp, horizontal = dimen16dp),
                    text = clockTime, style = MaterialTheme.typography.headlineLarge.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
        AnimatedVisibility(
            visible = displayShortcuts,
            enter = slideInVertically(),
            exit = slideOutVertically()
        ) {
            Dialog(onDismissRequest = {
                displayShortcuts = false
            }) {
                Card {
                    LazyColumn(
                        modifier = Modifier.padding(dimen16dp)
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
                                            shortcutsList?.first?.let {
                                                onApplicationInfoPressed(it)
                                            }
                                        } else {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                                coroutineScope.launch {
                                                    homeViewModel.startShortcut(this@run)
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