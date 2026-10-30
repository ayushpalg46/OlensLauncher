package com.olens.launcher.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.olens.launcher.OlensApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class OlensNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "OlensNotification"
        private val _notificationsMap = MutableStateFlow<Map<String, String>>(emptyMap())
        val notificationsMap: StateFlow<Map<String, String>> = _notificationsMap.asStateFlow()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            val pkg = it.packageName
            val extras = it.notification.extras ?: return
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()

            val summary = when {
                !title.isNullOrBlank() && !text.isNullOrBlank() -> "$title: $text"
                !title.isNullOrBlank() -> title
                !text.isNullOrBlank() -> text
                else -> null
            }

            if (summary != null) {
                val current = _notificationsMap.value.toMutableMap()
                current[pkg] = summary
                _notificationsMap.value = current
                try {
                    OlensApplication.instance.appRepository.updateNotification(pkg, summary)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update notification in repository", e)
                }
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let {
            val pkg = it.packageName
            val current = _notificationsMap.value.toMutableMap()
            if (current.remove(pkg) != null) {
                _notificationsMap.value = current
                try {
                    OlensApplication.instance.appRepository.updateNotification(pkg, null)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to remove notification in repository", e)
                }
            }
        }
    }
}
