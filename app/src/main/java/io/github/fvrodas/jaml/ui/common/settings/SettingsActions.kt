package io.github.fvrodas.jaml.ui.common.settings

import android.net.Uri

interface SettingsActions {
    fun isDefaultHome() : Boolean
    fun setAsDefaultHome()
    fun setWallpaper()
    fun enableNotificationAccess()
    fun openWebPage(url: Uri)
}
