package com.example.parkmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.parkmate.data.local.entity.ParkingSessionEntity
import com.example.parkmate.data.local.entity.SavedLocationEntity
import com.example.parkmate.data.local.entity.VehicleEntity
import com.example.parkmate.data.model.ManualMapSelection
import com.example.parkmate.data.model.ParkingType
import com.example.parkmate.data.repository.ParkingSessionRepository
import com.example.parkmate.data.repository.SavedLocationRepository
import com.example.parkmate.data.repository.VehicleRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ParkingSessionViewModel(
    private val parkingSessionRepository: ParkingSessionRepository,
    vehicleRepository: VehicleRepository,
    savedLocationRepository: SavedLocationRepository
) : ViewModel() {

    val vehicles: StateFlow<List<VehicleEntity>> =
        vehicleRepository.getAllVehicles()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val savedLocations: StateFlow<List<SavedLocationEntity>> =
        savedLocationRepository.getAllSavedLocations()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val sessions: StateFlow<List<ParkingSessionEntity>> =
        parkingSessionRepository.getAllSessions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val activeSessions: StateFlow<List<ParkingSessionEntity>> =
        parkingSessionRepository.getActiveSessions()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _manualSelection = MutableStateFlow<ManualMapSelection?>(null)
    val manualSelection = _manualSelection.asStateFlow()

    private val _now = MutableStateFlow(System.currentTimeMillis())
    val now: StateFlow<Long> = _now.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                _now.value = System.currentTimeMillis()
                delay(1_000)
            }
        }
    }

    fun setManualSelection(latitude: Double, longitude: Double) {
        _manualSelection.value = ManualMapSelection(
            latitude = latitude,
            longitude = longitude,
            label = "Posizione manuale"
        )
    }

    fun clearManualSelection() {
        _manualSelection.value = null
    }

    fun startSessionFromCoordinates(
        vehicleId: Long,
        parkingType: ParkingType,
        latitude: Double,
        longitude: Double,
        locationName: String?,
        hourlyRate: String,
        fixedTicketCost: String,
        fixedTicketExpiryMillis: Long?,
        note: String,
        photoUri: String?
    ) {
        val parsedHourlyRate = hourlyRate.toDoubleOrNull()
        val parsedFixedCost = fixedTicketCost.toDoubleOrNull()

        viewModelScope.launch {
            parkingSessionRepository.startSession(
                vehicleId = vehicleId,
                parkingType = parkingType,
                latitude = latitude,
                longitude = longitude,
                locationName = locationName ?: "Selected place",
                hourlyRate = parsedHourlyRate,
                fixedTicketCost = parsedFixedCost,
                fixedTicketExpiryMillis = fixedTicketExpiryMillis,
                note = note.ifBlank { null },
                photoUri = photoUri
            )
        }
    }

    fun startSessionFromSavedLocation(
        vehicleId: Long,
        parkingType: ParkingType,
        savedLocationId: Long,
        hourlyRate: String,
        fixedTicketCost: String,
        fixedTicketExpiryMillis: Long?,
        note: String,
        photoUri: String?
    ) {
        val location = savedLocations.value.firstOrNull { it.id == savedLocationId } ?: return
        val parsedHourlyRate = hourlyRate.toDoubleOrNull()
        val parsedFixedCost = fixedTicketCost.toDoubleOrNull()

        viewModelScope.launch {
            parkingSessionRepository.startSession(
                vehicleId = vehicleId,
                parkingType = parkingType,
                latitude = location.latitude,
                longitude = location.longitude,
                locationName = location.name,
                hourlyRate = parsedHourlyRate,
                fixedTicketCost = parsedFixedCost,
                fixedTicketExpiryMillis = fixedTicketExpiryMillis,
                note = note.ifBlank { null },
                photoUri = photoUri
            )
        }
    }

    fun endSession(session: ParkingSessionEntity) {
        viewModelScope.launch {
            parkingSessionRepository.endSession(session)
        }
    }
}

class ParkingSessionViewModelFactory(
    private val parkingSessionRepository: ParkingSessionRepository,
    private val vehicleRepository: VehicleRepository,
    private val savedLocationRepository: SavedLocationRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ParkingSessionViewModel::class.java)) {
            return ParkingSessionViewModel(
                parkingSessionRepository = parkingSessionRepository,
                vehicleRepository = vehicleRepository,
                savedLocationRepository = savedLocationRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}