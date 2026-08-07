package io.github.fvrodas.jaml.ui.launcher.views

import android.os.Build
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import io.github.fvrodas.jaml.core.common.utils.BitmapUtils
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.common.themes.dimen4dp
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
    shouldDisplayThemeIcons: Boolean = false,
    toggleListVisibility: () -> Unit,
    changeShortcutVisibility: (Boolean, Boolean) -> Unit,
    onApplicationPressed: (PackageInfo) -> Unit,
    onApplicationLongPressed: (PackageInfo) -> Unit,
    displayAppList: (Boolean) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val pinnedList = remember(state?.pinnedApplications) {
        state?.pinnedApplications?.toList() ?: emptyList()
    }

    with(sharedTransitionLayout) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = dimen8dp)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.changes.any { it.pressed }) {
                                val change = event.changes.first()
                                if (change.previousPosition.y - change.position.y > MIN_DISPLACEMENT) {
                                    displayAppList(true)
                                }
                            }
                        }
                    }
                }
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimen4dp),
            ) {
                pinnedList.forEach { item ->
                    ApplicationItem(
                        label = item.packageInfo.label,
                        iconBitmap = if (shouldHideApplicationIcons) null
                                     else BitmapUtils.loadIconForPackage(
                                         item.packageInfo.packageName,
                                         shouldDisplayThemeIcons,
                                     ),
                        hasNotification = item.hasNotification,
                        notificationText = item.notificationTitle,
                        isFavorite = true,
                        onApplicationLongPressed = { isFavorite ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                                coroutineScope.launch {
                                    onApplicationLongPressed(item.packageInfo)
                                    changeShortcutVisibility(true, !isFavorite)
                                }
                            }
                        },
                        onApplicationPressed = {
                            coroutineScope.launch {
                                onApplicationPressed(item.packageInfo)
                                toggleListVisibility()
                            }
                        },
                    )
                }

                Row(
                    modifier = Modifier
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
            }
        }
    }
}

internal const val MIN_DISPLACEMENT = 10f
