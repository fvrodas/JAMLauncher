package io.github.fvrodas.jaml.features.launcher.presentation.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.features.common.themes.dimen16dp
import io.github.fvrodas.jaml.features.common.themes.dimen48dp
import io.github.fvrodas.jaml.features.common.themes.dimen56dp
import io.github.fvrodas.jaml.features.common.themes.dimen64dp
import io.github.fvrodas.jaml.features.common.themes.dimen8dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ApplicationsSheet(
    applicationsList: List<AppInfo>,
    onSearchApplication: (String) -> Unit,
    onApplicationPressed: (AppInfo) -> Unit
) {
    var searchFieldValue by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
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
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface),
                modifier = Modifier.size(
                    dimen48dp
                )
            )
        }
        OutlinedTextField(value = searchFieldValue, onValueChange = {
            searchFieldValue = it
            onSearchApplication.invoke(searchFieldValue)
        }, leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.search),
                contentDescription = ""
            )
        }, trailingIcon = {
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
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.onPrimary,
                cursorColor = MaterialTheme.colors.secondary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = dimen8dp,
                    horizontal = dimen16dp
                )
        )
        LazyColumn {
            items(applicationsList.size) {
                val item = applicationsList[it]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(dimen64dp)
                        .padding(vertical = dimen8dp, horizontal = dimen16dp)
                        .combinedClickable {
                            onApplicationPressed.invoke(item)
                        },
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.icon != null) {
                        Image(
                            bitmap = item.icon!!.asImageBitmap(),
                            contentDescription = "",
                            modifier = Modifier.size(dimen56dp)
                        )
                        Spacer(modifier = Modifier.width(dimen16dp))
                    }
                    Text(text = item.label, style = MaterialTheme.typography.subtitle1)
                }
            }
        }
    }
}