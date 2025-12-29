package io.github.fvrodas.jaml.core.domain.entities

import android.graphics.Bitmap
import kotlinx.serialization.Transient

@kotlinx.serialization.Serializable
data class PackageInfo(
    val packageName: String,
    val label: String,
    @Transient val icon: Bitmap? = null,
    var hasNotification: Boolean = false,
    var groupName: String? = null
) {

    @kotlinx.serialization.Serializable
    class ShortcutInfo(
        val id: String,
        val packageName: String,
        val label: String,
        @Transient val icon: Bitmap? = null
    )
}
