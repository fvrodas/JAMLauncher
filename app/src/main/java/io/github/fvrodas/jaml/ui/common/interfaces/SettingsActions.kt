package io.github.fvrodas.jaml.ui.common.interfaces

import android.net.Uri

interface SettingsActions {
    fun isDefaultHome() : Boolean
    fun setAsDefaultHome()
    fun setWallpaper()
    fun enableNotificationAccess()
    fun openWebPage(url: Uri)
}
