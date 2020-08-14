package com.momentolabs.app.security.applocker.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.momentolabs.app.security.applocker.R


class ServiceNotificationManager(val context: Context) {

    fun createNotification(): Notification {
        createAppLockerServiceChannel()

        val resultIntent: Intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID_CALLRECEIVER_SERVICE)

        val resultPendingIntent =
            PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return NotificationCompat.Builder(context, CHANNEL_ID_CALLRECEIVER_SERVICE)
            .setSmallIcon(R.drawable.ic_locked_24px)
            .setContentTitle("Protecting apps")
            .setContentText("You may hide this notification, click me")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(resultPendingIntent)
            .build()
    }

    private fun createAppLockerServiceChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
            val importance = android.app.NotificationManager.IMPORTANCE_LOW
            val channel =
                NotificationChannel(CHANNEL_ID_CALLRECEIVER_SERVICE, name, importance).apply {
                    description = descriptionText
                }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID_CALLRECEIVER_SERVICE = "CHANNEL_ID_CALLRECEIVER_SERVICE"
    }
}