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
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.parkmate.MainActivity
import com.example.parkmate.R
import com.example.parkmate.data.local.ParkMateDatabase
import com.example.parkmate.data.model.ParkingType
import com.example.parkmate.ui.util.calculateSessionCostNow
import com.example.parkmate.ui.util.formatCountdown
import com.example.parkmate.ui.util.formatDurationCompact

class ParkingReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!hasNotificationPermission(applicationContext)) {
            return Result.success()
        }

        val db = ParkMateDatabase.getDatabase(applicationContext)
        val activeSessions = db.parkingSessionDao().getActiveSessionsOnce()

        if (activeSessions.isEmpty()) return Result.success()

        val session = activeSessions.first()
        val now = System.currentTimeMillis()
        val elapsedMillis = now - session.startTimeMillis
        val elapsed = formatDurationCompact(elapsedMillis)

        val justStarted = elapsedMillis < 60_000L

        val message = when (session.parkingType) {
            ParkingType.FREE -> {
                if (justStarted) {
                    applicationContext.getString(R.string.parking_just_started)
                } else {
                    applicationContext.getString(R.string.parking_active_for_format, elapsed)
                }
            }

            ParkingType.HOURLY_PAID -> {
                val cost = calculateSessionCostNow(session, now)
                if (justStarted) {
                    applicationContext.getString(
                        R.string.parking_just_started_cost_format,
                        cost
                    )
                } else {
                    applicationContext.getString(
                        R.string.parking_active_for_cost_format,
                        elapsed,
                        cost
                    )
                }
            }

            ParkingType.FIXED_TICKET -> {
                val countdown = formatCountdown(applicationContext, session.fixedTicketExpiryMillis, now)
                applicationContext.getString(
                    R.string.fixed_ticket_active_countdown_format,
                    countdown
                )
            }
        }

        val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra(
                NotificationNavigation.EXTRA_OPEN_ROUTE,
                NotificationNavigation.ROUTE_ACTIVE_MAP
            )
            putExtra(NotificationNavigation.EXTRA_SESSION_ID, session.id)
        }

        val contentPendingIntent = TaskStackBuilder.create(applicationContext)
            .addNextIntentWithParentStack(launchIntent)
            .getPendingIntent(
                5001,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val largeIcon = BitmapFactory.decodeResource(
            applicationContext.resources,
            R.drawable.park_icon
        )

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannels.CHANNEL_REMINDERS
        )
            .setSmallIcon(R.drawable.ic_stat_parking_notification)
            .setLargeIcon(largeIcon)
            .setContentTitle(session.locationName ?: applicationContext.getString(R.string.parking_active))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(1001, notification)

        return Result.success()
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return android.os.Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }
}
