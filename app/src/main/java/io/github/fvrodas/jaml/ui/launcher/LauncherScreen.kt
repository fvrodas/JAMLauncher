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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.interfaces.LauncherActions
import io.github.fvrodas.jaml.ui.common.models.LauncherEntry
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen32dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp
import io.github.fvrodas.jaml.ui.launcher.viewmodels.ApplicationSheetState
import io.github.fvrodas.jaml.ui.launcher.views.ApplicationsSheet
import io.github.fvrodas.jaml.ui.launcher.views.GroupNameDialog
import io.github.fvrodas.jaml.ui.launcher.views.GroupPickerDialog
import io.github.fvrodas.jaml.ui.launcher.views.HomeScreen
import io.github.fvrodas.jaml.ui.launcher.views.ShortcutsList

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LauncherScreen(
    applicationSheetState: ApplicationSheetState,
    listOfShortcuts: Pair<LauncherEntry, Set<PackageInfo.ShortcutInfo>>?,
    shouldHideApplicationIcons: Boolean = false,
    shouldDisplayThemeIcons: Boolean = false,
    pinnedAppsAlignment: Int = io.github.fvrodas.jaml.R.string.alignment_center,
    searchApplications: (String) -> Unit = {},
    retrieveShortcuts: (PackageInfo) -> Unit = {},
    pinToTop: (LauncherEntry) -> Unit = {},
    openShortcut: (PackageInfo.ShortcutInfo) -> Unit = {},
    openLauncherSettings: () -> Unit = {},
    createGroup: (String) -> Unit = {},
    renameGroup: (String, String) -> Unit = { _, _ -> },
    deleteGroup: (String) -> Unit = {},
    addAppToGroup: (LauncherEntry, String) -> Unit = { _, _ -> },
    removeAppFromGroup: (LauncherEntry) -> Unit = {},
    launcherActions: LauncherActions,
) {

    var sheetState by retain {
        mutableStateOf(applicationSheetState)
    }

    var shouldDisplayAppList by remember { mutableStateOf(false) }

    var shouldShowGroupPicker by remember { mutableStateOf(false) }
    var shouldShowGroupCreate by remember { mutableStateOf(false) }
    var groupPickerEntry by remember { mutableStateOf<LauncherEntry?>(null) }

    LaunchedEffect(applicationSheetState) {
        sheetState = applicationSheetState
    }

    val focusManager = LocalFocusManager.current
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        shouldDisplayAppList = false
        focusManager.clearFocus()
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { _ ->
        val height = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        AnimatedVisibility(
            visible = !shouldDisplayAppList,
            enter = slideInVertically(),
            exit = slideOutVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height + dimen8dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.background.copy(alpha = .35f),
                                MaterialTheme.colorScheme.background.copy(alpha = .25f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        SharedTransitionLayout {
            AnimatedContent(
                targetState = shouldDisplayAppList,
                label = "home",
                content = { targetState ->
                    if (targetState) {
                        with(this@SharedTransitionLayout) {
                            ApplicationsSheet(
                                state = sheetState,
                                shouldHideApplicationIcons = shouldHideApplicationIcons,
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@AnimatedContent,
                                listOfShortcuts = listOfShortcuts,
                                canPinApps = sheetState.canPinApps,
                                toggleListVisibility = {
                                    shouldDisplayAppList = !shouldDisplayAppList
                                },
                                onSettingsPressed = openLauncherSettings,
                                onApplicationPressed = launcherActions::openApplication,
                                retrieveShortcuts = retrieveShortcuts,
                                startShortcut = openShortcut,
                                pinAppToTop = pinToTop,
                                onApplicationInfoPressed = launcherActions::openApplicationInfo,
                                onAddToGroup = { entry ->
                                    groupPickerEntry = entry
                                    shouldShowGroupPicker = true
                                },
                                onRemoveFromGroup = removeAppFromGroup,
                                performWebSearch = launcherActions::performWebSearch,
                                onSearchApplication = { searchApplications(it) },
                                onRenameGroup = renameGroup,
                                onDeleteGroup = deleteGroup,
                            )
                        }
                    } else {
                        HomeScreen(
                            this@SharedTransitionLayout,
                            this@AnimatedContent,
                            sheetState,
                            shouldHideApplicationIcons,
                            shouldDisplayThemeIcons,
                            pinnedAlignment = pinnedAppsAlignment,
                            toggleListVisibility = {
                                shouldDisplayAppList = !shouldDisplayAppList
                            },
                            onApplicationPressed = launcherActions::openApplication,
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

        AnimatedVisibility(
            shouldShowGroupPicker,
            enter = slideInVertically(),
            exit = slideOutVertically()
        ) {
            Popup(
                alignment = Alignment.BottomCenter,
                onDismissRequest = {
                    shouldShowGroupPicker = false
                    groupPickerEntry = null
                },
                properties = PopupProperties(focusable = true)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
                        .pointerInput(Unit) {
                            detectTapGestures {
                                shouldShowGroupPicker = false
                                groupPickerEntry = null
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = dimen32dp, vertical = dimen16dp),
                        shape = RoundedCornerShape(dimen16dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = dimen8dp
                    ) {
                        GroupPickerDialog(
                            groups = sheetState.groups,
                            shouldHideApplicationIcons = shouldHideApplicationIcons,
                            onGroupSelected = { groupName ->
                                groupPickerEntry?.let { addAppToGroup(it, groupName) }
                                shouldShowGroupPicker = false
                                groupPickerEntry = null
                            },
                            onCreateNew = {
                                shouldShowGroupPicker = false
                                shouldShowGroupCreate = true
                            },
                        )
                    }
                }
            }
        }

        if (shouldShowGroupCreate) {
            GroupNameDialog(
                title = stringResource(R.string.group_create_title),
                onConfirm = { name ->
                    createGroup(name)
                    groupPickerEntry?.let { addAppToGroup(it, name) }
                    shouldShowGroupCreate = false
                    groupPickerEntry = null
                },
                onDismiss = {
                    shouldShowGroupCreate = false
                    groupPickerEntry = null
                },
            )
        }

        BackHandler {
            shouldDisplayAppList = false
        }
    }
}

internal const val ANIMATION_DURATION = 220
internal const val DELAY_DURATION = 90
