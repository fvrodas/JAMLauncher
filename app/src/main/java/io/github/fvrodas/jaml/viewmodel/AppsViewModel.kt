package io.github.fvrodas.jaml.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.fvrodas.jaml.BuildConfig
import io.github.fvrodas.jaml.model.AppInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class AppsViewModel(application: Application, private val packageManager: PackageManager) : AndroidViewModel(
    application
) {
    var applicationsList: MutableLiveData<ArrayList<AppInfo>> = MutableLiveData()

    init {
        retrieveApplicationsList()
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun retrieveApplicationsList() {
        GlobalScope.launch {
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val apps: ArrayList<AppInfo> = ArrayList<AppInfo>()
            val it: Iterator<ResolveInfo> =
                packageManager.queryIntentActivities(intent, 0).iterator()
            if (it.hasNext()) {
                do {
                    val item = it.next()
                    if (!BuildConfig.APPLICATION_ID.contains(item.activityInfo.packageName)) {
                        apps.add(
                            AppInfo(
                                item.activityInfo.packageName,
                                item.loadLabel(packageManager).toString()
                            )
                        )
                    }
                } while (it.hasNext())
            }
            apps.sortWith { t1, t2 ->
                t1.label.toLowerCase(Locale.getDefault())
                    .compareTo(t2.label.toLowerCase(Locale.getDefault()))
            }

            applicationsList.postValue(apps)
        }
    }
}

class JAMLViewModelFactory(
    private val application: Application,
    private val packageManager: PackageManager?
): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        packageManager?.let {
            if (modelClass.isAssignableFrom(AppsViewModel::class.java)) {
                return modelClass.cast(AppsViewModel(application, it))!!
            }
        }
        throw IllegalArgumentException("Wrong view model!!")
    }
}