package com.example.parkmate

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.example.parkmate.ui.navigation.AppNavGraph
import com.example.parkmate.ui.theme.ParkMateTheme
import com.example.parkmate.ui.viewmodel.ParkingSessionViewModel
import com.example.parkmate.ui.viewmodel.SavedLocationViewModel
import com.example.parkmate.ui.viewmodel.StatsViewModel
import com.example.parkmate.ui.viewmodel.VehicleViewModel

@Composable
fun ParkMateApp(
    vehicleViewModel: VehicleViewModel,
    savedLocationViewModel: SavedLocationViewModel,
    parkingSessionViewModel: ParkingSessionViewModel,
    statsViewModel: StatsViewModel,
    onRequestExactAlarmPermission: () -> Unit,
    initialRoute: String? = null
) {
    ParkMateTheme {
        Surface {
            AppNavGraph(
                vehicleViewModel = vehicleViewModel,
                savedLocationViewModel = savedLocationViewModel,
                parkingSessionViewModel = parkingSessionViewModel,
                statsViewModel = statsViewModel,
                onRequestExactAlarmPermission = onRequestExactAlarmPermission,
                initialRoute = initialRoute
            )
        }
    }
}