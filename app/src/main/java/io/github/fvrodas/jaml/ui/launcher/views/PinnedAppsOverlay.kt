package io.github.fvrodas.jaml.ui.launcher.views

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.fvrodas.jaml.core.common.utils.BitmapUtils
import io.github.fvrodas.jaml.ui.common.models.LauncherEntry
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen2dp
import io.github.fvrodas.jaml.ui.common.themes.dimen32dp
import io.github.fvrodas.jaml.ui.common.themes.dimen36dp
import io.github.fvrodas.jaml.ui.common.themes.dimen4dp
import io.github.fvrodas.jaml.ui.common.themes.dimen64dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp

@Composable
fun PinnedAppsOverlay(
    pinnedApps: List<LauncherEntry>,
    selectedIndex: Int?,
    dwellProgress: Float,
    alignment: Alignment = Alignment.Center,
    shouldHideIcons: Boolean = false,
    shouldDisplayThemeIcons: Boolean = false,
    onItemCenterYChanged: (index: Int, centerY: Float) -> Unit,
) {
    val columnModifier = when (alignment) {
        Alignment.CenterStart -> Modifier.padding(start = dimen16dp)
        Alignment.CenterEnd -> Modifier.padding(end = dimen16dp)
        else -> Modifier.padding(horizontal = dimen32dp)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = alignment
    ) {
        Column(
            modifier = columnModifier
                .clip(RoundedCornerShape(dimen16dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = dimen8dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimen4dp)
        ) {
            pinnedApps.forEachIndexed { index, entry ->
                val isSelected = index == selectedIndex
                val iconSize: Dp by animateDpAsState(
                    targetValue = if (isSelected) dimen64dp else dimen36dp,
                    animationSpec = tween(150),
                    label = "pinnedIconSize"
                )
                val itemAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.45f,
                    animationSpec = tween(150),
                    label = "pinnedAlpha"
                )
                val ringColor = MaterialTheme.colorScheme.primary

                Box(
                    modifier = Modifier
                        .onGloballyPositioned { coords ->
                            val centerY = coords.positionInWindow().y + coords.size.height / 2f
                            onItemCenterYChanged(index, centerY)
                        }
                        .padding(horizontal = dimen16dp, vertical = dimen4dp)
                        .alpha(itemAlpha),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            if (!shouldHideIcons) {
                                BitmapUtils.loadIconForPackage(
                                    entry.packageInfo.packageName,
                                    shouldDisplayThemeIcons
                                )?.let { bmp ->
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = entry.packageInfo.label,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .size(iconSize)
                                            .shadow(dimen2dp, CircleShape)
                                            .clip(CircleShape)
                                    )
                                }
                            }
                            if (isSelected && dwellProgress > 0f) {
                                Canvas(modifier = Modifier.size(iconSize + 10.dp)) {
                                    drawArc(
                                        color = ringColor,
                                        startAngle = -90f,
                                        sweepAngle = 360f * dwellProgress,
                                        useCenter = false,
                                        style = Stroke(
                                            width = 3.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    )
                                }
                            }
                        }
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(dimen4dp))
                            Text(
                                text = entry.packageInfo.label,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = dimen8dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
