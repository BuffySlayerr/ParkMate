package com.example.parkmate.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.parkmate.MainActivity
import com.example.parkmate.R
import com.example.parkmate.data.local.ParkMateDatabase
import com.example.parkmate.data.model.ParkingType
import com.example.parkmate.ui.util.calculateSessionCostNow
import com.example.parkmate.ui.util.formatCountdown
import com.example.parkmate.ui.util.formatDurationCompact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ParkingStatusWidgetUpdater {

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, ParkingStatusWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        updateAllWidgets(context, appWidgetManager, appWidgetIds)
    }

    fun updateAllWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        if (appWidgetIds.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = ParkMateDatabase.getDatabase(context)
            val activeSession = db.parkingSessionDao().getActiveSessionsOnce().firstOrNull()

            appWidgetIds.forEach { widgetId ->
                val views = buildRemoteViews(context, activeSession)
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    private fun buildRemoteViews(
        context: Context,
        activeSession: com.example.parkmate.data.local.entity.ParkingSessionEntity?
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_parking_status)

        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            9001,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_open_button, openAppPendingIntent)

        if (activeSession == null) {
            views.setTextViewText(R.id.widget_title, "No active parking")
            views.setTextViewText(R.id.widget_subtitle, "Tap to open ParkMate")
            views.setTextViewText(R.id.widget_status, "Ready")
            views.setTextViewText(R.id.widget_action, "Open app")
            return views
        }

        val now = System.currentTimeMillis()
        val locationName = activeSession.locationName ?: "Active parking"

        val subtitle = when (activeSession.parkingType) {
            ParkingType.FREE -> {
                val elapsed = formatDurationCompact(now - activeSession.startTimeMillis)
                "Free · $elapsed"
            }

            ParkingType.HOURLY_PAID -> {
                val elapsed = formatDurationCompact(now - activeSession.startTimeMillis)
                val cost = calculateSessionCostNow(activeSession, now)
                "Hourly · $elapsed · € %.2f".format(cost)
            }

            ParkingType.FIXED_TICKET -> {
                val countdown = formatCountdown(activeSession.fixedTicketExpiryMillis, now)
                "Fixed ticket · $countdown"
            }
        }

        val status = when (activeSession.parkingType) {
            ParkingType.FREE -> "Parking active"
            ParkingType.HOURLY_PAID -> "Cost updating"
            ParkingType.FIXED_TICKET -> "Ticket countdown"
        }

        views.setTextViewText(R.id.widget_title, locationName)
        views.setTextViewText(R.id.widget_subtitle, subtitle)
        views.setTextViewText(R.id.widget_status, status)
        views.setTextViewText(R.id.widget_action, "Open map")

        return views
    }
}