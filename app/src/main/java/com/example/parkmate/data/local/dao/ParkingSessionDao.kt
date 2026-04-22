package com.example.parkmate.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.parkmate.data.local.entity.ParkingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingSessionDao {

    @Query("SELECT * FROM parking_sessions ORDER BY startTimeMillis DESC")
    fun getAllSessions(): Flow<List<ParkingSessionEntity>>

    @Query("SELECT * FROM parking_sessions WHERE isActive = 1 ORDER BY startTimeMillis DESC")
    fun getActiveSessions(): Flow<List<ParkingSessionEntity>>

    @Query("SELECT * FROM parking_sessions WHERE vehicleId = :vehicleId AND isActive = 1 LIMIT 1")
    suspend fun getActiveSessionForVehicle(vehicleId: Long): ParkingSessionEntity?

    @Query("SELECT * FROM parking_sessions WHERE isActive = 1")
    suspend fun getActiveSessionsOnce(): List<ParkingSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ParkingSessionEntity): Long



    @Query("""
        UPDATE parking_sessions
        SET endTimeMillis = :endTimeMillis,
            finalCost = :finalCost,
            isActive = 0
        WHERE id = :sessionId
    """)
    suspend fun closeSession(
        sessionId: Long,
        endTimeMillis: Long,
        finalCost: Double
    )
}