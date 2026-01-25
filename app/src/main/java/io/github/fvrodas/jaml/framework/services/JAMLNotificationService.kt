package io.github.fvrodas.jaml.framework.services

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import io.github.fvrodas.jaml.R
import io.github.fvrodas.jaml.framework.LauncherEventBus
import io.github.fvrodas.jaml.framework.LauncherEvents

class JAMLNotificationService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        activeNotifications
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val counter = activeNotifications.count { it.packageName == sbn?.packageName }

        val notificationText = sbn?.notification?.extras?.getString("android.title")?.let {
            "($counter) $it"
        } ?: sbn?.notification?.extras?.getCharSequence(
            "android.text"
        )?.let {
            "($counter) $it"
        } ?: this.getString(R.string.notification_default_title)

        LauncherEventBus.postEvent(
            LauncherEvents.OnNotificationChanged(
                packageName = sbn?.packageName,
                message = notificationText
            )
        )
        super.onNotificationPosted(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        LauncherEventBus.postEvent(
            LauncherEvents.OnNotificationChanged(
                packageName = sbn?.packageName,
                message = null
            )
        )
        super.onNotificationRemoved(sbn)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        //tryReEnableNotificationListener(this)
    }

    companion object {
        internal fun isNotificationListenerServiceEnabled(context: Context): Boolean {
            return NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)
        }

        @Deprecated("Modern Android versions may not need this method")
        fun tryReEnableNotificationListener(context: Context) {

            try {
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
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(this::class.java.name, e.message ?: e.toString())
            }
        }
    }
}
