package io.github.fvrodas.jaml.ui.launcher.views

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen18dp
import io.github.fvrodas.jaml.ui.common.themes.dimen24dp
import io.github.fvrodas.jaml.ui.common.themes.dimen2dp
import io.github.fvrodas.jaml.ui.common.themes.dimen36dp
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShortcutItem(
    label: String,
    icon: Bitmap?,
    shouldHideShortcutIcons: Boolean = false,
    onApplicationPressed: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimen48dp)
            .clickable(
                onClick = { onApplicationPressed.invoke() },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = MaterialTheme.colorScheme.primary)
            ),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!shouldHideShortcutIcons) {
            Box(modifier = Modifier.padding(start = dimen16dp)) {
                icon?.let {
                    Image(
                        bitmap = icon.asImageBitmap(),
                        contentScale = ContentScale.Fit,
                        contentDescription = "",
                        modifier = Modifier
                            .size(dimen36dp)
                            .shadow(dimen2dp, shape = RoundedCornerShape(dimen18dp)),
                    )
                } ?: run {
                    Box(modifier = Modifier.size(dimen36dp)) {
                        Icon(
                            painter = painterResource(R.drawable.ic_push_pin_24),
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(dimen24dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(dimen16dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onBackground
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
