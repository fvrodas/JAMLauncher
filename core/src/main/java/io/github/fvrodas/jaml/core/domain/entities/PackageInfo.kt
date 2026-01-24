package io.github.fvrodas.jaml.core.domain.entities

import android.graphics.Bitmap
import java.io.Serializable

data class PackageInfo(
    val packageName: String,
    val label: String,
    @Transient val icon: Bitmap? = null,
    var hasNotification: Boolean = false,
    var notificationTitle: String? = null
) : Serializable {

    class ShortcutInfo(
        val id: String,
        val packageName: String,
        val label: String,
        @Transient val icon: Bitmap?
    ) : Serializable {
        companion object {
            private const val serialVersionUID: Long = 1L
        }
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
