package com.example.parkmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.parkmate.data.local.entity.VehicleEntity
import com.example.parkmate.data.model.VehicleType
import com.example.parkmate.data.repository.VehicleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VehicleViewModel(
    private val repository: VehicleRepository
) : ViewModel() {

    val vehicles: StateFlow<List<VehicleEntity>> =
        repository.getAllVehicles()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun addVehicle(name: String, type: VehicleType) {
        if (name.isBlank()) return

        viewModelScope.launch {
            repository.insertVehicle(
                VehicleEntity(
                    name = name.trim(),
                    type = type
                )
            )
        }
    }

    fun updateVehicle(vehicle: VehicleEntity) {
        if (vehicle.name.isBlank()) return

        viewModelScope.launch {
            repository.insertVehicle(vehicle.copy(name = vehicle.name.trim()))
        }
    }

    fun deleteVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
        }
    }
}

class VehicleViewModelFactory(
    private val repository: VehicleRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VehicleViewModel::class.java)) {
            return VehicleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}