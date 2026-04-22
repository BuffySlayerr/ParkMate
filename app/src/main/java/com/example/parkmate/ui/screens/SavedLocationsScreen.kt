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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.parkmate.data.local.entity.SavedLocationEntity
import com.example.parkmate.data.model.LocationIconType
import com.example.parkmate.ui.components.LocationIconPicker
import com.example.parkmate.ui.components.SectionTitle
import com.example.parkmate.ui.components.locationIconFor
import com.example.parkmate.ui.theme.SecondaryContainerSoft
import com.example.parkmate.ui.theme.SurfaceContainerLowest
import com.example.parkmate.ui.theme.parkMateColors
import com.example.parkmate.ui.viewmodel.SavedLocationViewModel
import com.example.parkmate.ui.util.ReverseGeocodingUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedLocationsScreen(
    viewModel: SavedLocationViewModel
) {
    val context = LocalContext.current
    val locations by viewModel.savedLocations.collectAsState()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var showSheet by remember { mutableStateOf(false) }
    var editingLocation by remember { mutableStateOf<SavedLocationEntity?>(null) }
    var selectedMapLocation by remember { mutableStateOf<LatLng?>(null) }

    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(LocationIconType.HOME) }

    val bologna = LatLng(44.4949, 11.3426)

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
                    ReverseGeocodingUtils.reverseGeocode(
                        context = context,
                        latitude = latLng.latitude,
                        longitude = latLng.longitude
                    ) { result ->
                        address = result ?: "Current location"
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
                ReverseGeocodingUtils.reverseGeocode(
                    context = context,
                    latitude = latLng.latitude,
                    longitude = latLng.longitude
                ) { result ->
                    address = result ?: "Current location"
                }
            }
        }
    }

    androidx.compose.material3.Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingLocation = null
                    selectedMapLocation = null
                    name = ""
                    address = ""
                    selectedIcon = LocationIconType.HOME
                    showSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add location"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = 110.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SectionTitle(
                    title = "Saved Locations",

                )
            }

            items(locations.size) { index ->
                val location = locations[index]
                SavedLocationMenuCard(
                    location = location,
                    viewModel = viewModel,
                    onEdit = {
                        editingLocation = location
                        name = location.name
                        address = location.address
                        selectedIcon = location.iconType
                        selectedMapLocation = LatLng(location.latitude, location.longitude)
                        showSheet = true
                    },
                    onDelete = {
                        viewModel.deleteSavedLocation(location)
                    }
                )
            }
        }
    }

    if (showSheet) {
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                selectedMapLocation ?: bologna,
                if (selectedMapLocation != null) 16f else 13f
            )
        }

        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            LazyColumn(
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = if (editingLocation == null) "New Location" else "Edit Location",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Location name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Street / address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(
                        text = "Choose icon",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                item {
                    LocationIconPicker(
                        selected = selectedIcon,
                        onSelected = { selectedIcon = it }
                    )
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
                    GoogleMap(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        cameraPositionState = cameraPositionState,
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            mapToolbarEnabled = false,
                            myLocationButtonEnabled = false
                        ),
                        onMapClick = { latLng ->
                            selectedMapLocation = latLng
                            ReverseGeocodingUtils.reverseGeocode(
                                context = context,
                                latitude = latLng.latitude,
                                longitude = latLng.longitude
                            ) { result ->
                                address = result ?: "Selected place"
                            }
                        }
                    ) {
                        selectedMapLocation?.let { selected ->
                            Marker(
                                state = MarkerState(position = selected),
                                title = if (name.isBlank()) "Selected location" else name
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            val loc = selectedMapLocation ?: return@Button
                            if (name.isBlank()) return@Button

                            val editing = editingLocation
                            if (editing == null) {
                                viewModel.addSavedLocation(
                                    name = name,
                                    address = address,
                                    latitude = loc.latitude,
                                    longitude = loc.longitude,
                                    iconType = selectedIcon
                                )
                            } else {
                                viewModel.updateSavedLocation(
                                    editing.copy(
                                        name = name.trim(),
                                        address = address.trim(),
                                        latitude = loc.latitude,
                                        longitude = loc.longitude,
                                        iconType = selectedIcon
                                    )
                                )
                            }

                            name = ""
                            address = ""
                            selectedMapLocation = null
                            editingLocation = null
                            selectedIcon = LocationIconType.HOME
                            showSheet = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (editingLocation == null) "Save Location" else "Update Location")
                    }
                }

            }
        }
    }
}

@Composable
private fun SavedLocationMenuCard(
    location: SavedLocationEntity,
    viewModel: SavedLocationViewModel,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.parkMateColors.surfaceContainerLowest
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = locationIconFor(location.iconType),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.parkMateColors.secondaryContainerSoft,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = location.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = location.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = location.geofenceEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.setGeofenceEnabled(
                            context = context,
                            location = location,
                            enabled = enabled
                        )
                    }
                )

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Location actions",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}
