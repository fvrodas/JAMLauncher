package io.github.fvrodas.jaml.core.domain.entities

import android.graphics.Bitmap
import java.io.Serializable

data class PackageInfo(
    val packageName: String,
    val label: String,
    val icon: Bitmap? = null,
    var hasNotification: Boolean = false
) : Serializable {

    class ShortcutInfo(
        val id: String,
        val packageName: String,
        val label: String,
        val icon: Bitmap?
    ) : Serializable
}
