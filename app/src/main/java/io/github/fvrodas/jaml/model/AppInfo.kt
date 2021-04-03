package io.github.fvrodas.jaml.model

import android.graphics.Bitmap
import java.io.Serializable

class AppInfo(var packageName: String, var label: String, var icon: Bitmap? = null) : Serializable