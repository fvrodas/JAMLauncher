package io.github.fvrodas.jaml.ui.launcher.views

import android.os.Build
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.common.themes.dimen64dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp
import io.github.fvrodas.jaml.ui.launcher.viewmodels.ApplicationSheetState
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    sharedTransitionLayout: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    state: ApplicationSheetState?,
    shouldHideApplicationIcons: Boolean = false,
    toggleListVisibility: () -> Unit,
    changeShortcutVisibility: (Boolean, Boolean) -> Unit,
    onApplicationPressed: (PackageInfo) -> Unit,
    onApplicationLongPressed: (PackageInfo) -> Unit,
    displayAppList: (Boolean) -> Unit
) {
    var startOffset: Offset = Offset.Zero

    with(sharedTransitionLayout) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {

                    }
                }
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
            if (state?.pinnedApplications?.isNotEmpty() == true) {
                val coroutineScope = rememberCoroutineScope()
                val lazyListState = rememberLazyListState()

                LazyColumn(
                    state = lazyListState
                ) {
                    items(state.pinnedApplications.size) {
                        val item = state.pinnedApplications.elementAt(it)
                        ApplicationItem(
                            label = item.label,
                            iconBitmap = if (shouldHideApplicationIcons) null else item.icon,
                            hasNotification = item.hasNotification,
                            isFavorite = true,
                            onApplicationLongPressed = { isFavorite ->
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                    coroutineScope.launch {
                                        onApplicationLongPressed.invoke(item)
                                        changeShortcutVisibility(true, !isFavorite)
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
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
                    .padding(bottom = dimen8dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(dimen64dp)
                        .background(MaterialTheme.colorScheme.background),
                    onClick = { displayAppList(true) }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = null,
                        modifier = Modifier
                            .sharedElement(
                                rememberSharedContentState("arrow"),
                                animatedVisibilityScope = animatedVisibilityScope
                            )
                            .size(dimen48dp),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}
