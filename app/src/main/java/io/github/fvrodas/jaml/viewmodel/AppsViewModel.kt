package io.github.fvrodas.jaml.viewmodel

import android.app.Application
import android.content.Intent
import android.content.pm.*
import android.graphics.*
import android.graphics.drawable.*
import android.os.Build
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.*
import io.github.fvrodas.jaml.BuildConfig
import io.github.fvrodas.jaml.model.AppInfo
import io.github.fvrodas.jaml.ui.SettingsActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class AppsViewModel(application: Application, private val packageManager: PackageManager) :
        AndroidViewModel(
                application
        ) {
    private val cachedApplicationsList: MutableLiveData<ArrayList<AppInfo>> = MutableLiveData()
    val filteredApplicationsList: MutableLiveData<ArrayList<AppInfo>> = MutableLiveData()

    init {
        retrieveApplicationsList()
    }

    fun retrieveApplicationsList() {
        GlobalScope.launch {
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val apps: ArrayList<AppInfo> = ArrayList()
            packageManager.queryIntentActivities(intent, 0).apply {
                if (this.toList().count() != cachedApplicationsList.value?.count() ?: 0) {
                    this.iterator().apply {
                        if (hasNext()) {
                            do {
                                val item = next()
                                if (!BuildConfig.APPLICATION_ID.contains(item.activityInfo.packageName)) {
                                    apps.add(
                                            AppInfo(
                                                    item.activityInfo.packageName,
                                                    item.loadLabel(packageManager).toString(),
                                                    loadIcon(item)
                                            )
                                    )
                                }
                            } while (hasNext())
                        }
                        apps.sortWith { t1, t2 ->
                            t1.label.toLowerCase(Locale.getDefault())
                                    .compareTo(t2.label.toLowerCase(Locale.getDefault()))
                        }

                        apps.add(
                                AppInfo(
                                        SettingsActivity::class.java.name,
                                        "Launcher Settings",
                                )
                        )

                        cachedApplicationsList.postValue(apps)
                        filteredApplicationsList.postValue(apps)
                    }
                }
            }
        }
    }

    fun filterApplicationsList(query: String) {
        val filtered = ArrayList(cachedApplicationsList.value?.filter { it.label.contains(query, ignoreCase = true) }?.toList())
        filteredApplicationsList.value = filtered
    }

    private fun loadIcon(item: ResolveInfo): Bitmap {
        if (iconCache.containsKey(item.activityInfo.packageName)) {
            return iconCache[item.activityInfo.packageName]!!
        } else {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val drawable = packageManager.getApplicationIcon(item.activityInfo.packageName)
                if (drawable is AdaptiveIconDrawable) {
                    iconCache[item.activityInfo.packageName] = drawable.toBitmap()
                    drawable.toBitmap()
                } else {
                    val scaled = InsetDrawable(drawable, 0.28f)
                    scaled.bounds = drawable.bounds
                    AdaptiveIconDrawable(ColorDrawable(Color.WHITE), scaled).toBitmap()
                }
            } else {
                iconCache[item.activityInfo.packageName] =
                        item.activityInfo.loadIcon(packageManager).toBitmap()
                item.activityInfo.loadIcon(packageManager).toBitmap()
            }
        }
    }
}

val iconCache: HashMap<String, Bitmap> = HashMap()