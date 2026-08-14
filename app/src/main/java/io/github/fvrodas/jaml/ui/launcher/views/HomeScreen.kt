package io.github.fvrodas.jaml.ui.launcher.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import io.github.fvrodas.jaml.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.common.themes.dimen64dp
import io.github.fvrodas.jaml.ui.launcher.viewmodels.ApplicationSheetState
import kotlinx.coroutines.delay
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
    toggleListVisibility: () -> Unit,
    onApplicationPressed: (PackageInfo) -> Unit,
    displayAppList: (Boolean) -> Unit,
) {
    val pinnedList = remember(state?.pinnedApplications) {
        state?.pinnedApplications?.toList() ?: emptyList()
    }
    val currentPinnedList by rememberUpdatedState(pinnedList)
    val currentOnApplicationPressed by rememberUpdatedState(onApplicationPressed)

    val overlayAlignment = when (pinnedAlignment) {
        R.string.alignment_left -> Alignment.CenterStart
        R.string.alignment_right -> Alignment.CenterEnd
        else -> Alignment.Center
    }

    var isOverlayActive by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var dwellProgress by remember { mutableStateOf(0f) }

    // Window-coordinate center Y for each item in the overlay
    val itemCenterYMap = remember { hashMapOf<Int, Float>() }
    var outerBoxCoords: LayoutCoordinates? by remember { mutableStateOf(null) }

    // Dwell timer: fires app launch after DWELL_DURATION_MS of holding on an item
    LaunchedEffect(selectedIndex, isOverlayActive) {
        dwellProgress = 0f
        if (selectedIndex == null || !isOverlayActive) return@LaunchedEffect
        val capturedIndex = selectedIndex!!
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < DWELL_DURATION_MS) {
            dwellProgress = (System.currentTimeMillis() - start) / DWELL_DURATION_MS.toFloat()
            delay(16L)
        }
        isOverlayActive = false
        dwellProgress = 0f
        currentPinnedList.getOrNull(capturedIndex)?.let {
            currentOnApplicationPressed(it.packageInfo)
        }
    }

    fun dismiss() {
        isOverlayActive = false
        selectedIndex = null
        dwellProgress = 0f
    }

    with(sharedTransitionLayout) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { outerBoxCoords = it }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (!isOverlayActive && event.changes.any { it.pressed }) {
                                val change = event.changes.first()
                                if (change.previousPosition.y - change.position.y > MIN_DISPLACEMENT) {
                                    displayAppList(true)
                                }
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            if (currentPinnedList.isNotEmpty()) {
                                itemCenterYMap.clear()
                                isOverlayActive = true
                                selectedIndex = null
                                dwellProgress = 0f
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            if (!isOverlayActive || itemCenterYMap.isEmpty()) return@detectDragGesturesAfterLongPress
                            val coords = outerBoxCoords ?: return@detectDragGesturesAfterLongPress
                            val fingerWindowY = coords.localToWindow(change.position).y
                            val nearest = itemCenterYMap.entries.minByOrNull { abs(fingerWindowY - it.value) }
                            selectedIndex = nearest?.key
                        },
                        onDragEnd = { dismiss() },
                        onDragCancel = { dismiss() }
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
                    .padding(bottom = dimen64dp),
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
                enter = fadeIn(animationSpec = tween(150)),
                exit = fadeOut(animationSpec = tween(150))
            ) {
                PinnedAppsOverlay(
                    pinnedApps = currentPinnedList,
                    selectedIndex = selectedIndex,
                    dwellProgress = dwellProgress,
                    alignment = overlayAlignment,
                    shouldHideIcons = shouldHideApplicationIcons,
                    shouldDisplayThemeIcons = shouldDisplayThemeIcons,
                    onItemCenterYChanged = { index, centerY ->
                        itemCenterYMap[index] = centerY
                    }
                )
            }
        }
    }
}

internal const val MIN_DISPLACEMENT = 10f
internal const val DWELL_DURATION_MS = 1000L
