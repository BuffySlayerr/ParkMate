package com.example.parkmate.data.repository

import com.example.parkmate.data.local.dao.VehicleDao
import com.example.parkmate.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

class VehicleRepository(
    private val vehicleDao: VehicleDao
) {
    fun getAllVehicles(): Flow<List<VehicleEntity>> = vehicleDao.getAllVehicles()

    suspend fun insertVehicle(vehicle: VehicleEntity) {
        vehicleDao.insertVehicle(vehicle)
    }

    suspend fun deleteVehicle(vehicle: VehicleEntity) {
        vehicleDao.deleteVehicle(vehicle)
    }
}