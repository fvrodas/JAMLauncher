package io.github.fvrodas.jaml.ui.launcher

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.framework.LauncherEventBus
import io.github.fvrodas.jaml.framework.LauncherEventListener
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.common.themes.dimen4dp
import io.github.fvrodas.jaml.ui.launcher.views.ApplicationsSheet
import io.github.fvrodas.jaml.ui.launcher.views.ShortcutsList

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LauncherScreen(
    listOfApplications: Set<PackageInfo>,
    listOfShortcuts: Pair<PackageInfo, Set<PackageInfo.ShortcutInfo>>?,
    shouldHideApplicationIcons: Boolean = false,
    clockTime: String,
    retrieveApplicationsList: () -> Unit = {},
    searchApplications: (String) -> Unit = {},
    retrieveShortcuts: (String) -> Unit = {},
    openShortcut: (PackageInfo.ShortcutInfo) -> Unit = {},
    markNotification: (packageName: String, hasNotification: Boolean) -> Unit = { _, _ -> },
    openLauncherSettings: () -> Unit = {},
    openApplicationInfo: (PackageInfo) -> Unit = {},
    openApplication: (PackageInfo) -> Unit = {}
) {
    val shortcutsBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var shouldDisplayShortcutsList by remember {
        mutableStateOf(false)
    }

    var shouldDisplayAppList by remember {
        mutableStateOf(false)
    }

    val launcherEventListener = object : LauncherEventListener {
        override fun onPackageChanged() {
            retrieveApplicationsList()
        }

        override fun onNotificationChanged(
            packageName: String?,
            hasNotification: Boolean
        ) {
            markNotification(packageName ?: "", hasNotification)
        }

    }

    LaunchedEffect(Unit) {
        shortcutsBottomSheetState.hide()
        LauncherEventBus.registerListener(launcherEventListener)
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
                                listOfApplications.toList(),
                                shouldHideApplicationIcons,
                                this@SharedTransitionLayout,
                                this@AnimatedContent,
                                toggleListVisibility = {
                                    shouldDisplayAppList = !shouldDisplayAppList
                                },
                                changeShortcutVisibility = { shouldShow ->
                                    shouldDisplayShortcutsList = shouldShow
                                },
                                openLauncherSettings,
                                onApplicationPressed = openApplication,
                                onApplicationLongPressed = retrieveShortcuts
                            ) { searchApplications(it) }
                        }
                    } else {
                        Home(
                            clockTime,
                            this@SharedTransitionLayout,
                            this@AnimatedContent
                        ) {
                            shouldDisplayAppList = it
                        }
                    }
                },
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                            slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(220, delayMillis = 90)
                            ))
                        .togetherWith(
                            fadeOut(animationSpec = tween(90))
                        )
                }
            )
        }

        if (shouldDisplayShortcutsList) {
            ModalBottomSheet(
                containerColor = MaterialTheme.colorScheme.background,
                dragHandle = null,
                onDismissRequest = {
                    shouldDisplayShortcutsList = false
                },
                sheetState = shortcutsBottomSheetState
            ) {
                ShortcutsList(
                    listOfShortcuts,
                    changeShortcutsVisibility = {
                        shouldDisplayShortcutsList = false
                    },
                    startShortcut = openShortcut,
                    onApplicationInfoPressed = openApplicationInfo
                )
            }
        }

        BackHandler {
            shouldDisplayAppList = false
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Home(
    clockTime: String,
    sharedTransitionLayout: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    displayAppList: (Boolean) -> Unit
) {
    var startOffset: Offset = Offset.Zero

    with(sharedTransitionLayout) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            startOffset = it
                        },
                        onDragEnd = {},
                        onDragCancel = {
                            displayAppList(false)
                        }
                    ) { change, _ ->
                        displayAppList(change.position.y < startOffset.y)
                    }
                }
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = null,
                        modifier = Modifier
                            .sharedElement(
                                rememberSharedContentState("arrow"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .padding(dimen4dp)
                            .size(dimen48dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

