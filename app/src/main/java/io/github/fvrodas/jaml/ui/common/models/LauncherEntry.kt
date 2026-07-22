package io.github.fvrodas.jaml.ui.common.models

import android.icu.util.Calendar
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import io.github.fvrodas.jaml.core.domain.entities.PackageInfo
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
@Immutable
data class LauncherEntry(
    @Contextual val packageInfo: PackageInfo,
    val notificationTitle: String? = null,
    val group: String? = null,
    val order: Long = -1,
) : Parcelable {

    val hasNotification: Boolean get() = notificationTitle != null

    val movedToHome: Boolean get() = group == DEFAULT_GROUP

    fun moveToHomeScreen(order: Long? = null): LauncherEntry =
        copy(group = DEFAULT_GROUP, order = order ?: Calendar.getInstance().timeInMillis)

    fun moveToGroup(groupName: String): LauncherEntry =
        copy(group = groupName)

    fun moveToDrawer(): LauncherEntry =
        copy(group = null, order = -1)

    companion object {
        const val DEFAULT_GROUP = "desktop"
    }
}

fun PackageInfo.toLauncherEntry() = LauncherEntry(this)

fun LauncherEntry.toPackageInfo() = packageInfo
