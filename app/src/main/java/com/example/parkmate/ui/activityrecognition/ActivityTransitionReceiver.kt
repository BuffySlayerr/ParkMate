package com.example.parkmate.ui.activityrecognition

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.parkmate.MainActivity
import com.example.parkmate.R
import com.example.parkmate.data.local.ParkMateDatabase
import com.example.parkmate.ui.notifications.NotificationChannels
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityTransitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (!ActivityTransitionResult.hasResult(intent)) return
        if (!hasNotificationPermission(context)) return

        val result = ActivityTransitionResult.extractResult(intent) ?: return
        val events = result.transitionEvents

        val hasVehicleExit = events.any { event ->
            event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_EXIT &&
                    (
                            event.activityType == DetectedActivity.IN_VEHICLE ||
                                    event.activityType == DetectedActivity.ON_BICYCLE
                            )
        }

        val hasWalkingEnter = events.any { event ->
            event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER &&
                    (
                            event.activityType == DetectedActivity.WALKING ||
                                    event.activityType == DetectedActivity.ON_FOOT
                            )
        }

        if (!hasVehicleExit && !hasWalkingEnter) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = ParkMateDatabase.getDatabase(context)
            val activeSessions = db.parkingSessionDao().getActiveSessionsOnce()

            if (activeSessions.isEmpty()) {
                showNotification(
                    context = context,
                    notificationId = 7101,
                    title = context.getString(R.string.activity_recognition_new_park_title),
                    message = context.getString(R.string.activity_recognition_new_park_message)
                )
            } else {
                showNotification(
                    context = context,
                    notificationId = 7102,
                    title = context.getString(R.string.activity_recognition_end_park_title),
                    message = context.getString(R.string.activity_recognition_end_park_message)
                )
            }
        }
    }

    private fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String
    ) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.park_icon
        )

        val notification = NotificationCompat.Builder(
            context,
            NotificationChannels.CHANNEL_REMINDERS
        )
            .setSmallIcon(R.drawable.ic_stat_parking_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
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