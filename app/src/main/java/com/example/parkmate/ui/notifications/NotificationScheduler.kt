package com.example.parkmate.ui.notifications

import android.os.Build
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/*
Centralized scheduler for all time-based events in the app.
It uses WorkManager for broad periodic tasks (Reminders)
and AlarmManager for time-critical events (Ticket Expiry).
*/
object NotificationScheduler {

    private const val REMINDER_WORK_NAME = "parking_reminder_work"
    private const val WARNING_MINUTES_DEFAULT = 15L

    /*
    Schedules a periodic background worker that checks for active parking sessions.
    If a session is found, it shows a notification to remind the user.
    Frequency: ~15 minutes.
    */
    fun scheduleParkingReminder(context: Context) {
        val request = PeriodicWorkRequestBuilder<ParkingReminderWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /*
    Stops the background reminder worker. Called when no parking sessions are active.
    */
    fun cancelParkingReminder(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(REMINDER_WORK_NAME)
    }

    fun scheduleRecurringParkingReminders(context: Context) {
        scheduleParkingReminder(context)
    }

    fun cancelRecurringParkingReminders(context: Context) {
        cancelParkingReminder(context)
    }

    /*
    Schedules two alarms for Fixed Ticket sessions:
    1. A warning alarm (e.g., 15 mins before expiry)
    2. An expiry alarm
    Uses AlarmManager.setExactAndAllowWhileIdle to ensure triggers even in Doze mode.
    */
    fun scheduleFixedTicketAlarms(
        context: Context,
        sessionId: Long,
        locationName: String?,
        expiryMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Warning Alarm
        val warningAt = expiryMillis - TimeUnit.MINUTES.toMillis(WARNING_MINUTES_DEFAULT)
        if (warningAt > System.currentTimeMillis()) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                warningAt,
                ticketPendingIntent(
                    context = context,
                    sessionId = sessionId,
                    locationName = locationName,
                    mode = TicketAlarmReceiver.MODE_WARNING,
                    requestCodeOffset = 10_000
                )
            )
        }

        // Expiry Alarm
        val expiredPendingIntent = ticketPendingIntent(
            context = context,
            sessionId = sessionId,
            locationName = locationName,
            mode = TicketAlarmReceiver.MODE_EXPIRED,
            requestCodeOffset = 20_000
        )

        try {
            // Android 12+ requires special permissions for EXACT alarms
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        expiryMillis,
                        expiredPendingIntent
                    )
                } else {
                    // Fallback to inexact if permission is missing
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        expiryMillis,
                        expiredPendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    expiryMillis,
                    expiredPendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                expiryMillis,
                expiredPendingIntent
            )
        }
    }

    /*
    Cancels any pending alarms related to a specific session.
    */
    fun cancelFixedTicketAlarms(
        context: Context,
        sessionId: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(
            ticketPendingIntent(
                context = context,
                sessionId = sessionId,
                locationName = null,
                mode = TicketAlarmReceiver.MODE_WARNING,
                requestCodeOffset = 10_000
            )
        )

        alarmManager.cancel(
            ticketPendingIntent(
                context = context,
                sessionId = sessionId,
                locationName = null,
                mode = TicketAlarmReceiver.MODE_EXPIRED,
                requestCodeOffset = 20_000
            )
        )
    }

    /*
    Helper to create unique PendingIntents for different sessions and alarm types.
    */
    private fun ticketPendingIntent(
        context: Context,
        sessionId: Long,
        locationName: String?,
        mode: String,
        requestCodeOffset: Int
    ): PendingIntent {
        val intent = Intent(context, TicketAlarmReceiver::class.java).apply {
            putExtra(TicketAlarmReceiver.EXTRA_SESSION_ID, sessionId)
            putExtra(TicketAlarmReceiver.EXTRA_LOCATION_NAME, locationName)
            putExtra(TicketAlarmReceiver.EXTRA_MODE, mode)
        }

        return PendingIntent.getBroadcast(
            context,
            (requestCodeOffset + sessionId).toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
