package com.example.parkmate.ui.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import com.example.parkmate.MainActivity
import com.example.parkmate.R

object GeofenceNotificationHelper {

    const val CHANNEL_GEOFENCE = "geofence_events"

    fun sendTransitionNotification(
        context: Context,
        locationId: Long,
        locationName: String,
        transition: String
    ) {
        if (!hasNotificationPermission(context)) return

        val title: String
        val text: String
        val notificationId: Int

        if (transition == "ENTER") {
            title = "Entered $locationName"
            text = "You are near a saved parking area. Want to start a parking session?"
            notificationId = (8000 + locationId).toInt()
        } else {
            title = "Exited $locationName"
            text = "You left the saved parking area. Want to end the related parking session?"
            notificationId = (9000 + locationId).toInt()
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(
                NotificationNavigation.EXTRA_OPEN_ROUTE,
                NotificationNavigation.ROUTE_ACTIVE_MAP
            )
        }

        val pendingIntent = TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(launchIntent)
            .getPendingIntent(
                notificationId,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.park_icon
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_GEOFENCE)
            .setSmallIcon(R.drawable.ic_stat_parking_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
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
}