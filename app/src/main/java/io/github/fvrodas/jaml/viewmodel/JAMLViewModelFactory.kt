package io.github.fvrodas.jaml.viewmodel

import android.app.Application
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class JAMLViewModelFactory(
    private val application: Application,
    private val packageName: String?,
    private val densityDpi: Int = -1,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppsViewModel::class.java)) {
            return modelClass.cast(AppsViewModel(application))!!
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            if (modelClass.isAssignableFrom(ShortcutsViewModel::class.java)) {
                return modelClass.cast(
                        ShortcutsViewModel(
                                application,
                                packageName ?: "",
                                densityDpi
                        )
                )!!
            }
        }

        throw IllegalArgumentException("Wrong view model!!")
    }
}