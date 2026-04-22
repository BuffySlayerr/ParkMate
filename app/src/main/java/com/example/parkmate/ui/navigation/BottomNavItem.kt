package com.example.parkmate.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Map : BottomNavItem(
        route = Routes.ACTIVE_MAP,
        label = "Map",
        icon = Icons.Default.Map
    )

    data object Vehicles : BottomNavItem(
        route = Routes.VEHICLES,
        label = "Vehicles",
        icon = Icons.Default.DirectionsCar
    )

    data object Locations : BottomNavItem(
        route = Routes.SAVED_LOCATIONS,
        label = "Places",
        icon = Icons.Default.Place
    )

    data object History : BottomNavItem(
        route = Routes.PARKING_HISTORY,
        label = "History",
        icon = Icons.Default.History
    )

    data object Stats : BottomNavItem(
        route = Routes.STATS,
        label = "Stats",
        icon = Icons.Default.BarChart
    )
}