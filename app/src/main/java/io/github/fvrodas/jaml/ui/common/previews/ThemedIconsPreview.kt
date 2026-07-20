package io.github.fvrodas.jaml.ui.common.previews

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.core.common.utils.BitmapUtils
import io.github.fvrodas.jaml.ui.common.themes.JamlColorScheme
import io.github.fvrodas.jaml.ui.common.themes.JamlTheme

private data class IconCase(@DrawableRes val resId: Int, val label: String)

private val LIGHT_BG = android.graphics.Color.WHITE
private val LIGHT_FG = android.graphics.Color.BLACK
private val DARK_BG = android.graphics.Color.parseColor("#141313")
private val DARK_FG = android.graphics.Color.parseColor("#E5E2E1")

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun IconCaseRow(case: IconCase) {
    val context = LocalContext.current
    val drawable = remember(case.resId) { ContextCompat.getDrawable(context, case.resId)!! }

    val original = remember(case.resId) { drawable.toBitmap(108, 108) }
    val light = remember(case.resId) {
        with(BitmapUtils) {
            drawable.forceAdaptiveIconIfNeeded()
                .toThemedIcon(LIGHT_BG, LIGHT_FG)
                .toBitmap(108, 108)
        }
    }
    val dark = remember(case.resId) {
        with(BitmapUtils) {
            drawable.forceAdaptiveIconIfNeeded()
                .toThemedIcon(DARK_BG, DARK_FG)
                .toBitmap(108, 108)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = case.label,
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.labelSmall,
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Image(bitmap = original.asImageBitmap(), contentDescription = null, modifier = Modifier.size(48.dp))
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Image(bitmap = light.asImageBitmap(), contentDescription = null, modifier = Modifier.size(48.dp))
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color(0xFF141313))
                .padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(bitmap = dark.asImageBitmap(), contentDescription = null, modifier = Modifier.size(48.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true, widthDp = 380, apiLevel = 33, name = "Themed Icons")
@Composable
private fun ThemedIconsPreview() {
    JamlTheme(
        colorScheme = JamlColorScheme.Default,
        isDynamicColorsEnabled = false,
        isInDarkMode = isSystemInDarkTheme(),
    ) { _ ->
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("", modifier = Modifier.weight(1.5f))
                Text("Original", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                Text("Light", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
                Text("Dark", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall)
            }
            Divider()
            IconCaseRow(IconCase(R.drawable.ic_preview_adaptive_with_monochrome, "Adaptive\n+ Monochrome"))
            Divider()
            IconCaseRow(IconCase(R.drawable.ic_preview_bitmap, "Adaptive\nNo Monochrome"))
            Divider()
            IconCaseRow(IconCase(R.drawable.ic_preview_legacy, "Legacy\n(Non-Adaptive)"))
        }
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}
