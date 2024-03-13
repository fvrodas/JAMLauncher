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
import androidx.compose.ui.unit.dp

@Composable
fun JamlTheme(
    colorScheme: JamlColorSchemes,
    isInDarkMode: Boolean,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val currentScheme = remember {
        mutableStateOf(JamlColorSchemes.Default.lightColorScheme)
    }

    //CURRENT STATE: DESPITE REACH THE RIGHT STATEMENTS IN BOTH SCENARIOS,
    //DARK THEME IS NOT APPLIED
    currentScheme.value = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isInDarkMode) {
                //REACH HERE
                dynamicDarkColorScheme(context)
            } else {
                //ALSO REACH HERE
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
