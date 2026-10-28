package com.example.parkmate.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.parkmate.R

sealed class BottomNavItem(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    data object Map : BottomNavItem(
        route = Routes.ACTIVE_MAP,
        labelRes = R.string.open_map,
        icon = Icons.Default.Map
    )

    data object Vehicles : BottomNavItem(
        route = Routes.VEHICLES,
        labelRes = R.string.vehicle,
        icon = Icons.Default.DirectionsCar
    )

    data object Locations : BottomNavItem(
        route = Routes.SAVED_LOCATIONS,
        labelRes = R.string.saved_locations,
        icon = Icons.Default.Place
    )

    data object History : BottomNavItem(
        route = Routes.PARKING_HISTORY,
        labelRes = R.string.parking_history,
        icon = Icons.Default.History
    )

    data object Stats : BottomNavItem(
        route = Routes.STATS,
        labelRes = R.string.stats_title,
        icon = Icons.Default.BarChart
    )
}