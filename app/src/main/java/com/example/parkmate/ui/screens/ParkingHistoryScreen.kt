package com.example.parkmate.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.example.parkmate.data.model.ParkingType
import com.example.parkmate.ui.components.FilterChip
import com.example.parkmate.ui.components.FilterDropdownChip
import com.example.parkmate.ui.components.FilterDropdownChipStyle
import com.example.parkmate.ui.components.SectionTitle
import com.example.parkmate.ui.components.SessionHistoryCard
import com.example.parkmate.ui.viewmodel.ParkingSessionViewModel

@Composable
fun ParkingHistoryScreen(
    viewModel: ParkingSessionViewModel
) {
    val sessions by viewModel.sessions.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()

    var selectedVehicleName by remember { mutableStateOf("Vehicle") }
    var selectedVehicleId by remember { mutableStateOf<Long?>(null) }

    var selectedTypeLabel by remember { mutableStateOf("Type") }
    var selectedTypeFilter by remember { mutableStateOf<String?>(null) }

    var selectedStatusLabel by remember { mutableStateOf("Status") }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }

    val filteredSessions = sessions.filter { session ->
        val matchesVehicle =
            selectedVehicleId == null || session.vehicleId == selectedVehicleId

        val matchesType = when (selectedTypeFilter) {
            "Free" -> session.parkingType == ParkingType.FREE
            "Hourly" -> session.parkingType == ParkingType.HOURLY_PAID
            "Ticket" -> session.parkingType == ParkingType.FIXED_TICKET
            null -> true
            else -> true
        }

        val matchesStatus = when (selectedStatusFilter) {
            "Active" -> session.isActive
            "Completed" -> !session.isActive
            null -> true
            else -> true
        }

        matchesVehicle && matchesType && matchesStatus
    }.sortedByDescending { it.startTimeMillis }

    val vehicleOptions = listOf("All Vehicles") + vehicles.map { it.name }
    val typeOptions = listOf("All Types", "Free", "Hourly", "Ticket")
    val statusOptions = listOf("All Status", "Active", "Completed")

    LazyColumn(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            SectionTitle(
                title = "Parking History",
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    FilterChip(
                        text = "All Filters",
                        selected = selectedVehicleId == null &&
                                selectedTypeFilter == null &&
                                selectedStatusFilter == null,
                        onClick = {
                            selectedVehicleId = null
                            selectedVehicleName = "Vehicle"
                            selectedTypeFilter = null
                            selectedTypeLabel = "Type"
                            selectedStatusFilter = null
                            selectedStatusLabel = "Status"
                        }
                    )
                }

                item {
                    FilterDropdownChip(
                        label = "Vehicle",
                        value = selectedVehicleName,
                        options = vehicleOptions,
                        selected = selectedVehicleId != null,
                        activeStyle = FilterDropdownChipStyle.Standard,
                        onOptionSelected = { selected ->
                            if (selected == "All Vehicles") {
                                selectedVehicleId = null
                                selectedVehicleName = "Vehicle"
                            } else {
                                val vehicle = vehicles.firstOrNull { it.name == selected }
                                selectedVehicleId = vehicle?.id
                                selectedVehicleName = selected
                            }
                        }
                    )
                }

                item {
                    FilterDropdownChip(
                        label = "Type",
                        value = selectedTypeLabel,
                        options = typeOptions,
                        selected = selectedTypeFilter != null,
                        activeStyle = FilterDropdownChipStyle.Highlighted,
                        onOptionSelected = { selected ->
                            if (selected == "All Types") {
                                selectedTypeFilter = null
                                selectedTypeLabel = "Type"
                            } else {
                                selectedTypeFilter = selected
                                selectedTypeLabel = selected
                            }
                        }
                    )
                }

                item {
                    FilterDropdownChip(
                        label = "Status",
                        value = selectedStatusLabel,
                        options = statusOptions,
                        selected = selectedStatusFilter != null,
                        activeStyle = FilterDropdownChipStyle.Highlighted,
                        onOptionSelected = { selected ->
                            if (selected == "All Status") {
                                selectedStatusFilter = null
                                selectedStatusLabel = "Status"
                            } else {
                                selectedStatusFilter = selected
                                selectedStatusLabel = selected
                            }
                        }
                    )
                }
            }
        }

        items(filteredSessions) { session ->
            val vehicle = vehicles.firstOrNull { it.id == session.vehicleId }

            SessionHistoryCard(
                session = session,
                vehicle = vehicle
            )
        }
    }
}