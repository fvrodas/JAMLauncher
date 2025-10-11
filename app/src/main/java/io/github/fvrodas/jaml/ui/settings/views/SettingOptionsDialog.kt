package io.github.fvrodas.jaml.ui.settings.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen32dp
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingOptionsDialog(
    showIf: Boolean,
    title: String,
    options: List<Int>,
    defaultValue: Int = R.string.colorscheme_default,
    onDismiss: () -> Unit,
    onSelected: (Int) -> Unit
) {
    val themesBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        themesBottomSheetState.hide()
    }

    if (showIf) {
        ModalBottomSheet(
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = {
                onDismiss()
            },
            sheetState = themesBottomSheetState
        ) {
            LazyColumn(
                modifier = Modifier
                    .clip(RoundedCornerShape(dimen16dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(bottom = dimen32dp)
                    .fillMaxWidth()
            ) {
                item {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(horizontal = dimen32dp)
                    )
                    Spacer(modifier = Modifier.height(dimen16dp))
                }
                items(options.size) { index ->
                    val optionId = options[index]
                    Box(
                        modifier = Modifier
                            .clickable {
                                onSelected(optionId)
                            }) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = dimen32dp)
                                .height(dimen48dp)
                                .fillMaxWidth(MAX_WIDTH_FACTOR),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(dimen16dp)
                        ) {
                            RadioButton(selected = optionId == defaultValue, onClick = null)
                            Text(
                                text = stringResource(optionId),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

internal const val MAX_WIDTH_FACTOR = 0.9F
