package com.example.parkmate.ui.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

class ParkingStatusWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        ParkingStatusWidgetUpdater.updateAllWidgets(
            context = context.applicationContext,
            pendingResult = pendingResult
        )
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        ParkingStatusWidgetUpdater.updateAllWidgets(
            context = context.applicationContext,
            appWidgetManager = appWidgetManager,
            appWidgetIds = appWidgetIds
        )
    }

    override fun onEnabled(context: Context) {
        ParkingStatusWidgetUpdater.updateAllWidgets(context.applicationContext)
    }
}