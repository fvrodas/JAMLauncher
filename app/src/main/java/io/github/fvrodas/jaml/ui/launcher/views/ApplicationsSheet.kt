package io.github.fvrodas.jaml.ui.launcher.views

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.core.common.utils.BitmapUtils
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun ApplicationsSheet(
    state: ApplicationSheetState,
    shouldHideApplicationIcons: Boolean = false,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    toggleListVisibility: () -> Unit,
    changeShortcutVisibility: (Boolean, Boolean) -> Unit,
    onSettingsPressed: () -> Unit,
    onApplicationPressed: (PackageInfo) -> Unit,
    onApplicationLongPressed: (PackageInfo) -> Unit,
    performWebSearch: (String) -> Unit,
    onSearchApplication: (String) -> Unit,
    onRenameGroup: (String, String) -> Unit,
    onDeleteGroup: (String) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val appList = remember(state.applicationsList) { state.applicationsList.toList() }
    val groupsList = state.groups
    val groupedApps = state.groupedApplications

    val pagerState = rememberPagerState(pageCount = { if (groupsList.isEmpty()) 1 else groupsList.size + 1 })
    val lazyListState = rememberLazyListState()

    var searchFieldValue by remember { mutableStateOf("") }
    var contextMenuGroup by remember { mutableStateOf<String?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameTargetGroup by remember { mutableStateOf<String?>(null) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusRequester = remember { FocusRequester() }
    val mutableInteractionSource = remember { MutableInteractionSource() }
    val focusState = mutableInteractionSource.collectIsFocusedAsState()

    var trackedDragAmount = 0f

    val usePager = groupsList.isNotEmpty() && searchFieldValue.isEmpty()

    LaunchedEffect(focusState.value) {
        if (focusState.value) {
            keyboardController?.show()
        } else {
            keyboardController?.hide()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            searchFieldValue = ""
            onSearchApplication(searchFieldValue)
        }
    }

    with(sharedTransitionScope) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = {},
                            onDragEnd = {
                                if (trackedDragAmount > 0) toggleListVisibility()
                            }
                        ) { _, dragAmount ->
                            trackedDragAmount = dragAmount
                        }
                    },
                verticalArrangement = Arrangement.spacedBy(dimen8dp)
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
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .padding(dimen4dp)
                            .size(dimen48dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                OutlinedTextField(
                    value = searchFieldValue,
                    textStyle = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    onValueChange = {
                        searchFieldValue = it
                        onSearchApplication.invoke(searchFieldValue)
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
                                onSearchApplication.invoke(searchFieldValue)
                                keyboardController?.hide()
                            }
                        )
                    },
                    shape = RoundedCornerShape(dimen16dp),
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .focusable(interactionSource = mutableInteractionSource)
                        .background(MaterialTheme.colorScheme.background)
                        .fillMaxWidth()
                        .padding(bottom = dimen8dp)
                        .padding(horizontal = dimen16dp),
                )

                if (usePager) {
                    PrimaryScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = dimen8dp,
                    ) {
                        GroupTab(
                            text = stringResource(R.string.group_all_apps),
                            selected = pagerState.currentPage == 0,
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                        )
                        groupsList.forEachIndexed { i, group ->
                            GroupTab(
                                text = group,
                                selected = pagerState.currentPage == i + 1,
                                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(i + 1) } },
                                onLongClick = { contextMenuGroup = group },
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
                            onApplicationLongPressed = onApplicationLongPressed,
                            changeShortcutVisibility = changeShortcutVisibility,
                            onApplicationPressed = onApplicationPressed,
                            toggleListVisibility = toggleListVisibility,
                            performWebSearch = performWebSearch,
                            onSettingsPressed = onSettingsPressed,
                            coroutineScope = coroutineScope,
                        )
                    }
                } else {
                    AppPage(
                        apps = appList,
                        lazyListState = lazyListState,
                        searchFieldValue = searchFieldValue,
                        shouldHideApplicationIcons = shouldHideApplicationIcons,
                        onApplicationLongPressed = onApplicationLongPressed,
                        changeShortcutVisibility = changeShortcutVisibility,
                        onApplicationPressed = onApplicationPressed,
                        toggleListVisibility = toggleListVisibility,
                        performWebSearch = performWebSearch,
                        onSettingsPressed = onSettingsPressed,
                        coroutineScope = coroutineScope,
                    )
                }
            }

            if (contextMenuGroup != null) {
                Popup(
                    alignment = Alignment.BottomCenter,
                    onDismissRequest = { contextMenuGroup = null },
                    properties = PopupProperties(focusable = true),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                            .pointerInput(Unit) {
                                detectTapGestures { contextMenuGroup = null }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            modifier = Modifier.padding(horizontal = dimen32dp, vertical = dimen16dp),
                            shape = RoundedCornerShape(dimen16dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = dimen8dp,
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = dimen32dp, vertical = dimen32dp)
                            ) {
                                Text(
                                    text = contextMenuGroup!!,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = MaterialTheme.colorScheme.onBackground,
                                    ),
                                    modifier = Modifier.padding(bottom = dimen12dp),
                                )
                                Column(
                                    modifier = Modifier.clip(RoundedCornerShape(dimen16dp)),
                                    verticalArrangement = Arrangement.spacedBy(dimen2dp),
                                ) {
                                    ShortcutItem(
                                        label = stringResource(R.string.group_rename),
                                        bitmapIcon = null,
                                        vectorIcon = Icons.Default.Edit,
                                        shouldHideShortcutIcons = shouldHideApplicationIcons,
                                    ) {
                                        renameTargetGroup = contextMenuGroup
                                        contextMenuGroup = null
                                        showRenameDialog = true
                                    }
                                    ShortcutItem(
                                        label = stringResource(R.string.group_delete),
                                        bitmapIcon = null,
                                        vectorIcon = Icons.Default.Delete,
                                        shouldHideShortcutIcons = shouldHideApplicationIcons,
                                    ) {
                                        onDeleteGroup(contextMenuGroup!!)
                                        contextMenuGroup = null
                                        coroutineScope.launch { pagerState.scrollToPage(0) }
                                    }
                                }
                            }
                        }
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
    }
}

@Composable
private fun AppPage(
    apps: List<io.github.fvrodas.jaml.ui.common.models.LauncherEntry>,
    lazyListState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    searchFieldValue: String,
    shouldHideApplicationIcons: Boolean,
    onApplicationLongPressed: (PackageInfo) -> Unit,
    changeShortcutVisibility: (Boolean, Boolean) -> Unit,
    onApplicationPressed: (PackageInfo) -> Unit,
    toggleListVisibility: () -> Unit,
    performWebSearch: (String) -> Unit,
    onSettingsPressed: () -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
) {
    LazyColumn(
        state = lazyListState,
        modifier = Modifier.padding(horizontal = dimen8dp).fillMaxSize(),
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
                onApplicationLongPressed = { isFavorite ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        coroutineScope.launch {
                            onApplicationLongPressed.invoke(item.packageInfo)
                            changeShortcutVisibility(true, !isFavorite)
                        }
                    }
                },
                onApplicationPressed = {
                    coroutineScope.launch {
                        onApplicationPressed.invoke(item.packageInfo)
                        toggleListVisibility()
                    }
                }
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
                    }
                )
            }
        }
        item {
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
                    onClick = { onSettingsPressed.invoke() }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.settings_button),
                        modifier = Modifier.size(dimen24dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
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
                color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
    }
}

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ApplicationsSheetPreview() {
    JamlTheme(
        colorScheme = JamlColorScheme.Default,
        isInDarkMode = isSystemInDarkTheme(),
        isDynamicColorsEnabled = false
    ) { _ ->
        SharedTransitionLayout {
            AnimatedContent(targetState = true, label = "ApplicationsSheet") {
                ApplicationsSheet(
                    state = ApplicationSheetState(
                        applicationsList = setOf(
                            PackageInfo(packageName = "com.android.settings", label = "Settings", key = "").toLauncherEntry(),
                            PackageInfo(packageName = "com.android.vending", label = "Play Store", key = "").toLauncherEntry().copy(group = "Test"),
                            PackageInfo(packageName = "com.google.android.apps.maps", label = "Maps", key = "").toLauncherEntry(),
                        )
                    ),
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent,
                    toggleListVisibility = {},
                    changeShortcutVisibility = { _, _ -> },
                    onSettingsPressed = {},
                    onApplicationPressed = {},
                    onApplicationLongPressed = {},
                    performWebSearch = {},
                    onSearchApplication = {},
                    onRenameGroup = { _, _ -> },
                    onDeleteGroup = {},
                )
            }
        }
    }
}
