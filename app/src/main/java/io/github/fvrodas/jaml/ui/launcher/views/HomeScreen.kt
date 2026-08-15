package io.github.fvrodas.jaml.ui.launcher.views

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.core.common.utils.BitmapUtils
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.models.LauncherEntry
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.common.themes.dimen64dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp
import io.github.fvrodas.jaml.ui.launcher.viewmodels.ApplicationSheetState
import kotlin.math.abs

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    sharedTransitionLayout: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    state: ApplicationSheetState?,
    shouldHideApplicationIcons: Boolean = false,
    shouldDisplayThemeIcons: Boolean = false,
    pinnedAlignment: Int = R.string.alignment_center,
    listOfShortcuts: Pair<LauncherEntry, Set<PackageInfo.ShortcutInfo>>? = null,
    retrieveShortcuts: (PackageInfo) -> Unit = {},
    startShortcut: (PackageInfo.ShortcutInfo) -> Unit = {},
    onPinToggle: (LauncherEntry) -> Unit = {},
    onApplicationPressed: (PackageInfo) -> Unit,
    displayAppList: (Boolean) -> Unit,
) {
    val pinnedList = remember(state?.pinnedApplications) {
        state?.pinnedApplications?.toList() ?: emptyList()
    }
    val currentPinnedList by rememberUpdatedState(pinnedList)
    val currentOnApplicationPressed by rememberUpdatedState(onApplicationPressed)
    val currentDisplayAppList by rememberUpdatedState(displayAppList)
    val currentRetrieveShortcuts by rememberUpdatedState(retrieveShortcuts)
    val currentStartShortcut by rememberUpdatedState(startShortcut)
    val currentOnPinToggle by rememberUpdatedState(onPinToggle)

    val overlayAlignment = when (pinnedAlignment) {
        R.string.alignment_left -> Alignment.CenterStart
        R.string.alignment_right -> Alignment.CenterEnd
        else -> Alignment.Center
    }

    // ── Home panel overlay state ───────────────────────────────────────────────
    var isOverlayActive by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val itemCenterYMap = remember { hashMapOf<Int, Float>() }
    var outerBoxCoords: LayoutCoordinates? by remember { mutableStateOf(null) }

    // ── Shortcut overlay state ─────────────────────────────────────────────────
    var isShortcutOverlayActive by remember { mutableStateOf(false) }
    var shortcutEntry by remember { mutableStateOf<LauncherEntry?>(null) }
    var shortcutSelectedIndex by remember { mutableStateOf<Int?>(null) }
    val shortcutItemCenterYMap = remember { hashMapOf<Int, Float>() }

    val motoToDrawerString = stringResource(R.string.shortcut_unpin)
    val moveToHomeString = stringResource(R.string.shortcut_pin)

    val shortcutOverlayActions = remember(listOfShortcuts, shortcutEntry) {
        val entry = shortcutEntry ?: return@remember emptyList()
        val actions = mutableListOf<OverlayAction>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            listOfShortcuts
                ?.takeIf { it.first.packageInfo.packageName == entry.packageInfo.packageName }
                ?.second
                ?.forEach { shortcut ->
                    actions += OverlayAction(
                        label = shortcut.label,
                        bitmapIcon = shortcut.icon,
                        action = { currentStartShortcut(shortcut) },
                    )
                }
        }
        actions += OverlayAction(
            label = if (entry.movedToHome) motoToDrawerString else moveToHomeString,
            vectorIcon = if (entry.movedToHome) Icons.Outlined.Apps else Icons.Outlined.Home,
            action = { currentOnPinToggle(entry) },
        )
        actions
    }
    val currentShortcutOverlayActions by rememberUpdatedState(shortcutOverlayActions)

    fun dismissShortcutOverlay() {
        isShortcutOverlayActive = false
        shortcutEntry = null
        shortcutSelectedIndex = null
        shortcutItemCenterYMap.clear()
    }

    fun dismiss() {
        isOverlayActive = false
        selectedIndex = null
    }

    // Drives the arc indicator and auto-transition after the full 2-second dwell
    val timeoutProgress = remember { Animatable(0f) }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != null) {
            timeoutProgress.snapTo(0f)
            timeoutProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = HOME_SHORTCUT_TIMEOUT_MS.toInt(),
                    easing = LinearEasing,
                ),
            )
            currentPinnedList.getOrNull(selectedIndex ?: return@LaunchedEffect)?.let { entry ->
                shortcutEntry = entry
                currentRetrieveShortcuts(entry.packageInfo)
                shortcutItemCenterYMap.clear()
                isShortcutOverlayActive = true
            }
            dismiss()
        } else {
            timeoutProgress.snapTo(0f)
        }
    }

    with(sharedTransitionLayout) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { outerBoxCoords = it }
                // Swipe-up + shortcut overlay tracking (pre-pass intercept)
                .pointerInput(Unit) {
                    val thresholdPx = MAX_SELECT_DISTANCE_DP * density
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val anyPressed = event.changes.any { it.pressed }
                            val change = event.changes.firstOrNull() ?: continue

                            if (isShortcutOverlayActive) {
                                if (anyPressed) {
                                    val coords = outerBoxCoords
                                    if (coords != null && shortcutItemCenterYMap.isNotEmpty()) {
                                        val fingerWindowY = coords.localToWindow(change.position).y
                                        val nearest = shortcutItemCenterYMap.entries
                                            .minByOrNull { abs(fingerWindowY - it.value) }
                                        shortcutSelectedIndex = if (nearest != null &&
                                            abs(fingerWindowY - nearest.value) <= thresholdPx
                                        ) nearest.key else null
                                    }
                                } else {
                                    val idx = shortcutSelectedIndex
                                    if (idx != null) {
                                        change.consume()
                                        currentShortcutOverlayActions.getOrNull(idx)?.action?.invoke()
                                    }
                                    dismissShortcutOverlay()
                                }
                            } else if (!isOverlayActive && anyPressed) {
                                if (change.previousPosition.y - change.position.y > MIN_DISPLACEMENT) {
                                    currentDisplayAppList(true)
                                }
                            }
                        }
                    }
                }
                // Long-press + drag for home panel overlay
                .pointerInput(Unit) {
                    val thresholdPx = MAX_SELECT_DISTANCE_DP * density
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            if (currentPinnedList.isNotEmpty()) {
                                itemCenterYMap.clear()
                                isOverlayActive = true
                                selectedIndex = null
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            if (!isOverlayActive || itemCenterYMap.isEmpty()) return@detectDragGesturesAfterLongPress
                            val coords = outerBoxCoords ?: return@detectDragGesturesAfterLongPress
                            val fingerWindowY = coords.localToWindow(change.position).y
                            val nearest = itemCenterYMap.entries.minByOrNull { abs(fingerWindowY - it.value) }
                            selectedIndex = if (nearest != null && abs(fingerWindowY - nearest.value) <= thresholdPx) {
                                nearest.key
                            } else {
                                null
                            }
                        },
                        onDragEnd = {
                            // Shortcut overlay took over — its PointerEventPass.Initial handler
                            // already fired and consumed the release event
                            if (!isShortcutOverlayActive) {
                                selectedIndex?.let { idx ->
                                    currentPinnedList.getOrNull(idx)?.let {
                                        currentOnApplicationPressed(it.packageInfo)
                                    }
                                }
                                dismiss()
                            }
                        },
                        onDragCancel = {
                            dismissShortcutOverlay()
                            dismiss()
                        }
                    )
                }
        ) {
            // Arrow button — always visible at bottom center
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .systemBarsPadding()
                    .navigationBarsPadding()
                    .padding(bottom = dimen8dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(dimen64dp)
                        .background(MaterialTheme.colorScheme.background),
                    onClick = { displayAppList(true) },
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = null,
                        modifier = Modifier
                            .sharedElement(
                                rememberSharedContentState("arrow"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            )
                            .size(dimen48dp),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }

            AnimatedVisibility(
                visible = isOverlayActive,
                enter = fadeIn(animationSpec = tween(OVERLAY_ANIM_DURATION)),
                exit = fadeOut(animationSpec = tween(OVERLAY_ANIM_DURATION))
            ) {
                HomePanelOverlay(
                    pinnedApps = currentPinnedList,
                    selectedIndex = selectedIndex,
                    alignment = overlayAlignment,
                    shouldHideIcons = shouldHideApplicationIcons,
                    shouldDisplayThemeIcons = shouldDisplayThemeIcons,
                    timeoutProgress = timeoutProgress.value,
                    onItemCenterYChanged = { index, centerY ->
                        itemCenterYMap[index] = centerY
                    }
                )
            }

            AnimatedVisibility(
                visible = isShortcutOverlayActive,
                enter = fadeIn(animationSpec = tween(OVERLAY_ANIM_DURATION)),
                exit = fadeOut(animationSpec = tween(OVERLAY_ANIM_DURATION)),
            ) {
                val headerIcon = remember(shortcutEntry?.packageInfo?.packageName) {
                    shortcutEntry?.let { BitmapUtils.loadIconForPackage(it.packageInfo.packageName) }
                }
                ActionsOverlay(
                    items = shortcutOverlayActions,
                    selectedIndex = shortcutSelectedIndex,
                    shouldHideIcons = shouldHideApplicationIcons,
                    headerLabel = shortcutEntry?.packageInfo?.label,
                    headerIcon = headerIcon,
                    onItemCenterYChanged = { index, centerY ->
                        shortcutItemCenterYMap[index] = centerY
                    },
                    onItemClicked = { index ->
                        shortcutOverlayActions.getOrNull(index)?.action?.invoke()
                        dismissShortcutOverlay()
                    },
                )
            }
        }
    }
}

internal const val MIN_DISPLACEMENT = 10f
internal const val MAX_SELECT_DISTANCE_DP = 56f
private const val OVERLAY_ANIM_DURATION = 150
private const val HOME_SHORTCUT_TIMEOUT_MS = 2_000L
