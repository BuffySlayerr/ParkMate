package com.example.parkmate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.parkmate.data.model.VehicleType

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: VehicleType,
    val isParked: Boolean = false
)