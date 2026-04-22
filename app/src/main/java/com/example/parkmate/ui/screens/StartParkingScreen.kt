package com.example.parkmate.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.parkmate.data.model.ParkingType
import com.example.parkmate.ui.components.LocationChoiceCard
import com.example.parkmate.ui.components.ParkingTypeChoiceCard
import com.example.parkmate.ui.components.PrimaryCtaButton
import com.example.parkmate.ui.components.SectionTitle
import com.example.parkmate.ui.components.VehicleChoiceCard
import com.example.parkmate.ui.viewmodel.ParkingSessionViewModel
import com.example.parkmate.ui.util.ReverseGeocodingUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.parkmate.ui.notifications.ExactAlarmPermissionHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartParkingScreen(
    viewModel: ParkingSessionViewModel,
    onRequestExactAlarmPermission: () -> Unit
) {
    val context = LocalContext.current
    val vehicles by viewModel.vehicles.collectAsState()
    val savedLocations by viewModel.savedLocations.collectAsState()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val canScheduleExactAlarms =
        ExactAlarmPermissionHelper.canScheduleExactAlarms(context)

    var selectedVehicleId by remember { mutableStateOf<Long?>(null) }
    var selectedLocationId by remember { mutableStateOf<Long?>(null) }
    var selectedParkingType by remember { mutableStateOf(ParkingType.FREE) }
    var hourlyRate by remember { mutableStateOf("") }
    var fixedTicketCost by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedMapLocation by remember { mutableStateOf<LatLng?>(null) }
    var selectedMapLabel by remember { mutableStateOf<String?>(null) }

    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var savedPhotoUriString by remember { mutableStateOf<String?>(null) }

    var fixedTicketDurationMinutes by remember { mutableStateOf("") }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            savedPhotoUriString = currentPhotoUri?.toString()
        } else {
            currentPhotoUri = null
            savedPhotoUriString = null
        }
    }

    fun launchCamera() {
        val uri = createImageUri(context)
        currentPhotoUri = uri
        takePictureLauncher.launch(uri)
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

        if (hasLocationPermission) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    selectedMapLocation = latLng
                    selectedLocationId = null
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 16f)

                    selectedMapLabel = "Loading address..."
                    ReverseGeocodingUtils.reverseGeocode(
                        context = context,
                        latitude = latLng.latitude,
                        longitude = latLng.longitude
                    ) { result ->
                        selectedMapLabel = result ?: "Current location"
                    }
                }
            }
        }
    }

    fun useCurrentGpsLocation() {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                selectedMapLocation = latLng
                selectedLocationId = null
                cameraPositionState.position = CameraPosition.fromLatLngZoom(latLng, 16f)

                selectedMapLabel = "Loading address..."
                ReverseGeocodingUtils.reverseGeocode(
                    context = context,
                    latitude = latLng.latitude,
                    longitude = latLng.longitude
                ) { result ->
                    selectedMapLabel = result ?: "Current location"
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            SectionTitle(
                eyebrow = "START SESSION",
                title = "New Parking"
            )
        }

        item {
            Text(
                text = "1. Pick Vehicle",
                style = MaterialTheme.typography.titleLarge
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vehicles) { vehicle ->
                    Box(
                        modifier = Modifier.fillParentMaxWidth(0.48f)
                    ) {
                        VehicleChoiceCard(
                            vehicle = vehicle,
                            selected = selectedVehicleId == vehicle.id,
                            onClick = { selectedVehicleId = vehicle.id }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "2. Parking Type",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ParkingType.entries.forEach { type ->
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        ParkingTypeChoiceCard(
                            parkingType = type,
                            selected = selectedParkingType == type,
                            onClick = { selectedParkingType = type }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "3. Saved Location",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedLocations) { location ->
                    Box(
                        modifier = Modifier.fillParentMaxWidth(0.55f)
                    ) {
                        LocationChoiceCard(
                            location = location,
                            selected = selectedLocationId == location.id,
                            onClick = {
                                selectedLocationId = location.id
                                selectedMapLocation = null
                                selectedMapLabel = null
                            }
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = { useCurrentGpsLocation() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Use current GPS location")
            }
        }

        item {
            Text(
                text = "Or pick from map",
                style = MaterialTheme.typography.titleMedium
            )
        }

        item {
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    mapToolbarEnabled = false,
                    myLocationButtonEnabled = false
                ),
                onMapClick = { latLng ->
                    selectedMapLocation = latLng
                    selectedLocationId = null
                    selectedMapLabel = "Loading address..."
                    ReverseGeocodingUtils.reverseGeocode(
                        context = context,
                        latitude = latLng.latitude,
                        longitude = latLng.longitude
                    ) { result ->
                        selectedMapLabel = result ?: "Selected place"
                    }
                }
            ) {
                selectedMapLocation?.let { selected ->
                    Marker(
                        state = MarkerState(position = selected),
                        title = selectedMapLabel ?: "Selected place"
                    )
                }
            }
        }

        selectedMapLabel?.let { label ->
            item {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        item {
            Text(
                text = "4. Photo",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        item {
            Button(
                onClick = { launchCamera() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (savedPhotoUriString == null) "Take photo" else "Retake photo")
            }
        }

        if (savedPhotoUriString != null) {
            item {
                Image(
                    painter = rememberAsyncImagePainter(savedPhotoUriString),
                    contentDescription = "Parking photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (selectedParkingType == ParkingType.HOURLY_PAID) {
            item {
                OutlinedTextField(
                    value = hourlyRate,
                    onValueChange = { hourlyRate = it },
                    label = { Text("Hourly rate") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (selectedParkingType == ParkingType.FIXED_TICKET) {
            item {
                OutlinedTextField(
                    value = fixedTicketCost,
                    onValueChange = { fixedTicketCost = it },
                    label = { Text("Ticket cost") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                OutlinedTextField(
                    value = fixedTicketDurationMinutes,
                    onValueChange = { fixedTicketDurationMinutes = it },
                    label = { Text("Validity in minutes") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (selectedParkingType == ParkingType.FIXED_TICKET && !canScheduleExactAlarms) {
                item {
                    Text(
                        text = "To receive exact expiry alerts, enable exact alarms in system settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    Button(
                        onClick = onRequestExactAlarmPermission,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enable exact expiry alerts")
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Add Note") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            PrimaryCtaButton(
                text = "Confirm & Start",
                onClick = {
                    val vehicleId = selectedVehicleId ?: return@PrimaryCtaButton

                    val fixedExpiryMillis = if (
                        selectedParkingType == ParkingType.FIXED_TICKET &&
                        fixedTicketDurationMinutes.isNotBlank()
                    ) {
                        val minutes = fixedTicketDurationMinutes.toLongOrNull()
                        if (minutes != null && minutes > 0) {
                            System.currentTimeMillis() + minutes * 60_000L
                        } else {
                            null
                        }
                    } else {
                        null
                    }

                    if (selectedMapLocation != null) {
                        viewModel.startSessionFromCoordinates(
                            vehicleId = vehicleId,
                            parkingType = selectedParkingType,
                            latitude = selectedMapLocation!!.latitude,
                            longitude = selectedMapLocation!!.longitude,
                            locationName = selectedMapLabel,
                            hourlyRate = hourlyRate,
                            fixedTicketCost = fixedTicketCost,
                            fixedTicketExpiryMillis = fixedExpiryMillis,
                            note = note,
                            photoUri = savedPhotoUriString
                        )
                    } else {
                        val locationId = selectedLocationId ?: return@PrimaryCtaButton

                        viewModel.startSessionFromSavedLocation(
                            vehicleId = vehicleId,
                            parkingType = selectedParkingType,
                            savedLocationId = locationId,
                            hourlyRate = hourlyRate,
                            fixedTicketCost = fixedTicketCost,
                            fixedTicketExpiryMillis = fixedExpiryMillis,
                            note = note,
                            photoUri = savedPhotoUriString
                        )
                    }

                    selectedVehicleId = null
                    selectedLocationId = null
                    selectedParkingType = ParkingType.FREE
                    hourlyRate = ""
                    fixedTicketCost = ""
                    fixedTicketDurationMinutes = ""
                    note = ""
                    selectedMapLocation = null
                    selectedMapLabel = null
                    currentPhotoUri = null
                    savedPhotoUriString = null
                }
            )
        }
    }
}

private fun createImageUri(context: Context): Uri {
    val imagesDir = File(context.filesDir, "images").apply { mkdirs() }
    val fileName = "parking_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
    val imageFile = File(imagesDir, fileName)

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        imageFile
    )
}