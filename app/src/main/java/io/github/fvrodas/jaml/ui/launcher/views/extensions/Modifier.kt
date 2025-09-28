package io.github.fvrodas.jaml.ui.launcher.views.extensions

import androidx.compose.ui.Modifier

fun Modifier.applyIf(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier {
    return if(condition) modifier() else this
}
