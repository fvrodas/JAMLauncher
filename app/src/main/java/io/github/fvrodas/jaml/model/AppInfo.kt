package io.github.fvrodas.jaml.model

import android.graphics.Bitmap
import java.io.Serializable

class AppInfo(val packageName: String, val label: String, val icon: Bitmap? = null, var hasNotification: Boolean = false) : Serializable