package io.github.fvrodas.jaml.features.launcher.presentation.composables

import android.graphics.drawable.ColorDrawable
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import io.github.fvrodas.jaml.core.domain.entities.AppInfo
import io.github.fvrodas.jaml.features.common.themes.dimen48dp
import io.github.fvrodas.jaml.features.launcher.presentation.viewmodels.ApplicationsListUiState
import io.github.fvrodas.jaml.features.launcher.presentation.viewmodels.AppsViewModel
import org.koin.java.KoinJavaComponent.getKoin

@OptIn(ExperimentalMaterialApi::class)
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
        bottomSheetState = BottomSheetState(
            BottomSheetValue.Collapsed,
            density = LocalDensity.current
        )
    )

    val applicationsListUiState by appsViewModel.appsUiState.collectAsState()

    BottomSheetScaffold(
        backgroundColor = Color.Transparent,
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            when (applicationsListUiState) {
                is ApplicationsListUiState.Success -> {
                    ApplicationsSheet(
                        (applicationsListUiState as ApplicationsListUiState.Success).apps,
                        onSettingsPressed,
                        onApplicationPressed
                    )  { appsViewModel.filterApplicationsList(it) }
                }

                else -> {}
            }
        }, sheetPeekHeight = dimen48dp
    ) {}
}