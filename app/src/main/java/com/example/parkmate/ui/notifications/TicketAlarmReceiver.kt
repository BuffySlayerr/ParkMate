package com.example.parkmate.ui.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.example.parkmate.MainActivity
import com.example.parkmate.R

class TicketAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!hasNotificationPermission(context)) return

        val sessionId = intent.getLongExtra(EXTRA_SESSION_ID, -1L)
        val locationName = intent.getStringExtra(EXTRA_LOCATION_NAME) ?: "Parking ticket"
        val mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_EXPIRED

        val title: String
        val text: String
        val notificationId: Int

        when (mode) {
            MODE_WARNING -> {
                title = "Ticket expiring soon"
                text = "Your ticket at $locationName is about to expire."
                notificationId = (2000 + sessionId).toInt()
            }

            else -> {
                title = "Ticket expired"
                text = "Your fixed-ticket parking at $locationName has expired."
                notificationId = (3000 + sessionId).toInt()
            }
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(
                NotificationNavigation.EXTRA_OPEN_ROUTE,
                NotificationNavigation.ROUTE_ACTIVE_MAP
            )
            putExtra(NotificationNavigation.EXTRA_SESSION_ID, sessionId)
        }

        val contentPendingIntent = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(launchIntent)
            .getPendingIntent(
                (4000 + sessionId).toInt(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.park_icon
        )

        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.CHANNEL_TICKETS
        )
            .setSmallIcon(R.drawable.ic_stat_parking_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, notification)
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return android.os.Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val EXTRA_SESSION_ID = "extra_session_id"
        const val EXTRA_LOCATION_NAME = "extra_location_name"
        const val EXTRA_MODE = "extra_mode"

        const val MODE_WARNING = "warning"
        const val MODE_EXPIRED = "expired"
    }
}