package io.github.fvrodas.jaml.ui.launcher.composables

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen24dp
import io.github.fvrodas.jaml.ui.common.themes.dimen2dp
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.common.themes.dimen64dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApplicationItem(
    label: String,
    icon: Bitmap? = null,
    hasNotification: Boolean = false,
    onApplicationLongPressed: (() -> Unit)? = null,
    onApplicationPressed: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimen64dp)
            .combinedClickable(
                onLongClick = { onApplicationLongPressed?.invoke() }
            ) {
                onApplicationPressed.invoke()
            },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(modifier = Modifier.padding(start = dimen16dp)) {
            icon?.let {
                Image(
                    bitmap = icon.asImageBitmap(),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = "",
                    modifier = Modifier
                        .size(dimen48dp)
                        .shadow(dimen2dp, shape = RoundedCornerShape(dimen24dp)),
                )
                if (hasNotification) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .clip(
                                shape = RoundedCornerShape(dimen8dp)
                            )
                            .size(dimen16dp)
                            .background(MaterialTheme.colorScheme.surface)
                            .align(Alignment.BottomEnd)
                    )
                }
            } ?: run {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(dimen48dp),
                )
            }
        }
        Spacer(modifier = Modifier.width(dimen16dp))
        Text(
            text = label, style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}
