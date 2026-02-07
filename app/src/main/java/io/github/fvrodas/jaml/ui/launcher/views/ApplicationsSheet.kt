package io.github.fvrodas.jaml.ui.launcher.views

import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.models.toLauncherEntry
import io.github.fvrodas.jaml.ui.common.themes.JamlColorScheme
import io.github.fvrodas.jaml.ui.common.themes.JamlTheme
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen24dp
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
) {
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    var searchFieldValue by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusRequester = remember { FocusRequester() }
    val mutableInteractionSource = remember { MutableInteractionSource() }
    val focusState = mutableInteractionSource.collectIsFocusedAsState()

    var trackedDragAmount = 0f

    LaunchedEffect(Unit) {
        lazyListState.animateScrollToItem(0)
    }

    LaunchedEffect(focusState) {
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
                    .background(
                        MaterialTheme.colorScheme.background,
                    )
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = {},
                            onDragEnd = {
                                if (trackedDragAmount > 0) {
                                    toggleListVisibility()
                                }
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
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search"
                        )
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
                        .padding(
                            horizontal = dimen16dp
                        ),
                )
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.padding(horizontal = dimen8dp),
                    verticalArrangement = Arrangement.spacedBy(dimen8dp)
                ) {
                    items(state.applicationsList.size) {
                        val item = state.applicationsList.elementAt(it)
                        ApplicationItem(
                            label = item.packageInfo.label,
                            notificationText = item.notificationTitle,
                            searchText = searchFieldValue,
                            iconBitmap = if (shouldHideApplicationIcons) null else item.packageInfo.icon,
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
                                    coroutineScope.launch {
                                        toggleListVisibility()
                                    }
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

                            if (shouldHideApplicationIcons) {
                                TextButton(onClick = { onSettingsPressed.invoke() }) {
                                    Text(
                                        stringResource(R.string.settings_button),
                                        textDecoration = TextDecoration.Underline
                                    )
                                }
                            } else {
                                IconButton(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.background),
                                    onClick = { onSettingsPressed.invoke() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Settings,
                                        contentDescription = stringResource(R.string.settings_button),
                                        modifier = Modifier
                                            .size(dimen24dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
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
    ) {
        SharedTransitionLayout {
            AnimatedContent(targetState = true, label = "ApplicationsSheet") {
                ApplicationsSheet(
                    state = ApplicationSheetState(
                        applicationsList = setOf(
                            PackageInfo(packageName = "com.android.settings", label = "Settings").toLauncherEntry(),
                            PackageInfo(packageName = "com.android.vending", label = "Play Store").toLauncherEntry(),
                            PackageInfo(
                                packageName = "com.google.android.apps.maps",
                                label = "Maps"
                            ).toLauncherEntry(),
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
                    onSearchApplication = {}
                )
            }
        }
    }
}
