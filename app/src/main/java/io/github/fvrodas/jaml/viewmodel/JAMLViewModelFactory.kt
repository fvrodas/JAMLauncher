package io.github.fvrodas.jaml.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class JAMLViewModelFactory(
        private val application: Application,
        private val packageManager: PackageManager?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        packageManager?.let {
            if (modelClass.isAssignableFrom(AppsViewModel::class.java)) {
                return modelClass.cast(AppsViewModel(application, it))!!
            }
        }
        throw IllegalArgumentException("Wrong view model!!")
    }
}