package com.example.parkmate

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parkmate.data.local.ParkMateDatabase
import com.example.parkmate.data.repository.ParkingSessionRepository
import com.example.parkmate.data.repository.SavedLocationRepository
import com.example.parkmate.data.repository.VehicleRepository
import com.example.parkmate.ui.activityrecognition.ActivityRecognitionScheduler
import com.example.parkmate.ui.notifications.ExactAlarmPermissionHelper
import com.example.parkmate.ui.notifications.NotificationChannels
import com.example.parkmate.ui.notifications.NotificationNavigation
import com.example.parkmate.ui.viewmodel.ParkingSessionViewModel
import com.example.parkmate.ui.viewmodel.ParkingSessionViewModelFactory
import com.example.parkmate.ui.viewmodel.SavedLocationViewModel
import com.example.parkmate.ui.viewmodel.SavedLocationViewModelFactory
import com.example.parkmate.ui.viewmodel.StatsViewModel
import com.example.parkmate.ui.viewmodel.StatsViewModelFactory
import com.example.parkmate.ui.viewmodel.VehicleViewModel
import com.example.parkmate.ui.viewmodel.VehicleViewModelFactory
import com.example.parkmate.ui.widget.ParkingStatusWidgetUpdater

/*
Main entry point of the application.
Handles system-level configurations, permission requests, and ViewModel initialization.
*/
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enables transparent system bars (status and navigation) to allow the app 
        // to draw behind them
        enableEdgeToEdge()

        // Initialize Database and Repositories
        val database = ParkMateDatabase.getDatabase(this)
        val vehicleRepository = VehicleRepository(database.vehicleDao())
        val savedLocationRepository = SavedLocationRepository(database.savedLocationDao())
        val parkingSessionRepository = ParkingSessionRepository(
            context = this,
            parkingSessionDao = database.parkingSessionDao(),
            vehicleDao = database.vehicleDao()
        )

        // Deep link handling: check if the app was opened from a notification
        val initialRoute = intent?.getStringExtra(NotificationNavigation.EXTRA_OPEN_ROUTE)

        // Ensure notification channels exist for all types of alerts
        NotificationChannels.create(this)

        // Synchronize widgets on startup to reflect any changes that happened 
        // while the app was closed (e.g., via background workers or alarms)
        ParkingStatusWidgetUpdater.updateAllWidgets(this)

        setContent {
            // Launcher for handling dynamic notification permission requests (Android 13+)
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { }

            // Launcher for taking users to system settings for exact alarm permissions
            val exactAlarmSettingsLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { }

            // Automatic check for notification permissions on app launch
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val granted = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!granted) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            // Create Geofence notification channel manually (legacy support)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                val manager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

                val geofenceChannel = android.app.NotificationChannel(
                    com.example.parkmate.ui.notifications.GeofenceNotificationHelper.CHANNEL_GEOFENCE,
                    "Geofence events",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for entering and exiting saved parking areas"
                }

                manager.createNotificationChannel(geofenceChannel)
            }

            // Helper lambda to open system settings for exact alarms
            val openExactAlarmSettings = remember {
                {
                    if (!ExactAlarmPermissionHelper.canScheduleExactAlarms(this@MainActivity)) {
                        exactAlarmSettingsLauncher.launch(
                            ExactAlarmPermissionHelper.createRequestIntent()
                        )
                    }
                }
            }

            val vehicleViewModel: VehicleViewModel = viewModel(
                factory = VehicleViewModelFactory(vehicleRepository)
            )

            val savedLocationViewModel: SavedLocationViewModel = viewModel(
                factory = SavedLocationViewModelFactory(savedLocationRepository)
            )

            val parkingSessionViewModel: ParkingSessionViewModel = viewModel(
                factory = ParkingSessionViewModelFactory(
                    parkingSessionRepository = parkingSessionRepository,
                    vehicleRepository = vehicleRepository,
                    savedLocationRepository = savedLocationRepository
                )
            )

            val statsViewModel: StatsViewModel = viewModel(
                factory = StatsViewModelFactory(
                    parkingSessionRepository = parkingSessionRepository,
                    vehicleRepository = vehicleRepository,
                    savedLocationRepository = savedLocationRepository
                )
            )

            // Start the main Compose UI
            ParkMateApp(
                vehicleViewModel = vehicleViewModel,
                savedLocationViewModel = savedLocationViewModel,
                parkingSessionViewModel = parkingSessionViewModel,
                statsViewModel = statsViewModel,
                onRequestExactAlarmPermission = openExactAlarmSettings,
                initialRoute = initialRoute
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh widgets whenever the user returns to the app
        ParkingStatusWidgetUpdater.updateAllWidgets(this)
    }
}
