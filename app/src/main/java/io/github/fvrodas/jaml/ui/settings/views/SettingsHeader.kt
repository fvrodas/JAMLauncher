package io.github.fvrodas.jaml.ui.settings.views

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.google.android.material.color.utilities.Blend
import io.github.fvrodas.jaml.ui.common.themes.dimen24dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp

@Composable
fun SettingsHeader(@StringRes resId: Int, icon: ImageVector? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = resId),
            style = MaterialTheme.typography.titleLarge.copy(
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        icon?.let {
            Image(
                imageVector = icon,
                contentDescription = icon.name,
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colorScheme.secondary,
                    BlendMode.SrcAtop
                ),
                modifier = Modifier
                    .padding(start = dimen8dp)
                    .size(dimen24dp)
            )
        }
    }
}
