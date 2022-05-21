package io.github.fvrodas.jaml.features.common.extensions

import android.content.Context
import android.util.TypedValue
import androidx.appcompat.view.ContextThemeWrapper

fun Context.deviceAccentColor(): Int {
    val typedValue = TypedValue()
    val contextThemeWrapper = ContextThemeWrapper(
        this,
        android.R.style.Theme_DeviceDefault
    )
    contextThemeWrapper.theme.resolveAttribute(
        android.R.attr.colorAccent,
        typedValue, true
    )
    return typedValue.data
}