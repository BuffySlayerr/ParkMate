package com.example.parkmate.ui.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.parkmate.data.local.entity.SavedLocationEntity
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

object GeofenceHelper {

    private const val TAG = "GeofenceHelper"

    private fun geofencingClient(context: Context): GeofencingClient =
        LocationServices.getGeofencingClient(context)

    /**
     * Controlla se l'app ha i permessi necessari per il Geofencing.
     * Nota: Su Android 10+ è necessario ACCESS_BACKGROUND_LOCATION per i geofence in background.
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return fineLocationGranted && backgroundLocationGranted
    }

    fun geofencePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            7001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE // Importante: deve essere MUTABLE per Geofencing
        )
    }

    fun addGeofence(
        context: Context,
        location: SavedLocationEntity,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (!hasLocationPermission(context)) {
            Log.e(TAG, "Missing necessary location permissions for Geofencing")
            return
        }

        val geofence = Geofence.Builder()
            .setRequestId(location.id.toString())
            .setCircularRegion(
                location.latitude,
                location.longitude,
                location.geofenceRadiusMeters
            )
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .setLoiteringDelay(10_000)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        try {
            geofencingClient(context)
                .addGeofences(request, geofencePendingIntent(context))
                .addOnSuccessListener {
                    Log.d(TAG, "Geofence added successfully for: ${location.name}")
                    onSuccess()
                }
                .addOnFailureListener {
                    Log.e(TAG, "Failed to add geofence for: ${location.name}", it)
                    onError(it)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException while adding geofence", e)
        }
    }

    fun removeGeofence(
        context: Context,
        locationId: Long,
        onDone: () -> Unit = {}
    ) {
        geofencingClient(context)
            .removeGeofences(listOf(locationId.toString()))
            .addOnCompleteListener {
                Log.d(TAG, "Geofence removal attempted for ID: $locationId")
                onDone()
            }
    }

    fun refreshAllGeofences(
        context: Context,
        locations: List<SavedLocationEntity>
    ) {
        geofencingClient(context)
            .removeGeofences(geofencePendingIntent(context))
            .addOnCompleteListener {
                Log.d(TAG, "Removed all previous geofences, refreshing...")
                locations
                    .filter { it.geofenceEnabled }
                    .forEach { location ->
                        addGeofence(context, location)
                    }
            }
    }
}