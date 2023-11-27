package io.github.fvrodas.jaml.features.common.themes

import android.annotation.SuppressLint
import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color

sealed class JamlColors(val lightColors: Colors, val darkColors: Colors) {
    object Default : JamlColors(
        lightColors = lightColors(
            primary = Color(0xffe6e6e6),
            primaryVariant = Color(0xffe6e6e6),
            secondary = Color(0xff424242),
            background = Color(0xffe6e6e6),
            surface = Color(0xffe6e6e6),
            onPrimary = Color(0xff424242),
            onSecondary = Color(0xffe6e6e6),
            onBackground = Color(0xff424242),
            onSurface = Color(0xff424242),
            error = Color(0xffe6e6e6),
            onError = Color(0xff424242)
        ),
        darkColors = darkColors(
            primary = Color(0xff424242),
            primaryVariant = Color(0xff424242),
            secondary = Color(0xffe6e6e6),
            background = Color(0xff424242),
            surface = Color(0xff424242),
            onPrimary = Color(0xffe6e6e6),
            onSecondary = Color(0xff424242),
            onBackground = Color(0xffe6e6e6),
            onSurface = Color(0xffe6e6e6),
            error = Color(0xff424242),
            onError = Color(0xffe6e6e6)
        )
    )
    @SuppressLint("ConflictingOnColor")
    object Gruvbox : JamlColors(
        lightColors = lightColors(
            primary = Color(0xfffbf1c7),
            primaryVariant = Color(0xfffbf1c7),
            secondary = Color(0xff427b58),
            background = Color(0xfffbf1c7),
            surface = Color(0xfffbf1c7),
            onPrimary = Color(0xff3c3836),
            onSecondary = Color(0xff665c54),
            onBackground = Color(0xff3c3836),
            onSurface = Color(0xff665c54),
            error = Color(0xfffbf1c7),
            onError = Color(0xff3c3836)
        ),
        darkColors = darkColors(
            primary = Color(0xff282828),
            primaryVariant = Color(0xff282828),
            secondary = Color(0xff8ec07c),
            background = Color(0xff282828),
            surface = Color(0xff282828),
            onPrimary = Color(0xffebdbb2),
            onSecondary = Color(0xffbdae93),
            onBackground = Color(0xffebdbb2),
            onSurface = Color(0xffebdbb2),
            error = Color(0xff282828),
            onError = Color(0xffebdbb2)
        )
    )

    @SuppressLint("ConflictingOnColor")
    object Nord : JamlColors(
        lightColors = lightColors(
            primary = Color(0xffECEFF4),
            primaryVariant = Color(0xffECEFF4),
            secondary = Color(0xff5E81AC),
            background = Color(0xffECEFF4),
            surface = Color(0xffECEFF4),
            onPrimary = Color(0xff3B4252),
            onSecondary = Color(0xff4C566A),
            onBackground = Color(0xff3B4252),
            onSurface = Color(0xff665c54),
            error = Color(0xffECEFF4),
            onError = Color(0xff3B4252)
        ),
        darkColors = darkColors(
            primary = Color(0xff2E3440),
            primaryVariant = Color(0xff2E3440),
            secondary = Color(0xff88C0D0),
            background = Color(0xff2E3440),
            surface = Color(0xff2E3440),
            onPrimary = Color(0xff3B4252),
            onSecondary = Color(0xff4C566A),
            onBackground = Color(0xff3B4252),
            onSurface = Color(0xff3B4252),
            error = Color(0xff2E3440),
            onError = Color(0xff3B4252)
        )
    )

}