package io.github.fvrodas.jaml.framework.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import io.github.fvrodas.jaml.framework.services.INotificationEventListener
import io.github.fvrodas.jaml.framework.services.JAMLNotificationService

class NotificationReceiver(private val listener: INotificationEventListener? = null) :
    BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            listener?.onNotificationEvent(
                packageName = it.getStringExtra("package_name"),
                hasNotification = intent.getBooleanExtra("has_notification", false)
            )
        }
    }

    companion object {
        fun provideIntentFilter(): IntentFilter =
            IntentFilter(JAMLNotificationService.NOTIFICATION_ACTION)
    }
}