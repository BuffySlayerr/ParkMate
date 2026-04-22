package com.example.parkmate.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.parkmate.data.local.entity.ParkingSessionEntity
import com.example.parkmate.data.local.entity.SavedLocationEntity
import com.example.parkmate.ui.components.ActiveParkingCard
import com.example.parkmate.ui.components.FloatingMapActionButton
import com.example.parkmate.ui.components.ParkingSessionDetailsSheet
import com.example.parkmate.ui.components.PrimaryCtaButton
import com.example.parkmate.ui.components.locationIconFor
import com.example.parkmate.ui.viewmodel.ParkingSessionViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun ActiveParkingMapScreen(
    viewModel: ParkingSessionViewModel,
    onRequestExactAlarmPermission: () -> Unit,
    navController: NavController? = null
) {
    val context = LocalContext.current

    val activeSessions by viewModel.activeSessions.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val savedLocations by viewModel.savedLocations.collectAsState()
    val now by viewModel.now.collectAsState()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val bologna = LatLng(44.4949, 11.3426)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bologna, 13f)
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    fun requestLocationPermission() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    fun centerOnCurrentLocation() {
        if (!hasLocationPermission) {
            requestLocationPermission()
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    LatLng(location.latitude, location.longitude),
                    16f
                )
            }
        }
    }

    val activeSessionDefault = activeSessions.firstOrNull()

    var showSavedLocations by remember { mutableStateOf(false) }
    var selectedSessionForCard by remember { mutableStateOf<ParkingSessionEntity?>(null) }
    var selectedSavedLocation by remember { mutableStateOf<SavedLocationEntity?>(null) }
    var showStartParkingSheet by remember { mutableStateOf(false) }
    var sessionForDetails by remember { mutableStateOf<ParkingSessionEntity?>(null) }

    val displayedSession = selectedSessionForCard ?: activeSessionDefault

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false
            ),
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission
            )
        ) {
            if (!showSavedLocations) {
                activeSessions.forEach { session ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(session.latitude, session.longitude)
                        ),
                        title = session.locationName ?: "Active parking",
                        snippet = null,
                        onClick = {
                            selectedSessionForCard = session
                            true
                        }
                    )
                }
            } else {
                savedLocations.forEach { location ->
                    Marker(
                        state = MarkerState(
                            position = LatLng(location.latitude, location.longitude)
                        ),
                        title = location.name,
                        snippet = location.address,
                        onClick = {
                            selectedSavedLocation = location
                            true
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.scrim.copy(
                        alpha = if (MaterialTheme.colorScheme.background.red < 0.2f) 0.22f else 0.12f
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingMapActionButton(
                icon = Icons.Default.GpsFixed,
                contentDescription = "Center map",
                onClick = { centerOnCurrentLocation() }
            )

            FloatingMapActionButton(
                icon = Icons.Default.Layers,
                contentDescription = "Layers",
                onClick = { showSavedLocations = !showSavedLocations }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!showSavedLocations) {
                displayedSession?.let { session ->
                    ActiveParkingCard(
                        session = session,
                        nowMillis = now,
                        onEndSession = {
                            viewModel.endSession(session)
                            if (selectedSessionForCard?.id == session.id) {
                                selectedSessionForCard = null
                            }
                        },
                        onViewDetails = {
                            sessionForDetails = session
                        }
                    )
                }
            }

            PrimaryCtaButton(
                text = "Start Parking Session",
                onClick = {
                    showStartParkingSheet = true
                }
            )
        }
    }

    selectedSavedLocation?.let { location ->
        AlertDialog(
            onDismissRequest = { selectedSavedLocation = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = locationIconFor(location.iconType),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    Text(location.name)
                }
            },
            text = {
                Text(
                    buildString {
                        appendLine(location.address)
                        appendLine()
                        append("Saved place available for quick parking start.")
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            iconContentColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            confirmButton = {
                TextButton(onClick = { selectedSavedLocation = null }) {
                    Text("OK")
                }
            }
        )
    }

    sessionForDetails?.let { session ->
        ParkingSessionDetailsSheet(
            session = session,
            vehicle = vehicles.firstOrNull { it.id == session.vehicleId },
            onDismiss = { sessionForDetails = null }
        )
    }

    if (showStartParkingSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStartParkingSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            StartParkingScreen(
                viewModel = viewModel,
                onRequestExactAlarmPermission = onRequestExactAlarmPermission
            )
        }
    }
}
