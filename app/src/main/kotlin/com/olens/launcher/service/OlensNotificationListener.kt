package com.olens.launcher.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class OlensNotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            val pkg = it.packageName
            Log.d("OlensNotification", "Notification posted for: $pkg")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let {
            val pkg = it.packageName
            Log.d("OlensNotification", "Notification removed for: $pkg")
        }
    }
}
