package io.github.fvrodas.jaml.features.launcher.presentation.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.features.common.themes.dimen36dp
import io.github.fvrodas.jaml.features.launcher.presentation.viewmodels.ApplicationsListUiState
import io.github.fvrodas.jaml.features.launcher.presentation.viewmodels.AppsViewModel
import org.koin.java.KoinJavaComponent.getKoin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    appsViewModel: AppsViewModel = getKoin().get(),
    onSettingsPressed: () -> Unit = {},
    onApplicationPressed: (AppInfo) -> Unit = {}
) {

    LaunchedEffect(Unit) {
        appsViewModel.retrieveApplicationsList()
    }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = SheetState(
            initialValue = SheetValue.PartiallyExpanded,
            skipPartiallyExpanded = false
        )
    )

    val applicationsListUiState by appsViewModel.appsUiState.collectAsState()

    BottomSheetScaffold(
        containerColor = Color.Transparent,
        scaffoldState = bottomSheetScaffoldState,
        sheetDragHandle = {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
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
                        onApplicationPressed
                    ) { appsViewModel.filterApplicationsList(it) }
                }

                else -> {}
            }
        }, sheetPeekHeight = dimen36dp
    ) {}
}