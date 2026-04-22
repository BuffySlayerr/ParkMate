package com.example.parkmate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.parkmate.data.local.entity.ParkingSessionEntity
import com.example.parkmate.data.local.entity.VehicleEntity
import com.example.parkmate.data.model.ParkingType
import com.example.parkmate.data.repository.ParkingSessionRepository
import com.example.parkmate.data.repository.SavedLocationRepository
import com.example.parkmate.data.repository.VehicleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

class StatsViewModel(
    parkingSessionRepository: ParkingSessionRepository,
    vehicleRepository: VehicleRepository,
    savedLocationRepository: SavedLocationRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = combine(
        parkingSessionRepository.getAllSessions(),
        parkingSessionRepository.getActiveSessions(),
        vehicleRepository.getAllVehicles(),
        savedLocationRepository.getAllSavedLocations()
    ) { sessions, activeSessions, vehicles, savedLocations ->
        buildUiState(
            sessions = sessions,
            activeSessionsCount = activeSessions.size,
            vehicles = vehicles,
            savedLocationsCount = savedLocations.size
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatsUiState()
    )

    private fun buildUiState(
        sessions: List<ParkingSessionEntity>,
        activeSessionsCount: Int,
        vehicles: List<VehicleEntity>,
        savedLocationsCount: Int
    ): StatsUiState {
        val completedSessions = sessions.filter { !it.isActive }
        val totalSpent = completedSessions.sumOf { it.finalCost ?: 0.0 }

        val currentMonthSpent = completedSessions
            .filter { isInCurrentMonth(it.endTimeMillis ?: it.startTimeMillis) }
            .sumOf { it.finalCost ?: 0.0 }

        val mostUsedParkingType = sessions
            .groupingBy { it.parkingType }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?.toUiLabel()
            ?: "-"

        val mostUsedVehicleName = sessions
            .groupingBy { it.vehicleId }
            .eachCount()
            .maxByOrNull { it.value }
            ?.key
            ?.let { mostUsedVehicleId ->
                vehicles.firstOrNull { it.id == mostUsedVehicleId }?.name
            }
            ?: "-"

        val monthlySpending = buildLastSixMonthsSpending(completedSessions)

        val heatmapPoints = buildHeatmapPoints(sessions)

        return StatsUiState(
            totalSessions = sessions.size,
            activeSessions = activeSessionsCount,
            totalSpent = totalSpent,
            monthlySpent = currentMonthSpent,
            mostUsedParkingType = mostUsedParkingType,
            mostUsedVehicleName = mostUsedVehicleName,
            savedLocationsCount = savedLocationsCount,
            monthlySpending = monthlySpending,
            heatmapPoints = heatmapPoints
        )
    }

    private fun buildLastSixMonthsSpending(
        completedSessions: List<ParkingSessionEntity>
    ): List<MonthlySpendUi> {
        val calendar = Calendar.getInstance()
        val monthKeys = mutableListOf<Pair<Int, Int>>()

        repeat(6) { index ->
            val temp = calendar.clone() as Calendar
            temp.add(Calendar.MONTH, -(5 - index))
            monthKeys.add(temp.get(Calendar.YEAR) to temp.get(Calendar.MONTH))
        }

        return monthKeys.map { (year, month) ->
            val total = completedSessions
                .filter { session ->
                    val time = session.endTimeMillis ?: session.startTimeMillis
                    isSameMonth(time, year, month)
                }
                .sumOf { it.finalCost ?: 0.0 }

            MonthlySpendUi(
                label = monthShortName(month),
                amount = total
            )
        }
    }

    private fun buildHeatmapPoints(
        sessions: List<ParkingSessionEntity>
    ): List<HeatmapPointUi> {
        if (sessions.isEmpty()) return emptyList()

        val grouped = sessions.groupBy {
            val latBucket = (it.latitude * 1000).toInt()
            val lonBucket = (it.longitude * 1000).toInt()
            latBucket to lonBucket
        }

        val maxCount = grouped.maxOfOrNull { it.value.size } ?: 1

        return grouped.map { (_, bucketSessions) ->
            val avgLat = bucketSessions.map { it.latitude }.average()
            val avgLon = bucketSessions.map { it.longitude }.average()
            val intensity = bucketSessions.size.toFloat() / maxCount.toFloat()

            HeatmapPointUi(
                latitude = avgLat,
                longitude = avgLon,
                intensity = intensity.coerceIn(0f, 1f)
            )
        }
    }

    private fun isInCurrentMonth(timeMillis: Long): Boolean {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timeMillis }

        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.MONTH) == target.get(Calendar.MONTH)
    }

    private fun isSameMonth(
        timeMillis: Long,
        year: Int,
        month: Int
    ): Boolean {
        val calendar = Calendar.getInstance().apply { timeInMillis = timeMillis }
        return calendar.get(Calendar.YEAR) == year &&
                calendar.get(Calendar.MONTH) == month
    }

    private fun monthShortName(month: Int): String {
        return DateFormatSymbols(Locale.getDefault()).shortMonths[month]
            .replace(".", "")
            .take(3)
            .replaceFirstChar { it.uppercase() }
    }

    private fun ParkingType.toUiLabel(): String {
        return when (this) {
            ParkingType.FREE -> "Free"
            ParkingType.HOURLY_PAID -> "Hourly"
            ParkingType.FIXED_TICKET -> "Fixed ticket"
        }
    }
}

class StatsViewModelFactory(
    private val parkingSessionRepository: ParkingSessionRepository,
    private val vehicleRepository: VehicleRepository,
    private val savedLocationRepository: SavedLocationRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatsViewModel::class.java)) {
            return StatsViewModel(
                parkingSessionRepository = parkingSessionRepository,
                vehicleRepository = vehicleRepository,
                savedLocationRepository = savedLocationRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}