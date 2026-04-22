package com.example.parkmate.data.local

import androidx.room.TypeConverter
import com.example.parkmate.data.model.LocationIconType
import com.example.parkmate.data.model.VehicleType

class Converters {

    @TypeConverter
    fun fromVehicleType(value: VehicleType): String = value.name

    @TypeConverter
    fun toVehicleType(value: String): VehicleType = VehicleType.valueOf(value)

    @TypeConverter
    fun fromLocationIconType(value: LocationIconType): String = value.name

    @TypeConverter
    fun toLocationIconType(value: String): LocationIconType =
        LocationIconType.valueOf(value)
}