package io.github.fvrodas.jaml.features.common.themes


import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun JamlTheme(
    colorScheme: JamlColorScheme,
    isInDarkMode: Boolean,
    isDynamicColorsEnabled: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentScheme = remember {
        mutableStateOf(JamlColorScheme.Default.lightColorScheme)
    }

    currentScheme.value = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isDynamicColorsEnabled -> {
            if (isInDarkMode) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        isInDarkMode -> colorScheme.darkColorScheme
        else -> colorScheme.lightColorScheme
    }


    MaterialTheme(
        colorScheme = currentScheme.value,
    ) {
        CompositionLocalProvider(
            LocalRippleTheme provides JamlRippleTheme,
            content = content
        )
    }
}

private object JamlRippleTheme : RippleTheme {
    @Composable
    override fun defaultColor(): Color = MaterialTheme.colorScheme.secondary

    @Composable
    override fun rippleAlpha(): RippleAlpha =
        RippleTheme.defaultRippleAlpha(Color.Black, lightTheme = !isSystemInDarkTheme())
}
