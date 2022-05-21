package io.github.fvrodas.jaml.framework.services

import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class JAMLNotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        Intent(NOTIFICATION_ACTION).apply {
            val bundle = Bundle().apply {
                putString("package_name", sbn?.packageName)
                putBoolean("has_notification", true)
                putString("notification_title", sbn?.notification?.extras?.get("android:title") as String?)
                putString("notification_text", sbn?.notification?.extras?.get("android:text") as String?)
            }
            putExtras(bundle)
            Log.d("NOTIFICATION_ADDED", "${sbn?.packageName}")
            this@JAMLNotificationService.sendBroadcast(this)
        }
        super.onNotificationPosted(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Intent(NOTIFICATION_ACTION).apply {
            val bundle = Bundle().apply {
                putString("package_name", sbn?.packageName)
                putBoolean("has_notification", false)
                putString("notification_title", sbn?.notification?.extras?.get("android:title") as String?)
                putString("notification_text", sbn?.notification?.extras?.get("android:text") as String?)
            }
            putExtras(bundle)
            Log.d("NOTIFICATION_DELETE", "${sbn?.packageName}")
            this@JAMLNotificationService.sendBroadcast(this)
        }

        super.onNotificationRemoved(sbn)
    }

    companion object {
        const val NOTIFICATION_ACTION = "io.github.fvrodas.jaml.NOTIFICATION_EVENT"
    }
}