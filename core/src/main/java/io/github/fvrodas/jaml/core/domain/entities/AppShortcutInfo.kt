package io.github.fvrodas.jaml.core.domain.entities

import android.graphics.Bitmap
import java.io.Serializable

class AppShortcutInfo(
    val id: String,
    val packageName: String,
    val label: String,
    val icon: Bitmap?
) : Serializable {

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + (icon?.hashCode() ?: 0)
        return result
    }
}