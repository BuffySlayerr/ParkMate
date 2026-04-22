package com.example.parkmate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.parkmate.data.model.ParkingType

@Entity(
    tableName = "parking_sessions",
    foreignKeys = [
        ForeignKey(
            entity = VehicleEntity::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("vehicleId")]
)
data class ParkingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val vehicleId: Long,
    val parkingType: ParkingType,
    val latitude: Double,
    val longitude: Double,
    val locationName: String? = null,
    val startTimeMillis: Long,
    val endTimeMillis: Long? = null,
    val isActive: Boolean = true,
    val hourlyRate: Double? = null,
    val fixedTicketCost: Double? = null,
    val fixedTicketExpiryMillis: Long? = null,
    val finalCost: Double? = null,
    val note: String? = null,
    val photoUri: String? = null
)