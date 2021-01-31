package io.github.fvrodas.jaml.viewmodel

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.*
import io.github.fvrodas.jaml.BuildConfig
import io.github.fvrodas.jaml.model.AppInfo
import io.github.fvrodas.jaml.ui.SettingsActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class AppsViewModel(application: Application, private val packageManager: PackageManager) : AndroidViewModel(
        application
) {
    val applicationsList: MutableLiveData<ArrayList<AppInfo>> = MutableLiveData()

    init {
        retrieveApplicationsList()
    }

    fun retrieveApplicationsList() {
        GlobalScope.launch {
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val apps: ArrayList<AppInfo> = ArrayList()
            packageManager.queryIntentActivities(intent, 0).iterator().apply {
                if (hasNext()) {
                    do {
                        val item = next()
                        if (!BuildConfig.APPLICATION_ID.contains(item.activityInfo.packageName)) {
                            apps.add(
                                    AppInfo(
                                            item.activityInfo.packageName,
                                            item.loadLabel(packageManager).toString()
                                    )
                            )
                        }
                    } while (hasNext())
                }
                apps.sortWith { t1, t2 ->
                    t1.label.toLowerCase(Locale.getDefault())
                            .compareTo(t2.label.toLowerCase(Locale.getDefault()))
                }

                apps.add(AppInfo(
                        SettingsActivity::class.java.name,
                        "Launcher Settings"
                ))

                applicationsList.postValue(apps)
            }

        }
    }

}