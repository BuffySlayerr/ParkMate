package com.example.parkmate.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.parkmate.data.local.entity.SavedLocationEntity
import com.example.parkmate.data.model.LocationIconType
import com.example.parkmate.data.repository.SavedLocationRepository
import com.example.parkmate.ui.geofence.GeofenceHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SavedLocationViewModel(
    private val repository: SavedLocationRepository
) : ViewModel() {

    val savedLocations: StateFlow<List<SavedLocationEntity>> =
        repository.getAllSavedLocations()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun addSavedLocation(
        name: String,
        address: String,
        latitude: Double,
        longitude: Double,
        iconType: LocationIconType
    ) {
        if (name.isBlank()) return

        viewModelScope.launch {
            repository.insertSavedLocation(
                SavedLocationEntity(
                    name = name.trim(),
                    address = address.trim(),
                    latitude = latitude,
                    longitude = longitude,
                    iconType = iconType
                )
            )
        }
    }

    fun updateSavedLocation(location: SavedLocationEntity) {
        if (location.name.isBlank()) return

        viewModelScope.launch {
            repository.insertSavedLocation(location.copy(name = location.name.trim()))
        }
    }

    fun deleteSavedLocation(location: SavedLocationEntity, context: Context? = null) {
        viewModelScope.launch {
            repository.deleteSavedLocation(location)
            context?.let {
                GeofenceHelper.removeGeofence(it, location.id)
            }
        }
    }

    fun setGeofenceEnabled(
        context: Context,
        location: SavedLocationEntity,
        enabled: Boolean,
        radiusMeters: Float = location.geofenceRadiusMeters
    ) {
        viewModelScope.launch {
            val updated = location.copy(
                geofenceEnabled = enabled,
                geofenceRadiusMeters = radiusMeters
            )

            repository.insertSavedLocation(updated)

            if (enabled) {
                GeofenceHelper.addGeofence(context, updated)
            } else {
                GeofenceHelper.removeGeofence(context, updated.id)
            }
        }
    }

    fun refreshGeofences(context: Context) {
        viewModelScope.launch {
            val locations = repository.getAllSavedLocationsOnce()
            GeofenceHelper.refreshAllGeofences(context, locations)
        }
    }
}

class SavedLocationViewModelFactory(
    private val repository: SavedLocationRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavedLocationViewModel::class.java)) {
            return SavedLocationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}