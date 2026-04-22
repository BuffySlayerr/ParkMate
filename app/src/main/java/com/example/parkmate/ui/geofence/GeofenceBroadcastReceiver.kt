package com.example.parkmate.ui.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.parkmate.data.local.ParkMateDatabase
import com.example.parkmate.ui.notifications.GeofenceNotificationHelper
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) return

        val transition = when (event.geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "ENTER"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "EXIT"
            else -> return
        }

        val triggeringIds = event.triggeringGeofences
            ?.mapNotNull { it.requestId.toLongOrNull() }
            .orEmpty()

        if (triggeringIds.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            val db = ParkMateDatabase.getDatabase(context)
            val dao = db.savedLocationDao()

            triggeringIds.forEach { id ->
                val location = dao.getSavedLocationById(id) ?: return@forEach
                GeofenceNotificationHelper.sendTransitionNotification(
                    context = context,
                    locationId = location.id,
                    locationName = location.name,
                    transition = transition
                )
            }
        }
    }
}