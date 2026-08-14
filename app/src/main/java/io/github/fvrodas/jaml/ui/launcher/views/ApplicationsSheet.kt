package io.github.fvrodas.jaml.ui.launcher.views

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.FolderOff
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.core.common.utils.BitmapUtils
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.models.LauncherEntry
import io.github.fvrodas.jaml.ui.common.models.LauncherEntry.Companion.DEFAULT_GROUP
import io.github.fvrodas.jaml.ui.common.models.toLauncherEntry
import io.github.fvrodas.jaml.ui.common.themes.JamlColorScheme
import io.github.fvrodas.jaml.ui.common.themes.JamlTheme
import io.github.fvrodas.jaml.ui.common.themes.dimen12dp
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen24dp
import io.github.fvrodas.jaml.ui.common.themes.dimen2dp
import io.github.fvrodas.jaml.ui.common.themes.dimen32dp
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.common.themes.dimen4dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp
import io.github.fvrodas.jaml.ui.launcher.viewmodels.ApplicationSheetState
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ApplicationsSheet(
    state: ApplicationSheetState,
    shouldHideApplicationIcons: Boolean = false,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    listOfShortcuts: Pair<LauncherEntry, Set<PackageInfo.ShortcutInfo>>?,
    canPinApps: Boolean,
    toggleListVisibility: () -> Unit,
    onSettingsPressed: () -> Unit,
    onApplicationPressed: (PackageInfo) -> Unit,
    retrieveShortcuts: (PackageInfo) -> Unit,
    startShortcut: (PackageInfo.ShortcutInfo) -> Unit,
    pinAppToTop: (LauncherEntry) -> Unit,
    onApplicationInfoPressed: (PackageInfo) -> Unit,
    onAddToGroup: (LauncherEntry) -> Unit,
    onRemoveFromGroup: (LauncherEntry) -> Unit,
    performWebSearch: (String) -> Unit,
    onSearchApplication: (String) -> Unit,
    onRenameGroup: (String, String) -> Unit,
    onDeleteGroup: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val appList = remember(state.applicationsList) { state.applicationsList.toList() }
    val groupsList = state.groups
    val groupedApps = state.groupedApplications
    val allEntries = remember(state) {
        (state.applicationsList + state.groupedApplications.values.flatten()).distinctBy {
            it.packageInfo.packageName
        }
    }

    val currentAllEntries by rememberUpdatedState(allEntries)
    val currentRetrieveShortcuts by rememberUpdatedState(retrieveShortcuts)
    val currentStartShortcut by rememberUpdatedState(startShortcut)
    val currentPinAppToTop by rememberUpdatedState(pinAppToTop)
    val currentOnApplicationInfoPressed by rememberUpdatedState(onApplicationInfoPressed)
    val currentOnAddToGroup by rememberUpdatedState(onAddToGroup)
    val currentOnRemoveFromGroup by rememberUpdatedState(onRemoveFromGroup)
    val currentOnDeleteGroup by rememberUpdatedState(onDeleteGroup)

    val pagerState =
        rememberPagerState(pageCount = { if (groupsList.isEmpty()) 1 else groupsList.size + 1 })
    val lazyListState = rememberLazyListState()

    var searchFieldValue by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameTargetGroup by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val mutableInteractionSource = remember { MutableInteractionSource() }
    val focusState = mutableInteractionSource.collectIsFocusedAsState()

    val usePager = groupsList.isNotEmpty() && searchFieldValue.isEmpty()

    var isListScrolledToTop by remember { mutableStateOf(true) }

    // ── Shortcut overlay state ─────────────────────────────────────────────────
    var isShortcutOverlayActive by remember { mutableStateOf(false) }
    var shortcutSelectedIndex by remember { mutableStateOf<Int?>(null) }
    var longPressedEntry by remember { mutableStateOf<LauncherEntry?>(null) }
    val appItemCenterYMap = remember { hashMapOf<String, Float>() }
    val shortcutItemCenterYMap = remember { hashMapOf<Int, Float>() }
    var outerBoxCoords: LayoutCoordinates? by remember { mutableStateOf(null) }

    // ── Group overlay state ────────────────────────────────────────────────────
    var isGroupOverlayActive by remember { mutableStateOf(false) }
    var groupSelectedIndex by remember { mutableStateOf<Int?>(null) }
    var groupOverlayActions by remember { mutableStateOf<List<OverlayAction>>(emptyList()) }
    var groupOverlayLabel by remember { mutableStateOf<String?>(null) }
    val groupItemCenterYMap = remember { hashMapOf<Int, Float>() }
    val renameLabel = stringResource(R.string.group_rename)
    val deleteLabel = stringResource(R.string.group_delete)

    // Build shortcut overlay items reactively from listOfShortcuts + longPressedEntry
    val shortcutOverlayActions = remember(listOfShortcuts, longPressedEntry, canPinApps) {
        val entry = longPressedEntry ?: return@remember emptyList()
        val actions = mutableListOf<OverlayAction>()
        // App shortcuts first (async — may be empty initially)
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
        // Static actions
        val isPinned = entry.movedToHome
        if (!isPinned && canPinApps || isPinned) {
            actions += OverlayAction(
                label = if (isPinned) "Unpin" else "Pin to home",
                vectorIcon = if (isPinned) Icons.Outlined.Apps else Icons.Outlined.Home,
                action = { currentPinAppToTop(entry) },
            )
        }
        actions += OverlayAction(
            label = "App info",
            vectorIcon = Icons.Outlined.Info,
            action = { currentOnApplicationInfoPressed(entry.packageInfo) },
        )
        val isInGroup = entry.group != null && entry.group != DEFAULT_GROUP
        if (isInGroup) {
            actions += OverlayAction(
                label = "Remove from group",
                vectorIcon = Icons.Outlined.FolderOff,
                action = { currentOnRemoveFromGroup(entry) },
            )
        }
        actions += OverlayAction(
            label = if (isInGroup) "Move to group" else "Add to group",
            vectorIcon = Icons.Outlined.CreateNewFolder,
            action = { currentOnAddToGroup(entry) },
        )
        actions
    }
    val currentShortcutOverlayActions by rememberUpdatedState(shortcutOverlayActions)

    fun dismissShortcutOverlay() {
        isShortcutOverlayActive = false
        shortcutSelectedIndex = null
        longPressedEntry = null
        shortcutItemCenterYMap.clear()
    }

    fun dismissGroupOverlay() {
        isGroupOverlayActive = false
        groupSelectedIndex = null
        groupOverlayLabel = null
        groupItemCenterYMap.clear()
    }

    fun showGroupOverlay(groupName: String) {
        groupOverlayLabel = groupName
        groupOverlayActions = listOf(
            OverlayAction(
                label = renameLabel,
                vectorIcon = Icons.Default.Edit,
                action = {
                    renameTargetGroup = groupName
                    showRenameDialog = true
                    dismissGroupOverlay()
                },
            ),
            OverlayAction(
                label = deleteLabel,
                vectorIcon = Icons.Default.Delete,
                action = {
                    currentOnDeleteGroup(groupName)
                    dismissGroupOverlay()
                    coroutineScope.launch { pagerState.scrollToPage(0) }
                },
            ),
        )
        isGroupOverlayActive = true
    }

    LaunchedEffect(pagerState.currentPage) {
        appItemCenterYMap.clear()
    }

    LaunchedEffect(focusState.value) {
        if (focusState.value) keyboardController?.show() else keyboardController?.hide()
    }

    DisposableEffect(Unit) {
        onDispose {
            searchFieldValue = ""
            onSearchApplication(searchFieldValue)
        }
    }

    with(sharedTransitionScope) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { outerBoxCoords = it }
                // Swipe-down + group overlay tracking (pre-pass intercept)
                .pointerInput(Unit) {
                    val thresholdPx = MAX_APP_SELECT_DISTANCE_DP * density
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            val anyPressed = event.changes.any { it.pressed }
                            val change = event.changes.firstOrNull() ?: continue

                            if (isGroupOverlayActive) {
                                if (anyPressed) {
                                    val coords = outerBoxCoords
                                    if (coords != null && groupItemCenterYMap.isNotEmpty()) {
                                        val fingerWindowY = coords.localToWindow(change.position).y
                                        val nearest = groupItemCenterYMap.entries
                                            .minByOrNull { abs(fingerWindowY - it.value) }
                                        groupSelectedIndex = if (nearest != null &&
                                            abs(fingerWindowY - nearest.value) <= thresholdPx
                                        ) nearest.key else null
                                    }
                                } else {
                                    // Finger released: execute selected action or dismiss
                                    val idx = groupSelectedIndex
                                    if (idx != null) {
                                        change.consume()
                                        groupOverlayActions.getOrNull(idx)?.action?.invoke()
                                    } else {
                                        dismissGroupOverlay()
                                    }
                                }
                            } else if (!isShortcutOverlayActive && anyPressed) {
                                if (isListScrolledToTop &&
                                    change.position.y - change.previousPosition.y > DRAWER_SWIPE_DOWN_THRESHOLD
                                ) {
                                    toggleListVisibility()
                                }
                            }
                        }
                    }
                }
                // Long-press + drag for shortcut overlay
                .pointerInput(Unit) {
                    val thresholdPx = MAX_APP_SELECT_DISTANCE_DP * density
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            if (isGroupOverlayActive) return@detectDragGesturesAfterLongPress
                            val coords = outerBoxCoords ?: return@detectDragGesturesAfterLongPress
                            val startWindowY = coords.localToWindow(offset).y
                            val nearest = appItemCenterYMap.entries
                                .minByOrNull { abs(startWindowY - it.value) }
                            if (nearest != null && abs(startWindowY - nearest.value) <= thresholdPx) {
                                val entry = currentAllEntries.find {
                                    it.packageInfo.packageName == nearest.key
                                } ?: return@detectDragGesturesAfterLongPress
                                longPressedEntry = entry
                                currentRetrieveShortcuts(entry.packageInfo)
                                shortcutItemCenterYMap.clear()
                                isShortcutOverlayActive = true
                                shortcutSelectedIndex = null
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            if (!isShortcutOverlayActive || shortcutItemCenterYMap.isEmpty()) {
                                return@detectDragGesturesAfterLongPress
                            }
                            val coords = outerBoxCoords ?: return@detectDragGesturesAfterLongPress
                            val fingerWindowY = coords.localToWindow(change.position).y
                            val nearest = shortcutItemCenterYMap.entries
                                .minByOrNull { abs(fingerWindowY - it.value) }
                            shortcutSelectedIndex = if (nearest != null &&
                                abs(fingerWindowY - nearest.value) <= thresholdPx
                            ) nearest.key else null
                        },
                        onDragEnd = {
                            shortcutSelectedIndex?.let { idx ->
                                currentShortcutOverlayActions.getOrNull(idx)?.action?.invoke()
                            }
                            dismissShortcutOverlay()
                        },
                        onDragCancel = { dismissShortcutOverlay() },
                    )
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(dimen8dp),
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier
                            .sharedElement(
                                rememberSharedContentState("arrow"),
                                animatedVisibilityScope = animatedVisibilityScope,
                            )
                            .padding(dimen4dp)
                            .size(dimen48dp),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
                OutlinedTextField(
                    value = searchFieldValue,
                    textStyle = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    onValueChange = {
                        searchFieldValue = it
                        onSearchApplication(searchFieldValue)
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "",
                            modifier = Modifier.clickable {
                                searchFieldValue = ""
                                onSearchApplication(searchFieldValue)
                                keyboardController?.hide()
                            },
                        )
                    },
                    shape = RoundedCornerShape(dimen16dp),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .focusable(interactionSource = mutableInteractionSource)
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxWidth()
                        .padding(horizontal = dimen16dp),
                )

                if (usePager) {
                    PrimaryScrollableTabRow(
                        modifier = Modifier.padding(vertical = dimen8dp),
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        edgePadding = dimen8dp,
                        divider = {},
                        indicator = {},
                    ) {
                        GroupTab(
                            text = stringResource(R.string.group_all_apps),
                            selected = pagerState.currentPage == 0,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(0) }
                            },
                        )
                        groupsList.forEachIndexed { i, group ->
                            GroupTab(
                                text = group,
                                selected = pagerState.currentPage == i + 1,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(i + 1)
                                    }
                                },
                                onLongClick = { showGroupOverlay(group) },
                            )
                        }
                    }
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.weight(1f),
                    ) { page ->
                        val pageApps = if (page == 0) appList
                        else groupedApps[groupsList[page - 1]] ?: emptyList()
                        AppPage(
                            apps = pageApps,
                            searchFieldValue = "",
                            shouldHideApplicationIcons = shouldHideApplicationIcons,
                            onApplicationPressed = onApplicationPressed,
                            toggleListVisibility = toggleListVisibility,
                            performWebSearch = performWebSearch,
                            onSettingsPressed = onSettingsPressed,
                            coroutineScope = coroutineScope,
                            shouldDisplaySettings = page == 0,
                            onItemPositioned = { packageName, centerY ->
                                if (page == pagerState.currentPage) {
                                    appItemCenterYMap[packageName] = centerY
                                }
                            },
                            onScrollAtTop = { isListScrolledToTop = it },
                        )
                    }
                } else {
                    AppPage(
                        apps = appList,
                        lazyListState = lazyListState,
                        searchFieldValue = searchFieldValue,
                        shouldHideApplicationIcons = shouldHideApplicationIcons,
                        onApplicationPressed = onApplicationPressed,
                        toggleListVisibility = toggleListVisibility,
                        performWebSearch = performWebSearch,
                        onSettingsPressed = onSettingsPressed,
                        coroutineScope = coroutineScope,
                        onItemPositioned = { packageName, centerY ->
                            appItemCenterYMap[packageName] = centerY
                        },
                        onScrollAtTop = { isListScrolledToTop = it },
                    )
                }
            }

            // ── Shortcut overlay ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = isShortcutOverlayActive,
                enter = fadeIn(animationSpec = tween(OVERLAY_ANIM_DURATION)),
                exit = fadeOut(animationSpec = tween(OVERLAY_ANIM_DURATION)),
            ) {
                val headerIcon = remember(longPressedEntry?.packageInfo?.packageName) {
                    longPressedEntry?.let {
                        BitmapUtils.loadIconForPackage(it.packageInfo.packageName)
                    }
                }
                ActionsOverlay(
                    items = shortcutOverlayActions,
                    selectedIndex = shortcutSelectedIndex,
                    shouldHideIcons = shouldHideApplicationIcons,
                    headerLabel = longPressedEntry?.packageInfo?.label,
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

            // ── Group overlay ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = isGroupOverlayActive,
                enter = fadeIn(animationSpec = tween(OVERLAY_ANIM_DURATION)),
                exit = fadeOut(animationSpec = tween(OVERLAY_ANIM_DURATION)),
            ) {
                ActionsOverlay(
                    items = groupOverlayActions,
                    selectedIndex = groupSelectedIndex,
                    shouldHideIcons = shouldHideApplicationIcons,
                    headerLabel = groupOverlayLabel,
                    onDismiss = { dismissGroupOverlay() },
                    onItemCenterYChanged = { index, centerY ->
                        groupItemCenterYMap[index] = centerY
                    },
                    onItemClicked = { index ->
                        groupOverlayActions.getOrNull(index)?.action?.invoke()
                    },
                )
            }
        }
    }

    if (showRenameDialog && renameTargetGroup != null) {
        GroupNameDialog(
            title = stringResource(R.string.group_rename_title),
            initialName = renameTargetGroup!!,
            onConfirm = { newName ->
                onRenameGroup(renameTargetGroup!!, newName)
                showRenameDialog = false
                renameTargetGroup = null
            },
            onDismiss = {
                showRenameDialog = false
                renameTargetGroup = null
            },
        )
    }
}

@Composable
private fun AppPage(
    apps: List<LauncherEntry>,
    lazyListState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    searchFieldValue: String,
    shouldHideApplicationIcons: Boolean,
    onApplicationPressed: (PackageInfo) -> Unit,
    toggleListVisibility: () -> Unit,
    performWebSearch: (String) -> Unit,
    onSettingsPressed: () -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    shouldDisplaySettings: Boolean = true,
    onItemPositioned: (packageName: String, centerY: Float) -> Unit = { _, _ -> },
    onScrollAtTop: (Boolean) -> Unit = {},
) {
    LaunchedEffect(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset) {
        onScrollAtTop(lazyListState.firstVisibleItemIndex == 0 && lazyListState.firstVisibleItemScrollOffset == 0)
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .padding(horizontal = dimen16dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(dimen8dp),
    ) {
        items(apps, key = { "${it.packageInfo.packageName}+${it.packageInfo.label}" }) { item ->
            val groupLabel = if (searchFieldValue.isNotEmpty()) when {
                item.movedToHome -> stringResource(R.string.group_label_desktop)
                item.group != null -> item.group
                else -> null
            } else null
            ApplicationItem(
                label = item.packageInfo.label,
                notificationText = item.notificationTitle,
                searchText = searchFieldValue,
                groupLabel = groupLabel,
                iconBitmap = if (shouldHideApplicationIcons) null
                else BitmapUtils.loadIconForPackage(item.packageInfo.packageName),
                hasNotification = item.hasNotification,
                onGloballyPositioned = { centerY ->
                    onItemPositioned(item.packageInfo.packageName, centerY)
                },
                onApplicationPressed = {
                    coroutineScope.launch {
                        onApplicationPressed(item.packageInfo)
                        toggleListVisibility()
                    }
                },
            )
        }
        if (searchFieldValue.isNotEmpty()) {
            item {
                ApplicationItem(
                    label = "\"$searchFieldValue\" on the web...",
                    searchText = searchFieldValue,
                    iconBitmap = null,
                    iconVector = Icons.Default.Search,
                    hasNotification = false,
                    onApplicationLongPressed = null,
                    onApplicationPressed = {
                        performWebSearch(searchFieldValue)
                        coroutineScope.launch { toggleListVisibility() }
                    },
                )
            }
        }
        item {
            AnimatedVisibility(shouldDisplaySettings) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = dimen16dp)
                        .padding(bottom = dimen8dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    IconButton(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background),
                        onClick = { onSettingsPressed() },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = stringResource(R.string.settings_button),
                            modifier = Modifier.size(dimen24dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    Box(
        modifier = Modifier
            .height(dimen48dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary),
            )
            .padding(horizontal = dimen16dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = if (selected) MaterialTheme.colorScheme.secondary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Light,
            ),
        )
    }
}

internal const val DRAWER_SWIPE_DOWN_THRESHOLD = 10f
internal const val MAX_APP_SELECT_DISTANCE_DP = 56f
private const val OVERLAY_ANIM_DURATION = 150

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ApplicationsSheetPreview() {
    JamlTheme(
        colorScheme = JamlColorScheme.Nord,
        isInDarkMode = true,
        isDynamicColorsEnabled = false,
    ) { _ ->
        SharedTransitionLayout {
            AnimatedContent(targetState = true, label = "ApplicationsSheet") {
                ApplicationsSheet(
                    state = ApplicationSheetState(
                        applicationsList = setOf(
                            PackageInfo(
                                packageName = "com.android.settings",
                                label = "Settings",
                                key = "",
                            ).toLauncherEntry(),
                            PackageInfo(
                                packageName = "com.android.vending",
                                label = "Play Store",
                                key = "",
                            ).toLauncherEntry().copy(group = "Test"),
                        ),
                        groups = listOf("Test"),
                    ),
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent,
                    listOfShortcuts = null,
                    canPinApps = true,
                    toggleListVisibility = {},
                    onSettingsPressed = {},
                    onApplicationPressed = {},
                    retrieveShortcuts = {},
                    startShortcut = {},
                    pinAppToTop = {},
                    onApplicationInfoPressed = {},
                    onAddToGroup = {},
                    onRemoveFromGroup = {},
                    performWebSearch = {},
                    onSearchApplication = {},
                    onRenameGroup = { _, _ -> },
                    onDeleteGroup = {},
                )
            }
        }
    }
}
