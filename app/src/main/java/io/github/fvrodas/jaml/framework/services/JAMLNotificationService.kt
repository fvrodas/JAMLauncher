package io.github.fvrodas.jaml.framework.services

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import io.github.fvrodas.jaml.framework.LauncherEventBus
import io.github.fvrodas.jaml.framework.LauncherEvents

class JAMLNotificationService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        getActiveNotifications()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        Log.d("NOTIFICATION_ADDED", "${sbn?.packageName}")
        LauncherEventBus.postEvent(
            LauncherEvents.OnNotificationChanged(
                packageName = sbn?.packageName,
                hasNotification = false
            )
        )
        super.onNotificationPosted(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Log.d("NOTIFICATION_DELETE", "${sbn?.packageName}")
        LauncherEventBus.postEvent(
            LauncherEvents.OnNotificationChanged(
                packageName = sbn?.packageName,
                hasNotification = false
            )
        )
        super.onNotificationRemoved(sbn)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        tryReEnableNotificationListener(this)
    }

    companion object {
        internal fun isNotificationListenerServiceEnabled(context: Context): Boolean {
            return NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)
        }

        fun tryReEnableNotificationListener(context: Context) {
            if (isNotificationListenerServiceEnabled(context)) {
                // Rebind the service if it's already enabled
                val componentName =
                    ComponentName(
                        context,
                        JAMLNotificationService::class.java,
                    )
                val pm = context.packageManager
                pm.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP,
                )
                pm.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP,
                )
            }
        }
    }
}
