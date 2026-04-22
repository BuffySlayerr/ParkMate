package com.example.parkmate.ui.viewmodel

data class StatsUiState(
    val totalSessions: Int = 0,
    val activeSessions: Int = 0,
    val totalSpent: Double = 0.0,
    val monthlySpent: Double = 0.0,
    val mostUsedParkingType: String = "-",
    val mostUsedVehicleName: String = "-",
    val savedLocationsCount: Int = 0,
    val monthlySpending: List<MonthlySpendUi> = emptyList(),
    val heatmapPoints: List<HeatmapPointUi> = emptyList()
)

data class MonthlySpendUi(
    val label: String,
    val amount: Double
)

data class HeatmapPointUi(
    val latitude: Double,
    val longitude: Double,
    val intensity: Float
)