package io.github.fvrodas.jaml.core.domain.entities

import android.graphics.Bitmap
import java.io.Serializable

class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Bitmap? = null,
    var hasNotification: Boolean = false
) : Serializable {

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + label.hashCode()
        result = 31 * result + (icon?.hashCode() ?: 0)
        result = 31 * result + hasNotification.hashCode()
        return result
    }
}
