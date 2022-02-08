package io.github.fvrodas.jaml.model

import android.graphics.Bitmap
import java.io.Serializable

class AppShortcutInfo(
    val id: String,
    val packageName: String,
    val label: String,
    val icon: Bitmap?
) : Serializable