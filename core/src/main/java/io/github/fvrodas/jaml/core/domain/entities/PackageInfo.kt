package io.github.fvrodas.jaml.core.domain.entities

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
@kotlinx.serialization.Serializable
data class PackageInfo(
    val packageName: String,
    val label: String,
    @Transient val icon: Bitmap? = null
) : Serializable, Parcelable {

    @Parcelize
    class ShortcutInfo(
        val id: String,
        val packageName: String,
        val label: String,
        @Transient val icon: Bitmap?
    ) : Serializable, Parcelable {
        companion object {
            private const val serialVersionUID: Long = 1L
        }
    }

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
