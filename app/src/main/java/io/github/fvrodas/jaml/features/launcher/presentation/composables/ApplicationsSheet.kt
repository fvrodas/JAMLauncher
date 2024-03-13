package io.github.fvrodas.jaml.features.launcher.presentation.composables

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.features.common.themes.dimen16dp
import io.github.fvrodas.jaml.features.common.themes.dimen24dp
import io.github.fvrodas.jaml.features.common.themes.dimen2dp
import io.github.fvrodas.jaml.features.common.themes.dimen36dp
import io.github.fvrodas.jaml.features.common.themes.dimen48dp
import io.github.fvrodas.jaml.features.common.themes.dimen64dp
import io.github.fvrodas.jaml.features.common.themes.dimen8dp

@Composable
fun ApplicationsSheet(
    applicationsList: List<AppInfo>,
    onSettingsPressed: () -> Unit,
    onApplicationPressed: (AppInfo) -> Unit,
    onSearchApplication: (String) -> Unit
) {
    var searchFieldValue by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.chevron_up),
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.size(
                    dimen48dp
                )
            )
        }
        OutlinedTextField(
            value = searchFieldValue,
            onValueChange = {
                searchFieldValue = it
                onSearchApplication.invoke(searchFieldValue)
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = ""
                )
            },
            trailingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.backspace),
                    contentDescription = "",
                    modifier = Modifier.clickable {
                        searchFieldValue = ""
                        onSearchApplication.invoke(searchFieldValue)
                    }
                )
            },
            shape = RoundedCornerShape(dimen8dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = dimen8dp,
                    horizontal = dimen16dp
                ),
        )
        LazyColumn {
            items(applicationsList.size) {
                val item = applicationsList[it]
                ApplicationItem(label = item.label, icon = item.icon) {
                    onApplicationPressed.invoke(item)
                }
            }
            item {
                Divider()
                ApplicationItem(label = "Launcher Settings") {
                    onSettingsPressed.invoke()
                }
            }
        }
    }
}

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
            Image(
                painter = painterResource(id = R.drawable.settings),
                contentScale = ContentScale.Inside,
                contentDescription = "",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .size(dimen48dp),
            )
        }
        Spacer(modifier = Modifier.width(dimen16dp))
        Text(
            text = label, style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}