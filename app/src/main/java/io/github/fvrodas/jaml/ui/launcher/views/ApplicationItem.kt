package io.github.fvrodas.jaml.ui.launcher.views

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import io.github.fvrodas.jaml.ui.common.themes.JamlColorScheme
import io.github.fvrodas.jaml.ui.common.themes.JamlTheme
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen24dp
import io.github.fvrodas.jaml.ui.common.themes.dimen2dp
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.common.themes.dimen4dp
import io.github.fvrodas.jaml.ui.common.themes.dimen64dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp
import io.github.fvrodas.jaml.ui.launcher.views.extensions.applyIf
import io.github.fvrodas.jaml.ui.launcher.views.extensions.hightlightCoincidence


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApplicationItem(
    label: String,
    notificationText: String? = null,
    searchText: String? = null,
    groupLabel: String? = null,
    iconBitmap: Bitmap? = null,
    iconVector: ImageVector? = null,
    hasNotification: Boolean = false,
    onApplicationLongPressed: ((isFavorite: Boolean) -> Unit)? = null,
    onGloballyPositioned: (centerY: Float) -> Unit = {},
    onApplicationPressed: () -> Unit
) {

    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.0f),
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = dimen64dp)
            .clip(RoundedCornerShape(dimen16dp))
            .applyIf(hasNotification) {
                background(gradient)
            }
            .onGloballyPositioned { coords ->
                onGloballyPositioned(coords.positionInWindow().y + coords.size.height / 2f)
            }
            .combinedClickable(
                onLongClick = longClickHandler(onApplicationLongPressed, false),
                onClick = { onApplicationPressed.invoke() },
                indication = ripple(color = MaterialTheme.colorScheme.primary),
                interactionSource = remember { MutableInteractionSource() }
            ),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.padding(start = dimen16dp)) {
            iconBitmap?.let {
                Box {
                    Image(
                        bitmap = iconBitmap.asImageBitmap(),
                        contentScale = ContentScale.FillBounds,
                        contentDescription = "",
                        modifier = Modifier
                            .size(dimen48dp)
                            .shadow(dimen2dp, shape = RoundedCornerShape(dimen24dp)),
                    )
                }
            }
            iconVector?.let {
                Icon(
                    imageVector = it,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(dimen48dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(dimen16dp))
        Column(modifier = Modifier.padding(vertical = dimen8dp)) {
            Text(
                text = label.hightlightCoincidence(searchText, MaterialTheme.colorScheme.tertiary),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = when {
                        hasNotification -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onBackground
                    }
                )
            )
            if (!groupLabel.isNullOrEmpty()) {
                Badge(
                    modifier = Modifier.padding(top = dimen4dp),
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Text(
                        text = groupLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            if (hasNotification && !notificationText.isNullOrEmpty()) {
                Badge(
                    modifier = Modifier.padding(top = dimen4dp),
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    if (iconBitmap != null || iconVector != null) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "",
                            modifier = Modifier
                                .size(dimen16dp),
                        )
                    }
                    Text(
                        text = notificationText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

private fun longClickHandler(
    handler: ((isFavorite: Boolean) -> Unit)?,
    isFavorite: Boolean,
): (() -> Unit)? = handler?.let { h -> { h(isFavorite) } }

internal const val DEFAULT_ALPHA = 0.9f
private const val SHADOW_ALPHA = 0.7f
private const val SHADOW_OFFSET_X = 2f
private const val SHADOW_OFFSET_Y = 3f
private const val SHADOW_BLUR_RADIUS = 3f

@Preview(showBackground = true)
@Composable
fun ApplicationItemPreview() {
    JamlTheme(
        colorScheme = JamlColorScheme.Gruvbox,
        isDynamicColorsEnabled = false,
        isInDarkMode = isSystemInDarkTheme(),
    ) { _ ->
        ApplicationItem(
            label = "Application",
            notificationText = "This is a notification",
            iconVector = null,
            hasNotification = true
        ) { }
    }
}
