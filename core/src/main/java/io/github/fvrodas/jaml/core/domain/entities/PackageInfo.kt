package io.github.fvrodas.jaml.core.domain.entities

import android.graphics.Bitmap
import java.io.Serializable

data class PackageInfo(
    val packageName: String,
    val label: String,
    @Transient val icon: Bitmap? = null,
    var notificationTitle: String? = null,
    var group: String? = null
) : Serializable {

    val hasNotification: Boolean get() = notificationTitle != null

    val movedToHome: Boolean get() = group == HOME_GROUP


    fun moveToHomeScreen() {
        moveToGroup(HOME_GROUP)
    }

    fun moveToGroup(groupName: String) {
        group = groupName
    }

    fun moveToDrawer() {
        group = null
    }

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
        const val HOME_GROUP = "home_screen"
    }
}
