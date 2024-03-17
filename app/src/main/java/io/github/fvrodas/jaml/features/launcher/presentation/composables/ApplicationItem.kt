package io.github.fvrodas.jaml.features.launcher.presentation.composables

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.github.fvrodas.jaml.features.common.themes.dimen16dp
import io.github.fvrodas.jaml.features.common.themes.dimen24dp
import io.github.fvrodas.jaml.features.common.themes.dimen2dp
import io.github.fvrodas.jaml.features.common.themes.dimen48dp
import io.github.fvrodas.jaml.features.common.themes.dimen64dp


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApplicationItem(label: String, icon: Bitmap? = null, onApplicationPressed: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimen64dp)
            .padding(horizontal = dimen16dp)
            .combinedClickable {
                onApplicationPressed.invoke()
            },
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.let {
            Image(
                bitmap = icon.asImageBitmap(),
                contentScale = ContentScale.FillBounds,
                contentDescription = "",
                modifier = Modifier
                    .size(dimen48dp)
                    .shadow(dimen2dp, shape = RoundedCornerShape(dimen24dp)),
            )
        } ?: run {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(dimen48dp),
            )
        }
        Spacer(modifier = Modifier.width(dimen16dp))
        Text(
            text = label, style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}
