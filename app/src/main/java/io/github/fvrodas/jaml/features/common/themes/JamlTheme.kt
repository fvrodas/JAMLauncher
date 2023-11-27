package io.github.fvrodas.jaml.features.common.themes

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun JamlTheme(
    colorScheme: JamlColors,
    isInDarkMode: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {

    val currentScheme = if (isInDarkMode) {
        colorScheme.darkColors
    } else {
        colorScheme.lightColors
    }

    MaterialTheme(
        colors = currentScheme,
    ) {
        CompositionLocalProvider(
            LocalRippleTheme provides JamlRippleTheme,
            content = content
        )
    }
}

private object JamlRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor(): Color = MaterialTheme.colors.secondary

    @Composable
    override fun rippleAlpha(): RippleAlpha =
        RippleTheme.defaultRippleAlpha(Color.Black, lightTheme = !isSystemInDarkTheme())
}

val dimen2dp = 2.dp
val dimen4dp = 4.dp
val dimen8dp = 8.dp
val dimen16dp = 16.dp
val dimen24dp = 24.dp
val dimen32dp = 32.dp
val dimen48dp = 48.dp
val dimen56dp = 56.dp
val dimen64dp = 64.dp
val dimen72dp = 72.dp
val dimen112dp = 112.dp
val dimen200dp = 300.dp
