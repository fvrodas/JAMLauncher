package io.github.fvrodas.jaml.ui.launcher.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.ui.common.themes.dimen36dp
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.launcher.viewmodels.ApplicationsListUiState
import io.github.fvrodas.jaml.ui.launcher.viewmodels.AppsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appsViewModel: AppsViewModel,
    onSettingsPressed: () -> Unit = {},
    onApplicationPressed: (AppInfo) -> Unit = {}
) {

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        appsViewModel.retrieveApplicationsList()
    }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            density = LocalDensity.current,
            initialValue = SheetValue.PartiallyExpanded,
            skipPartiallyExpanded = false
        )
    )

    DisposableEffect(Unit) {
        onDispose {
            coroutineScope.launch {
                bottomSheetScaffoldState.bottomSheetState.partialExpand()
            }
        }
    }

    val applicationsListUiState by appsViewModel.appsUiState.collectAsState()

    BottomSheetScaffold(
        containerColor = Color.Transparent,
        scaffoldState = bottomSheetScaffoldState,
        sheetDragHandle = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimen48dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(
                            dimen36dp
                        )
                )
            }
        },
        sheetContent = {
            when (applicationsListUiState) {
                is ApplicationsListUiState.Success -> {
                    ApplicationsSheet(
                        (applicationsListUiState as ApplicationsListUiState.Success).apps,
                        onSettingsPressed,
                        {
                            coroutineScope.launch {
                                onApplicationPressed.invoke(it)
                                bottomSheetScaffoldState.bottomSheetState.partialExpand()
                            }
                        }
                    ) { appsViewModel.filterApplicationsList(it) }
                }

                else -> {}
            }
        }, sheetPeekHeight = dimen48dp
    ) {}
}