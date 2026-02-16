package io.github.fvrodas.jaml

import android.content.Context

data class AppConfig(private val context: Context) {
    val packageName: String by lazy {
        context.packageName
    }

}
