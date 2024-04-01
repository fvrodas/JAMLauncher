package io.github.fvrodas.jaml.framework.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_PACKAGE_ADDED
import android.content.Intent.ACTION_PACKAGE_CHANGED
import android.content.Intent.ACTION_PACKAGE_FULLY_REMOVED
import android.content.IntentFilter
import android.util.Log

class PackageChangedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            if (it.action == ACTION_PACKAGE_FULLY_REMOVED
                || it.action == ACTION_PACKAGE_ADDED
                || it.action == ACTION_PACKAGE_CHANGED
            ) {
                Log.d(PackageChangedReceiver::class.java.name, "${it.action}")
                CommunicationChannel.onPackageChangedReceived.invoke()
            }
        }
    }

    companion object {
        fun provideIntentFilter(): IntentFilter =
            IntentFilter().apply {
                addAction(ACTION_PACKAGE_FULLY_REMOVED)
                addAction(ACTION_PACKAGE_ADDED)
                addAction(ACTION_PACKAGE_CHANGED)
            }
    }

}

object CommunicationChannel {
    var onPackageChangedReceived: () -> Unit = {}
}