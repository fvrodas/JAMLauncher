package io.github.fvrodas.jaml.ui.launcher

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.interfaces.LauncherActions
import io.github.fvrodas.jaml.ui.launcher.viewmodels.ApplicationSheetState
import io.github.fvrodas.jaml.ui.launcher.views.ApplicationsSheet
import io.github.fvrodas.jaml.ui.launcher.views.HomeScreen
import io.github.fvrodas.jaml.ui.launcher.views.ShortcutsList

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LauncherScreen(
    applicationSheetState: ApplicationSheetState,
    listOfShortcuts: Pair<PackageInfo, Set<PackageInfo.ShortcutInfo>>?,
    shouldHideApplicationIcons: Boolean = false,
    searchApplications: (String) -> Unit = {},
    retrieveShortcuts: (PackageInfo) -> Unit = {},
    pinToTop: (PackageInfo) -> Unit = {},
    openShortcut: (PackageInfo.ShortcutInfo) -> Unit = {},
    openLauncherSettings: () -> Unit = {},
    launcherActions: LauncherActions
) {
    val shortcutsBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var sheetState by rememberSaveable(stateSaver = ApplicationSheetState.Saver) {
        mutableStateOf(applicationSheetState)
    }

    var shouldDisplayShortcutsList by remember {
        mutableStateOf(false)
    }

    var shortcutListPinningMode by remember {
        mutableStateOf(false)
    }

    var shouldDisplayAppList by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        shortcutsBottomSheetState.hide()
    }

    LaunchedEffect(applicationSheetState) {
        sheetState = applicationSheetState
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        shouldDisplayAppList = false
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { _ ->
        /** Experimental Status Bar Tint **/
        val height = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        AnimatedVisibility(
            visible = !shouldDisplayAppList,
            enter = slideInVertically(),
            exit = slideOutVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .background(
                        MaterialTheme.colorScheme.background.copy(alpha = .25f)
                    )
            )
        }
        /** End of Experimental Status Bar Tint **/
        SharedTransitionLayout {
            AnimatedContent(
                targetState = shouldDisplayAppList,
                label = "home",
                content = { targetState ->

                    if (targetState) {
                        with(this@SharedTransitionLayout) {
                            ApplicationsSheet(
                                sheetState,
                                shouldHideApplicationIcons,
                                this@SharedTransitionLayout,
                                this@AnimatedContent,
                                toggleListVisibility = {
                                    shouldDisplayAppList = !shouldDisplayAppList
                                },
                                changeShortcutVisibility = { shouldShow, pinningMode ->
                                    shouldDisplayShortcutsList = shouldShow
                                    shortcutListPinningMode = pinningMode
                                },
                                openLauncherSettings,
                                onApplicationPressed = launcherActions::openApplication,
                                onApplicationLongPressed = retrieveShortcuts,
                                performWebSearch = launcherActions::performWebSearch
                            ) { searchApplications(it) }
                        }
                    } else {
                        HomeScreen(
                            this@SharedTransitionLayout,
                            this@AnimatedContent,
                            sheetState,
                            shouldHideApplicationIcons,
                            toggleListVisibility = {
                                shouldDisplayAppList = !shouldDisplayAppList
                            },
                            changeShortcutVisibility = { shouldShow, pinningMode ->
                                shouldDisplayShortcutsList = shouldShow
                                shortcutListPinningMode = pinningMode
                            },
                            onApplicationPressed = launcherActions::openApplication,
                            onApplicationLongPressed = retrieveShortcuts
                        ) {
                            shouldDisplayAppList = it
                        }
                    }
                },
                transitionSpec = {
                    (fadeIn(
                        animationSpec = tween(
                            ANIMATION_DURATION,
                            delayMillis = DELAY_DURATION
                        )
                    ) +
                            slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(
                                    ANIMATION_DURATION,
                                    delayMillis = DELAY_DURATION
                                )
                            ))
                        .togetherWith(
                            fadeOut(animationSpec = tween(DELAY_DURATION))
                        )
                }
            )
        }

        if (shouldDisplayShortcutsList) {
            ModalBottomSheet(
                containerColor = MaterialTheme.colorScheme.background,
                onDismissRequest = {
                    shouldDisplayShortcutsList = false
                },
                sheetState = shortcutsBottomSheetState
            ) {
                ShortcutsList(
                    listOfShortcuts,
                    shouldHideApplicationIcons,
                    sheetState.canPinApps,
                    shortcutListPinningMode,
                    changeShortcutsVisibility = {
                        shouldDisplayShortcutsList = false
                    },
                    startShortcut = {
                        shouldDisplayShortcutsList = false
                        openShortcut(it)
                    },
                    pinAppToTop = {
                        shouldDisplayShortcutsList = false
                        pinToTop(it)
                    },
                    onApplicationInfoPressed = launcherActions::openApplicationInfo
                )
            }
        }

        BackHandler {
            shouldDisplayAppList = false
        }
    }
}

internal const val ANIMATION_DURATION = 220
internal const val DELAY_DURATION = 90
