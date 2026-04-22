package com.example.parkmate.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.parkmate.ui.components.AppHeader
import com.example.parkmate.ui.components.BottomBar
import com.example.parkmate.ui.screens.ActiveParkingMapScreen
import com.example.parkmate.ui.screens.ParkingHistoryScreen
import com.example.parkmate.ui.screens.SavedLocationsScreen
import com.example.parkmate.ui.screens.StartParkingScreen
import com.example.parkmate.ui.screens.StatsScreen
import com.example.parkmate.ui.screens.VehiclesScreen
import com.example.parkmate.ui.viewmodel.ParkingSessionViewModel
import com.example.parkmate.ui.viewmodel.SavedLocationViewModel
import com.example.parkmate.ui.viewmodel.StatsViewModel
import com.example.parkmate.ui.viewmodel.VehicleViewModel

object Routes {
    const val ACTIVE_MAP = "active_map"
    const val VEHICLES = "vehicles"
    const val SAVED_LOCATIONS = "saved_locations"
    const val START_PARKING = "start_parking"
    const val PARKING_HISTORY = "parking_history"
    const val STATS = "stats"
}

@Composable
fun AppNavGraph(
    vehicleViewModel: VehicleViewModel,
    savedLocationViewModel: SavedLocationViewModel,
    parkingSessionViewModel: ParkingSessionViewModel,
    statsViewModel: StatsViewModel,
    onRequestExactAlarmPermission: () -> Unit,
    initialRoute: String? = null,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentDestination = currentBackStackEntry?.destination

    val bottomItems = listOf(
        BottomNavItem.Map,
        BottomNavItem.Vehicles,
        BottomNavItem.Locations,
        BottomNavItem.History,
        BottomNavItem.Stats
    )

    LaunchedEffect(initialRoute) {
        if (!initialRoute.isNullOrBlank() && currentDestination?.route != initialRoute) {
            navController.navigate(initialRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { AppHeader() },
        bottomBar = {
            BottomBar(
                items = bottomItems,
                currentDestination = currentDestination,
                onItemClick = { item ->
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.ACTIVE_MAP
            ) {
                composable(Routes.ACTIVE_MAP) {
                    ActiveParkingMapScreen(
                        viewModel = parkingSessionViewModel,
                        onRequestExactAlarmPermission = onRequestExactAlarmPermission,
                        navController = navController
                    )
                }

                composable(Routes.VEHICLES) {
                    VehiclesScreen(viewModel = vehicleViewModel)
                }

                composable(Routes.SAVED_LOCATIONS) {
                    SavedLocationsScreen(viewModel = savedLocationViewModel)
                }

                composable(Routes.START_PARKING) {
                    StartParkingScreen(
                        viewModel = parkingSessionViewModel,
                        onRequestExactAlarmPermission = onRequestExactAlarmPermission
                    )
                }

                composable(Routes.PARKING_HISTORY) {
                    ParkingHistoryScreen(viewModel = parkingSessionViewModel)
                }

                composable(Routes.STATS) {
                    StatsScreen(viewModel = statsViewModel)
                }
            }
        }
    }
}