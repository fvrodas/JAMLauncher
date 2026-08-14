package io.github.fvrodas.jaml.ui.launcher.views

import android.graphics.Bitmap
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import io.github.fvrodas.jaml.ui.common.themes.dimen16dp
import io.github.fvrodas.jaml.ui.common.themes.dimen24dp
import io.github.fvrodas.jaml.ui.common.themes.dimen2dp
import io.github.fvrodas.jaml.ui.common.themes.dimen32dp
import io.github.fvrodas.jaml.ui.common.themes.dimen48dp
import io.github.fvrodas.jaml.ui.common.themes.dimen4dp
import io.github.fvrodas.jaml.ui.common.themes.dimen8dp

@Composable
fun ActionsOverlay(
    items: List<OverlayAction>,
    selectedIndex: Int?,
    alignment: Alignment = Alignment.Center,
    shouldHideIcons: Boolean = false,
    headerLabel: String? = null,
    headerIcon: Bitmap? = null,
    onDismiss: (() -> Unit)? = null,
    onItemCenterYChanged: (index: Int, centerY: Float) -> Unit,
    onItemClicked: (index: Int) -> Unit,
) {
    val columnModifier = when (alignment) {
        Alignment.CenterStart -> Modifier.padding(start = dimen16dp)
        Alignment.CenterEnd -> Modifier.padding(end = dimen16dp)
        else -> Modifier.padding(horizontal = dimen32dp)
    }
    val horizontalAlignment = when (alignment) {
        Alignment.CenterStart -> Alignment.Start
        Alignment.CenterEnd -> Alignment.End
        else -> Alignment.CenterHorizontally
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .then(
                if (onDismiss != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onDismiss() },
                ) else Modifier
            ),
        contentAlignment = alignment,
    ) {
        Column(
            modifier = columnModifier,
            horizontalAlignment = horizontalAlignment,
        ) {
            if (headerLabel != null) {
                Row(
                    modifier = Modifier.padding(bottom = dimen8dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (headerIcon != null && !shouldHideIcons) {
                        Image(
                            bitmap = headerIcon.asImageBitmap(),
                            contentDescription = headerLabel,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .padding(bottom = dimen16dp)
                                .size(dimen48dp)
                                .shadow(dimen2dp, shape = RoundedCornerShape(dimen16dp)),
                        )
                        Spacer(modifier = Modifier.width(dimen8dp))
                    }
                    Text(
                        text = headerLabel,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = Color.White,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(dimen16dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(vertical = dimen8dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(dimen4dp),
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = index == selectedIndex
                    val iconSize: Dp by animateDpAsState(
                        targetValue = if (isSelected) dimen32dp else dimen24dp,
                        animationSpec = tween(OVERLAY_ANIM_DURATION),
                        label = "actionIconSize",
                    )
                    val itemAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else UNSELECTED_ALPHA,
                        animationSpec = tween(OVERLAY_ANIM_DURATION),
                        label = "actionAlpha",
                    )

                    val textSize by animateFloatAsState(
                        targetValue = if (isSelected) TEXT_SIZE_SELECTED_SP else TEXT_SIZE_NORMAL_SP,
                        animationSpec = tween(OVERLAY_ANIM_DURATION),
                        label = "actionTextSize"
                    )

                    val textWeight by animateIntAsState(
                        targetValue = if (isSelected) FontWeight.SemiBold.weight else FontWeight.Normal.weight,
                        animationSpec = tween(OVERLAY_ANIM_DURATION),
                        label = "actionTextWeight"
                    )

                    Row(
                        modifier = Modifier
                            .onGloballyPositioned { coords ->
                                val centerY = coords.positionInWindow().y + coords.size.height / 2f
                                onItemCenterYChanged(index, centerY)
                            }
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onItemClicked(index) }
                            .padding(horizontal = dimen16dp, vertical = dimen8dp)
                            .alpha(itemAlpha),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (!shouldHideIcons) {
                            item.bitmapIcon?.let { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = item.label,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(iconSize),
                                )
                            } ?: item.vectorIcon?.let { icon ->
                                Icon(
                                    imageVector = icon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(iconSize),
                                )
                            }
                            Spacer(modifier = Modifier.width(dimen8dp))
                        }
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight(textWeight),
                                fontSize = textSize.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
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
