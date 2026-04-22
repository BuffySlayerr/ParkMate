package com.example.parkmate.ui.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val REMINDER_WORK_NAME = "parking_reminder_work"
    private const val WARNING_MINUTES_DEFAULT = 15L

    fun scheduleRecurringParkingReminders(context: Context) {
        val request = PeriodicWorkRequestBuilder<ParkingReminderWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelRecurringParkingReminders(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(REMINDER_WORK_NAME)
    }

    fun scheduleFixedTicketAlarms(
        context: Context,
        sessionId: Long,
        locationName: String?,
        expiryMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

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

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            expiryMillis,
            ticketPendingIntent(
                context = context,
                sessionId = sessionId,
                locationName = locationName,
                mode = TicketAlarmReceiver.MODE_EXPIRED,
                requestCodeOffset = 20_000
            )
        )
    }

    fun cancelFixedTicketAlarms(context: Context, sessionId: Long) {
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