package com.example.parkmate.data.repository

import com.example.parkmate.data.local.dao.SavedLocationDao
import com.example.parkmate.data.local.entity.SavedLocationEntity
import kotlinx.coroutines.flow.Flow

class SavedLocationRepository(
    private val dao: SavedLocationDao
) {
    fun getAllSavedLocations(): Flow<List<SavedLocationEntity>> =
        dao.getAllSavedLocations()

    suspend fun getAllSavedLocationsOnce(): List<SavedLocationEntity> =
        dao.getAllSavedLocationsOnce()

    suspend fun getSavedLocationById(locationId: Long): SavedLocationEntity? =
        dao.getSavedLocationById(locationId)

    suspend fun insertSavedLocation(location: SavedLocationEntity): Long =
        dao.insertSavedLocation(location)

    suspend fun deleteSavedLocation(location: SavedLocationEntity) =
        dao.deleteSavedLocation(location)
}