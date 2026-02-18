package io.github.fvrodas.jaml.ui.common.themes

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun JamlTheme(
    colorScheme: JamlColorScheme,
    isInDarkMode: Boolean,
    isDynamicColorsEnabled: Boolean,
    content: @Composable (currentColorScheme: ColorScheme) -> Unit
) {
    val context = LocalContext.current
    val currentScheme = remember {
        mutableStateOf(JamlColorScheme.Default.lightColorScheme)
    }

    val view = LocalView.current

    if(!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !isInDarkMode
        }
    }

    LaunchedEffect(colorScheme, isDynamicColorsEnabled, isInDarkMode) {
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
    }

    MaterialTheme(
        colorScheme = currentScheme.value,
    ) {
        content(currentScheme.value)
    }
}
