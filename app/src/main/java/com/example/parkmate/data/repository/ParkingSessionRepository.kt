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

class ParkingSessionRepository(
    private val context: Context,
    private val parkingSessionDao: ParkingSessionDao,
    private val vehicleDao: VehicleDao
) {

    fun getAllSessions(): Flow<List<ParkingSessionEntity>> =
        parkingSessionDao.getAllSessions()

    fun getActiveSessions(): Flow<List<ParkingSessionEntity>> =
        parkingSessionDao.getActiveSessions()

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

        vehicleDao.updateParkedState(vehicleId, true)

        NotificationScheduler.scheduleParkingReminder(context)

        if (parkingType == ParkingType.FIXED_TICKET && fixedTicketExpiryMillis != null) {
            NotificationScheduler.scheduleFixedTicketAlarms(
                context = context,
                sessionId = insertedSessionId,
                locationName = locationName,
                expiryMillis = fixedTicketExpiryMillis
            )
        }

        ParkingStatusWidgetUpdater.updateAllWidgets(context)
    }

    suspend fun endSession(session: ParkingSessionEntity) {
        val now = System.currentTimeMillis()
        val durationMillis = now - session.startTimeMillis

        val finalCost = when (session.parkingType) {
            ParkingType.FREE -> 0.0

            ParkingType.HOURLY_PAID -> {
                val rate = session.hourlyRate ?: 0.0
                val hoursRoundedUp = ceil(durationMillis / 3_600_000.0)
                rate * hoursRoundedUp
            }

            ParkingType.FIXED_TICKET -> session.fixedTicketCost ?: 0.0
        }

        parkingSessionDao.insertSession(
            session.copy(
                endTimeMillis = now,
                isActive = false,
                finalCost = finalCost
            )
        )

        vehicleDao.updateParkedState(session.vehicleId, false)

        val stillHasActiveSessions = parkingSessionDao.getActiveSessionsOnce().isNotEmpty()
        if (!stillHasActiveSessions) {
            NotificationScheduler.cancelParkingReminder(context)
        }

        NotificationScheduler.cancelFixedTicketAlarms(context, session.id)

        ParkingStatusWidgetUpdater.updateAllWidgets(context)
    }
}