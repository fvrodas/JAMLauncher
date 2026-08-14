package io.github.fvrodas.jaml.ui.launcher.views

import android.graphics.Bitmap
import androidx.compose.ui.graphics.vector.ImageVector

data class OverlayAction(
    val label: String,
    val bitmapIcon: Bitmap? = null,
    val vectorIcon: ImageVector? = null,
    val action: () -> Unit,
)
