package io.github.fvrodas.jaml.core.domain.entities

import android.graphics.Color

data class IconConfig(
    val shouldUseThemedIcons: Boolean = false,
    val backgroundColor: Int = Color.WHITE,
    val foregroundColor: Int = Color.BLACK
)
