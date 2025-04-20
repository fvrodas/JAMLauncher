package io.github.fvrodas.jaml.ui.launcher.composables

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.framework.LauncherEventBus
import io.github.fvrodas.jaml.framework.LauncherEventListener
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen200dp
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.common.themes.dimen4dp
import io.github.fvrodas.jaml.ui.common.themes.dimen72dp

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    applicationsList: Set<PackageInfo>,
    shortcutsList: Pair<PackageInfo, Set<PackageInfo.ShortcutInfo>>?,
    clockTime: String,
    retrieveApplicationsList: () -> Unit = {},
    filterApplicationsList: (String) -> Unit = {},
    retrieveShortcuts: (String) -> Unit = {},
    startShortcut: (PackageInfo.ShortcutInfo) -> Unit = {},
    markNotification: (packageName: String, hasNotification: Boolean) -> Unit = { _, _ -> },
    onSettingsPressed: () -> Unit = {},
    onApplicationInfoPressed: (PackageInfo) -> Unit = {},
    onApplicationPressed: (PackageInfo) -> Unit = {}
) {

    var displayShortcuts by remember {
        mutableStateOf(false)
    }

    var displayList by remember {
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
        LauncherEventBus.registerListener(launcherEventListener)
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { _ ->
        SharedTransitionLayout {
            AnimatedContent(
                targetState = displayList,
                label = "home",
                content = { targetState ->
                    if (targetState) {
                        with(this@SharedTransitionLayout) {
                            ApplicationsSheet(
                                applicationsList.toList(),
                                this@SharedTransitionLayout,
                                this@AnimatedContent,
                                toggleListVisibility = { displayList = !displayList },
                                changeShortcutVisibility = { displayShortcuts = it },
                                onSettingsPressed,
                                onApplicationPressed = onApplicationPressed,
                                onApplicationLongPressed = retrieveShortcuts
                            ) { filterApplicationsList(it) }
                        }
                    } else {
                        HomeScreen(
                            clockTime,
                            this@SharedTransitionLayout,
                            this@AnimatedContent
                        ) {
                            displayList = it
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
                            slideOutVertically(
                                targetOffsetY = { it / 2 },
                                animationSpec = tween(90)
                            ) + fadeOut(animationSpec = tween(90))
                        )
                }
            )
        }

        BackHandler {
            displayList = false
        }

        AnimatedVisibility(
            visible = displayShortcuts,
            enter = slideInVertically(),
            exit = slideOutVertically()
        ) {
            Dialog(onDismissRequest = {
                displayShortcuts = false
            }) {
                ShortcutsList(
                    shortcutsList,
                    changeShortcutsVisibility = { displayShortcuts = it },
                    startShortcut = startShortcut,
                    onApplicationInfoPressed = onApplicationInfoPressed
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun HomeScreen(
    clockTime: String,
    sharedTransitionLayout: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    setListVisibility: (Boolean) -> Unit
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
                            setListVisibility(false)
                        }
                    ) { change, _ ->
                        setListVisibility(change.position.y < startOffset.y)
                    }
                }
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
                    modifier = Modifier.padding(
                        vertical = dimen72dp,
                        horizontal = dimen16dp
                    ),
                    text = clockTime,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
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

