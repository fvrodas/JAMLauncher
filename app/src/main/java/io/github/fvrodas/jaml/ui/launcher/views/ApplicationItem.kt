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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import io.github.fvrodas.jaml.ui.common.themes.JamlColorScheme
import io.github.fvrodas.jaml.ui.common.themes.JamlTheme
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen24dp
import io.github.fvrodas.jaml.ui.common.themes.dimen2dp
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
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
    iconBitmap: Bitmap? = null,
    iconVector: ImageVector? = null,
    hasNotification: Boolean = false,
    isFavorite: Boolean = false,
    onApplicationLongPressed: ((isFavorite: Boolean) -> Unit)? = null,
    onApplicationPressed: () -> Unit
) {

    var hasNotificationState by remember {
        mutableStateOf(hasNotification)
    }

    var notificationTextState by remember {
        mutableStateOf(notificationText)
    }

    LaunchedEffect(hasNotification, notificationText) {
        hasNotificationState = hasNotification
        notificationTextState = notificationText
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimen64dp)
            .clip(RoundedCornerShape(dimen16dp))
            .applyIf(hasNotificationState) {
                background(GRADIENT)
            }
            .combinedClickable(
                onLongClick = { onApplicationLongPressed?.invoke(isFavorite) },
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
                    if (hasNotificationState && !isFavorite) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)
                                .size(dimen8dp)
                        )
                    }
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
        Column {
            Text(
                text = label.hightlightCoincidence(searchText, MaterialTheme.colorScheme.tertiary),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = if (hasNotificationState) {
                        MaterialTheme.colorScheme.primary
                    } else if (isFavorite) {
                        Color.White
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    },
                    shadow = if (isFavorite) FAVORITE_DROP_SHADOW else null
                )
            )
            if (isFavorite && hasNotificationState && !notificationTextState.isNullOrEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if(iconBitmap != null || iconVector != null) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.onBackground.copy(.9f),
                            modifier = Modifier
                                .size(dimen16dp),
                        )
                    }
                    Text(
                        text = notificationTextState ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(.9f),
                        )
                    )
                }
            }
        }
    }
}

internal val FAVORITE_DROP_SHADOW: Shadow = Shadow(
    Color.Black.copy(.9f),
    Offset(2f, 3f),
    3f
)

internal val GRADIENT = Brush.linearGradient(
    colors = listOf(
        Color.Black.copy(alpha = 0.7f),
        Color.Black.copy(alpha = 0.1f)
    )
)

@Preview(showBackground = true)
@Composable
fun ApplicationItemPreview() {
    JamlTheme(
        colorScheme = JamlColorScheme.Gruvbox,
        isDynamicColorsEnabled = false,
        isInDarkMode = isSystemInDarkTheme(),
    ) {
        ApplicationItem(
            label = "Application",
            notificationText = "This is a notification",
            iconVector = null,
            isFavorite = true,
            hasNotification = true
        ) { }
    }
}
