package io.github.fvrodas.jaml.ui.launcher.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen24dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsSheet(
    bottomSheetValue: SheetValue,
    applicationsList: List<AppInfo>,
    onSettingsPressed: () -> Unit,
    onApplicationPressed: (AppInfo) -> Unit,
    onApplicationLongPressed: (AppInfo) -> Unit,
    onSearchApplication: (String) -> Unit,
) {
    var searchFieldValue by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()

    LaunchedEffect(bottomSheetValue) {
        if (bottomSheetValue != SheetValue.Expanded) {
            lazyListState.scrollToItem(0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
    ) {
        OutlinedTextField(
            value = searchFieldValue,
            onValueChange = {
                searchFieldValue = it
                onSearchApplication.invoke(searchFieldValue)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = ""
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Clear,
                    contentDescription = "",
                    modifier = Modifier.clickable {
                        searchFieldValue = ""
                        onSearchApplication.invoke(searchFieldValue)
                    }
                )
            },
            shape = RoundedCornerShape(dimen24dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = dimen8dp,
                    horizontal = dimen16dp
                ),
        )
        LazyColumn(
            state = lazyListState
        ) {
            items(applicationsList.size) {
                val item = applicationsList[it]
                ApplicationItem(
                    label = item.label,
                    icon = item.icon,
                    hasNotification = item.hasNotification,
                    onApplicationLongPressed = {
                        onApplicationLongPressed.invoke(item)
                    }
                ) {
                    onApplicationPressed.invoke(item)
                }
            }
            item {
                HorizontalDivider()
                ApplicationItem(label = "Launcher Settings") {
                    onSettingsPressed.invoke()
                }
            }
        }
    }
}
