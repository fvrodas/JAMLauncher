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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppShortcutInfo

        if (id != other.id) return false
        if (packageName != other.packageName) return false
        if (label != other.label) return false
        if (icon != other.icon) return false

        return true
    }


}