package com.example.parkmate.ui.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.parkmate.ui.notifications.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetUpdateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                ParkingStatusWidgetUpdater.updateAllWidgets(appContext)

            } finally {
                pendingResult.finish()
            }
        }
    }
}