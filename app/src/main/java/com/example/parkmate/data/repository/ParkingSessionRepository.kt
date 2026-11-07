package com.example.parkmate.data.repository

import android.content.Context
import com.example.parkmate.data.local.dao.ParkingSessionDao
import com.example.parkmate.data.local.dao.VehicleDao
import com.example.parkmate.data.local.entity.ParkingSessionEntity
import com.example.parkmate.data.model.ParkingType
import com.example.parkmate.ui.notifications.NotificationScheduler
import com.example.parkmate.ui.widget.ParkingStatusWidgetUpdater
import kotlinx.coroutines.flow.Flow
import kotlin.math.ceil

/*
Repository responsible for managing parking sessions.
It coordinates database operations with system-level services like Notifications and Widgets.
*/
class ParkingSessionRepository(
    private val context: Context,
    private val parkingSessionDao: ParkingSessionDao,
    private val vehicleDao: VehicleDao
) {

    /**
     * Returns a stream of all parking sessions, sorted by date.
     */
    fun getAllSessions(): Flow<List<ParkingSessionEntity>> =
        parkingSessionDao.getAllSessions()

    /**
     * Returns a stream of currently active parking sessions.
     */
    fun getActiveSessions(): Flow<List<ParkingSessionEntity>> =
        parkingSessionDao.getActiveSessions()

    /**
     * Starts a new parking session.
     * This method handles:
     * 1. DB insertion
     * 2. Updating vehicle parked status
     * 3. Scheduling periodic reminders
     * 4. Scheduling exact alarms for fixed tickets (if applicable)
     * 5. Triggering widget updates
     */
    suspend fun startSession(
        vehicleId: Long,
        parkingType: ParkingType,
        latitude: Double,
        longitude: Double,
        locationName: String?,
        hourlyRate: Double?,
        fixedTicketCost: Double?,
        fixedTicketExpiryMillis: Long?,
        note: String?,
        photoUri: String?
    ) {
        val insertedSessionId = parkingSessionDao.insertSession(
            ParkingSessionEntity(
                vehicleId = vehicleId,
                parkingType = parkingType,
                latitude = latitude,
                longitude = longitude,
                locationName = locationName,
                startTimeMillis = System.currentTimeMillis(),
                hourlyRate = hourlyRate,
                fixedTicketCost = fixedTicketCost,
                fixedTicketExpiryMillis = fixedTicketExpiryMillis,
                note = note,
                photoUri = photoUri
            )
        )

        // Mark the vehicle as parked in the DB
        vehicleDao.updateParkedState(vehicleId, true)

        // Start background reminders (e.g., every 15-30 mins)
        NotificationScheduler.scheduleParkingReminder(context)

        // If it's a fixed ticket, schedule system alarms for expiry warnings
        if (parkingType == ParkingType.FIXED_TICKET && fixedTicketExpiryMillis != null) {
            NotificationScheduler.scheduleFixedTicketAlarms(
                context = context,
                sessionId = insertedSessionId,
                locationName = locationName,
                expiryMillis = fixedTicketExpiryMillis
            )
        }

        // Force a refresh of all home screen widgets
        ParkingStatusWidgetUpdater.updateAllWidgets(context)
    }

    /**
     * Ends an active parking session.
     * Calculates the final cost based on the duration and parking rules.
     */
    suspend fun endSession(session: ParkingSessionEntity) {
        val now = System.currentTimeMillis()
        val durationMillis = now - session.startTimeMillis

        // Determine final cost based on parking type
        val finalCost = when (session.parkingType) {
            ParkingType.FREE -> 0.0

            ParkingType.HOURLY_PAID -> {
                val rate = session.hourlyRate ?: 0.0
                // Rounds up to the next full hour as per typical parking rules
                val hoursRoundedUp = ceil(durationMillis / 3_600_000.0)
                rate * hoursRoundedUp
            }

            ParkingType.FIXED_TICKET -> session.fixedTicketCost ?: 0.0
        }

        // Update the session in DB with end time and cost
        parkingSessionDao.insertSession(
            session.copy(
                endTimeMillis = now,
                isActive = false,
                finalCost = finalCost
            )
        )

        // Mark vehicle as available
        vehicleDao.updateParkedState(session.vehicleId, false)

        // Cleanup: stop reminders if no other sessions are active
        val activeSessions = parkingSessionDao.getActiveSessionsOnce()
        val stillHasActiveSessions = activeSessions.isNotEmpty()
        
        if (!stillHasActiveSessions) {
            NotificationScheduler.cancelParkingReminder(context)
        }

        // Always cancel alarms related to this specific session
        NotificationScheduler.cancelFixedTicketAlarms(context, session.id)

        // Update widget to reflect "Ready" state or other active sessions
        ParkingStatusWidgetUpdater.updateAllWidgets(context)
    }
}
