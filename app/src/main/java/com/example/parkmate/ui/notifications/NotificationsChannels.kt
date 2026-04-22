package com.example.parkmate.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {
    const val CHANNEL_REMINDERS = "parking_reminders"
    const val CHANNEL_TICKETS = "ticket_expiry"

    fun create(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val reminders = NotificationChannel(
            CHANNEL_REMINDERS,
            "Parking reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Ongoing reminders for active parking sessions"
        }

        val tickets = NotificationChannel(
            CHANNEL_TICKETS,
            "Ticket expiry alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Warnings and expiry alerts for fixed-ticket parking"
        }

        manager.createNotificationChannel(reminders)
        manager.createNotificationChannel(tickets)
    }
}