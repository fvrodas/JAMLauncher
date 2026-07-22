package io.github.fvrodas.jaml.ui.common.themes

import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.ui.common.settings.LauncherTheme

val colorSchemeByName = mapOf(
    R.string.colorscheme_default to JamlColorScheme.Default,
    R.string.colorscheme_gruvbox to JamlColorScheme.Gruvbox,
    R.string.colorscheme_nord to JamlColorScheme.Nord,
    R.string.colorscheme_solarized to JamlColorScheme.Solarized,
    R.string.colorscheme_tokyo_night to JamlColorScheme.TokyoNight,
)

val launcherThemeByName = mapOf(
    R.string.theme_light to LauncherTheme.Light,
    R.string.theme_dark to LauncherTheme.Dark,
    R.string.theme_system to LauncherTheme.System
)
