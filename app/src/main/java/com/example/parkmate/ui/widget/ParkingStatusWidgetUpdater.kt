package com.example.parkmate.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.parkmate.MainActivity
import com.example.parkmate.R
import com.example.parkmate.data.local.ParkMateDatabase
import com.example.parkmate.data.local.entity.ParkingSessionEntity
import com.example.parkmate.data.model.ParkingType
import com.example.parkmate.ui.util.calculateSessionCostNow
import com.example.parkmate.ui.util.formatCountdown
import com.example.parkmate.ui.util.formatDurationCompact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ParkingStatusWidgetUpdater {

    const val ACTION_REFRESH_WIDGET = "com.example.parkmate.ACTION_REFRESH_WIDGET"

    fun updateAllWidgets(context: Context) {
        val appContext = context.applicationContext
        val appWidgetManager = AppWidgetManager.getInstance(appContext)
        val componentName = ComponentName(appContext, ParkingStatusWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        updateAllWidgets(
            context = appContext,
            appWidgetManager = appWidgetManager,
            appWidgetIds = appWidgetIds
        )
    }

    fun updateAllWidgets(
        context: Context,
        pendingResult: BroadcastReceiver.PendingResult
    ) {
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateAllWidgetsInternal(appContext)
            } finally {
                pendingResult.finish()
            }
        }
    }

    fun updateAllWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        if (appWidgetIds.isEmpty()) return

        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            val db = ParkMateDatabase.getDatabase(appContext)
            val activeSession = db.parkingSessionDao()
                .getActiveSessionsOnce()
                .firstOrNull()

            withContext(Dispatchers.Main) {
                appWidgetIds.forEach { widgetId ->
                    val views = buildRemoteViews(appContext, activeSession)
                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            }
        }
    }

    fun hasActiveWidgets(context: Context): Boolean {
        val appContext = context.applicationContext
        val appWidgetManager = AppWidgetManager.getInstance(appContext)
        val componentName = ComponentName(appContext, ParkingStatusWidgetProvider::class.java)
        return appWidgetManager.getAppWidgetIds(componentName).isNotEmpty()
    }

    private suspend fun updateAllWidgetsInternal(context: Context) {
        val appContext = context.applicationContext
        val appWidgetManager = AppWidgetManager.getInstance(appContext)
        val componentName = ComponentName(appContext, ParkingStatusWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isEmpty()) return

        val db = ParkMateDatabase.getDatabase(appContext)
        val activeSession = db.parkingSessionDao()
            .getActiveSessionsOnce()
            .firstOrNull()

        withContext(Dispatchers.Main) {
            appWidgetIds.forEach { widgetId ->
                val views = buildRemoteViews(appContext, activeSession)
                appWidgetManager.updateAppWidget(widgetId, views)
            }
        }
    }

    private fun buildRemoteViews(
        context: Context,
        activeSession: ParkingSessionEntity?
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_parking_status)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            9001,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val refreshIntent = Intent(context, ParkingStatusWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_WIDGET
        }

        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            9002,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_open_button, openAppPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent)

        if (activeSession == null) {
            views.setTextViewText(
                R.id.widget_title,
                context.getString(R.string.no_active_parking)
            )
            views.setTextViewText(
                R.id.widget_subtitle,
                context.getString(R.string.tap_to_open_parkmate)
            )
            views.setTextViewText(
                R.id.widget_status,
                context.getString(R.string.ready)
            )
            views.setTextViewText(
                R.id.widget_action,
                context.getString(R.string.open_app)
            )
            views.setTextViewText(
                R.id.widget_open_button,
                context.getString(R.string.open_app)
            )
            return views
        }

        val now = System.currentTimeMillis()
        val locationName = activeSession.locationName
            ?: context.getString(R.string.parking_active)

        val subtitle = when (activeSession.parkingType) {
            ParkingType.FREE -> {
                val elapsed = formatDurationCompact(now - activeSession.startTimeMillis)
                context.getString(R.string.free_parking_format, elapsed)
            }

            ParkingType.HOURLY_PAID -> {
                val elapsed = formatDurationCompact(now - activeSession.startTimeMillis)
                val cost = calculateSessionCostNow(activeSession, now)
                context.getString(R.string.hourly_parking_format, elapsed, cost)
            }

            ParkingType.FIXED_TICKET -> {
                val countdown = formatCountdown(activeSession.fixedTicketExpiryMillis, now)
                context.getString(R.string.fixed_ticket_format, countdown)
            }
        }

        val status = when (activeSession.parkingType) {
            ParkingType.FREE -> context.getString(R.string.parking_active)
            ParkingType.HOURLY_PAID -> context.getString(R.string.cost_updating)
            ParkingType.FIXED_TICKET -> context.getString(R.string.ticket_countdown)
        }

        views.setTextViewText(R.id.widget_title, locationName)
        views.setTextViewText(R.id.widget_subtitle, subtitle)
        views.setTextViewText(R.id.widget_status, status)
        views.setTextViewText(R.id.widget_action, context.getString(R.string.open_map))
        views.setTextViewText(R.id.widget_open_button, context.getString(R.string.open_map))

        return views
    }
}