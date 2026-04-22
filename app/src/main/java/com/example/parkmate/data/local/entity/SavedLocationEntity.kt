package com.example.parkmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.parkmate.data.model.LocationIconType

@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val iconType: LocationIconType = LocationIconType.PIN,
    val geofenceEnabled: Boolean = false,
    val geofenceRadiusMeters: Float = 120f
)