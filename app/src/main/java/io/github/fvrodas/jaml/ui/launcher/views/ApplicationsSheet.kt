package io.github.fvrodas.jaml.ui.launcher.views

import android.os.Build
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
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
    onSearchApplication: (String) -> Unit,
) {

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    var searchFieldValue by remember { mutableStateOf("") }

    var startOffset: Offset = Offset.Zero
    var trackedDragAmount = 0f

    LaunchedEffect(Unit) {
        lazyListState.animateScrollToItem(0)
    }

    DisposableEffect(Unit) {
        onDispose {
            searchFieldValue = ""
            onSearchApplication(searchFieldValue)
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    with(sharedTransitionScope) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background,
                )
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            startOffset = it
                        },
                        onDragEnd = {
                            if (trackedDragAmount > 0) {
                                toggleListVisibility()
                            }
                        }
                    ) { change, dragAmount ->
                        trackedDragAmount = dragAmount
                    }
                }
                .navigationBarsPadding()
                .statusBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(dimen8dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = dimen16dp),
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
                textStyle = MaterialTheme.typography.bodyLarge,
                onValueChange = {
                    searchFieldValue = it
                    onSearchApplication.invoke(searchFieldValue)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = ""
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
                shape = RoundedCornerShape(dimen24dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimen16dp
                    ),
            )
            LazyColumn(
                state = lazyListState
            ) {
                if (state.pinnedApplications.isNotEmpty()) {
                    item {
                        Text(
                            "Favorites",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .padding(horizontal = dimen16dp)
                                .padding(top = dimen8dp)
                        )
                    }
                    items(state.pinnedApplications.size) {
                        val item = state.pinnedApplications.elementAt(it)
                        ApplicationItem(
                            label = item.label,
                            iconBitmap = if (shouldHideApplicationIcons) null else item.icon,
                            hasNotification = item.hasNotification,
                            onApplicationLongPressed = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                    coroutineScope.launch {
                                        onApplicationLongPressed.invoke(item)
                                        changeShortcutVisibility(true, false)
                                    }
                                }
                            },
                            onApplicationPressed = {
                                coroutineScope.launch {
                                    onApplicationPressed.invoke(item)
                                    toggleListVisibility()
                                }
                            }
                        )
                    }
                    item {
                        HorizontalDivider()
                    }
                }
                items(state.applicationsList.size) {
                    val item = state.applicationsList.elementAt(it)
                    ApplicationItem(
                        label = item.label,
                        iconBitmap = if (shouldHideApplicationIcons) null else item.icon,
                        hasNotification = item.hasNotification,
                        onApplicationLongPressed = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                coroutineScope.launch {
                                    onApplicationLongPressed.invoke(item)
                                    changeShortcutVisibility(true, true)
                                }
                            }
                        },
                        onApplicationPressed = {
                            coroutineScope.launch {
                                onApplicationPressed.invoke(item)
                                toggleListVisibility()
                            }
                        }
                    )
                }
                item {
                    HorizontalDivider()
                    ApplicationItem(
                        label = "Launcher Settings",
                        iconVector = if (shouldHideApplicationIcons) null else Icons.Rounded.Settings
                    ) {
                        onSettingsPressed.invoke()
                    }
                }
            }
        }
    }
}
