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
                putString(NOTIF_PACKAGE_NAME, sbn?.packageName)
                putBoolean(NOTIF_HAS_NOTIFICATION, true)
                putString(NOTIF_TITLE, sbn?.notification?.extras?.getString("android:title"))
                putString(NOTIF_TEXT, sbn?.notification?.extras?.getString("android:text"))
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
                putString(NOTIF_PACKAGE_NAME, sbn?.packageName)
                putBoolean(NOTIF_HAS_NOTIFICATION, false)
                putString(NOTIF_TITLE, sbn?.notification?.extras?.getString("android:title"))
                putString(NOTIF_TEXT, sbn?.notification?.extras?.getString("android:text"))
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

const val NOTIF_PACKAGE_NAME = "package_name"
const val NOTIF_HAS_NOTIFICATION = "has_notification"
const val NOTIF_TITLE = "notification_title"
const val NOTIF_TEXT = "notification_text"
