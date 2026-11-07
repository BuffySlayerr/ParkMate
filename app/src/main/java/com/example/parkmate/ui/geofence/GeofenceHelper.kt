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

/*
Helper object to manage Google Play Services Geofencing.
Geofencing allows the app to receive callbacks when the device enters or exits
specific geographical boundaries.
*/
object GeofenceHelper {

    private const val TAG = "GeofenceHelper"

    private fun geofencingClient(context: Context): GeofencingClient =
        LocationServices.getGeofencingClient(context)

    /**
     * Checks if the app has the required location permissions for Geofencing.
     * Note: Starting from Android 10 (API 29), ACCESS_BACKGROUND_LOCATION is strictly required
     * to trigger geofences while the app is not in the foreground.
     */
    fun hasLocationPermission(context: Context): Boolean {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Background permission check for Android 10+
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

    /**
     * Creates a PendingIntent that will trigger the GeofenceBroadcastReceiver.
     * Important: Must use FLAG_MUTABLE because the Geofencing system needs to 
     * append transition data to the intent.
     */
    fun geofencePendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            7001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    /**
     * Registers a circular geofence around a saved location.
     */
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

        // Define the geofence parameters
        val geofence = Geofence.Builder()
            .setRequestId(location.id.toString()) // Unique ID to identify this geofence
            .setCircularRegion(
                location.latitude,
                location.longitude,
                location.geofenceRadiusMeters
            )
            // Trigger on both entering and leaving the area
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .setLoiteringDelay(10_000) // 10 seconds before considering "dwell" transition
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .build()

        // Configure how the geofence request should behave initially
        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER) // Trigger immediately if already inside
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

    /**
     * Unregisters a specific geofence by its ID.
     */
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

    /**
     * Clears all current geofences and re-registers only the ones that are marked as enabled in DB.
     * Useful when permissions change or after a device reboot.
     */
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
