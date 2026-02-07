package io.github.fvrodas.jaml.ui.common.models

import android.icu.util.Calendar
import android.os.Parcelable
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class LauncherEntry(
    @Contextual val packageInfo: PackageInfo,
    var notificationTitle: String? = null,
    var group: String? = null,
    private var timeStamp: Long = -1,
) : Parcelable {

    val order: Long get() = timeStamp


    val hasNotification: Boolean get() = notificationTitle != null

    val movedToHome: Boolean get() = group == DEFAULT_GROUP

    fun moveToHomeScreen(order: Long? = null) {
        moveToGroup(DEFAULT_GROUP)
        timeStamp = order ?: Calendar.getInstance().timeInMillis
    }

    fun moveToGroup(groupName: String) {
        group = groupName
    }

    fun moveToDrawer() {
        group = null
        timeStamp = -1
    }

    companion object {
        const val DEFAULT_GROUP = "desktop"
    }
}

fun PackageInfo.toLauncherEntry() = LauncherEntry(this)

fun LauncherEntry.toPackageInfo() = packageInfo
