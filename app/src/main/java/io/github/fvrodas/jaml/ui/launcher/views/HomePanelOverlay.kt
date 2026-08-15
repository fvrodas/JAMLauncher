package io.github.fvrodas.jaml.ui.launcher.views

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.unit.sp
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
fun HomePanelOverlay(
    pinnedApps: List<LauncherEntry>,
    selectedIndex: Int?,
    alignment: Alignment = Alignment.Center,
    shouldHideIcons: Boolean = false,
    shouldDisplayThemeIcons: Boolean = false,
    timeoutProgress: Float = 0f,
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
                    animationSpec = tween(OVERLAY_ANIM_DURATION),
                    label = "pinnedIconSize"
                )
                val itemAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else UNSELECTED_ALPHA,
                    animationSpec = tween(OVERLAY_ANIM_DURATION),
                    label = "pinnedAlpha"
                )
                val icon = remember(entry.packageInfo.packageName, shouldDisplayThemeIcons) {
                    BitmapUtils.loadIconForPackage(
                        entry.packageInfo.packageName,
                        shouldDisplayThemeIcons
                    )
                }
                val textSize by animateFloatAsState(
                    targetValue = if (isSelected) TEXT_SIZE_SELECTED_SP else TEXT_SIZE_NORMAL_SP,
                    animationSpec = tween(OVERLAY_ANIM_DURATION),
                    label = "pinnedTextSize"
                )

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
                    if (shouldHideIcons) {
                        Text(
                            text = entry.packageInfo.label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = textSize.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = dimen8dp)
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(contentAlignment = Alignment.Center) {
                                icon?.let { bmp ->
                                    if (isSelected && timeoutProgress > 0f) {
                                        val arcColor = MaterialTheme.colorScheme.secondary
                                        Canvas(modifier = Modifier.size(iconSize + dimen8dp)) {
                                            val strokePx = TIMEOUT_INDICATOR_STROKE_DP * density
                                            val inset = strokePx / 2f
                                            drawArc(
                                                color = arcColor,
                                                startAngle = -90f,
                                                sweepAngle = 360f * timeoutProgress,
                                                useCenter = false,
                                                topLeft = Offset(inset, inset),
                                                size = Size(
                                                    size.width - strokePx,
                                                    size.height - strokePx
                                                ),
                                                style = Stroke(
                                                    width = strokePx,
                                                    cap = StrokeCap.Round
                                                ),
                                            )
                                        }
                                    }
                                    BadgedBox(
                                        badge = {
                                            if (entry.hasNotification) {
                                                val badgeSize = iconSize / 6f
                                                Badge(
                                                    containerColor = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier
                                                        .size(badgeSize)
                                                        .border(
                                                            badgeSize / 6f,
                                                            MaterialTheme.colorScheme.surface,
                                                            CircleShape
                                                        ),
                                                ) { }
                                            }
                                        }
                                    ) {
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
                            }
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(dimen4dp))
                                Text(
                                    text = entry.packageInfo.label,
                                    style = MaterialTheme.typography.titleMedium.copy(
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
}

private const val OVERLAY_ANIM_DURATION = 150
private const val UNSELECTED_ALPHA = 0.45f
private const val TEXT_SIZE_SELECTED_SP = 28f
private const val TEXT_SIZE_NORMAL_SP = 22f
private const val TIMEOUT_INDICATOR_STROKE_DP = 3f
