package com.example.parkmate.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.parkmate.data.local.entity.SavedLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedLocationDao {

    @Query("SELECT * FROM saved_locations ORDER BY name ASC")
    fun getAllSavedLocations(): Flow<List<SavedLocationEntity>>

    @Query("SELECT * FROM saved_locations ORDER BY name ASC")
    suspend fun getAllSavedLocationsOnce(): List<SavedLocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedLocation(location: SavedLocationEntity): Long

    @Delete
    suspend fun deleteSavedLocation(location: SavedLocationEntity)

    @Query("SELECT * FROM saved_locations WHERE id = :locationId LIMIT 1")
    suspend fun getSavedLocationById(locationId: Long): SavedLocationEntity?
}