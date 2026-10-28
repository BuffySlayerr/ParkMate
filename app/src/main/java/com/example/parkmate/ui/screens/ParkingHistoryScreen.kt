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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.parkmate.R
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

    val vehicleLabel = stringResource(id = R.string.vehicle)
    val typeLabel = stringResource(id = R.string.type)
    val statusLabel = stringResource(id = R.string.status)

    val allVehiclesOption = stringResource(id = R.string.all_vehicles)
    val allTypesOption = stringResource(id = R.string.all_types)
    val allStatusOption = stringResource(id = R.string.all_status)

    val freeOption = stringResource(id = R.string.free)
    val hourlyOption = stringResource(id = R.string.hourly)
    val ticketOption = stringResource(id = R.string.ticket)

    val activeOption = stringResource(id = R.string.active)
    val completedOption = stringResource(id = R.string.completed)

    var selectedVehicleName by remember { mutableStateOf(vehicleLabel) }
    var selectedVehicleId by remember { mutableStateOf<Long?>(null) }

    var selectedTypeFilterLabel by remember { mutableStateOf(typeLabel) }
    var selectedTypeFilter by remember { mutableStateOf<String?>(null) }

    var selectedStatusFilterLabel by remember { mutableStateOf(statusLabel) }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }

    val filteredSessions = sessions.filter { session ->
        val matchesVehicle =
            selectedVehicleId == null || session.vehicleId == selectedVehicleId

        val matchesType = when (selectedTypeFilter) {
            freeOption -> session.parkingType == ParkingType.FREE
            hourlyOption -> session.parkingType == ParkingType.HOURLY_PAID
            ticketOption -> session.parkingType == ParkingType.FIXED_TICKET
            null -> true
            else -> true
        }

        val matchesStatus = when (selectedStatusFilter) {
            activeOption -> session.isActive
            completedOption -> !session.isActive
            null -> true
            else -> true
        }

        matchesVehicle && matchesType && matchesStatus
    }.sortedByDescending { it.startTimeMillis }

    val vehicleOptions = listOf(allVehiclesOption) + vehicles.map { it.name }
    val typeOptions = listOf(allTypesOption, freeOption, hourlyOption, ticketOption)
    val statusOptions = listOf(allStatusOption, activeOption, completedOption)

    LazyColumn(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            SectionTitle(
                title = stringResource(id = R.string.parking_history),
            )
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    FilterChip(
                        text = stringResource(id = R.string.all_filters),
                        selected = selectedVehicleId == null &&
                                selectedTypeFilter == null &&
                                selectedStatusFilter == null,
                        onClick = {
                            selectedVehicleId = null
                            selectedVehicleName = vehicleLabel
                            selectedTypeFilter = null
                            selectedTypeFilterLabel = typeLabel
                            selectedStatusFilter = null
                            selectedStatusFilterLabel = statusLabel
                        }
                    )
                }

                item {
                    FilterDropdownChip(
                        label = vehicleLabel,
                        value = selectedVehicleName,
                        options = vehicleOptions,
                        selected = selectedVehicleId != null,
                        activeStyle = FilterDropdownChipStyle.Standard,
                        onOptionSelected = { selected ->
                            if (selected == allVehiclesOption) {
                                selectedVehicleId = null
                                selectedVehicleName = vehicleLabel
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
                        label = typeLabel,
                        value = selectedTypeFilterLabel,
                        options = typeOptions,
                        selected = selectedTypeFilter != null,
                        activeStyle = FilterDropdownChipStyle.Highlighted,
                        onOptionSelected = { selected ->
                            if (selected == allTypesOption) {
                                selectedTypeFilter = null
                                selectedTypeFilterLabel = typeLabel
                            } else {
                                selectedTypeFilter = selected
                                selectedTypeFilterLabel = selected
                            }
                        }
                    )
                }

                item {
                    FilterDropdownChip(
                        label = statusLabel,
                        value = selectedStatusFilterLabel,
                        options = statusOptions,
                        selected = selectedStatusFilter != null,
                        activeStyle = FilterDropdownChipStyle.Highlighted,
                        onOptionSelected = { selected ->
                            if (selected == allStatusOption) {
                                selectedStatusFilter = null
                                selectedStatusFilterLabel = statusLabel
                            } else {
                                selectedStatusFilter = selected
                                selectedStatusFilterLabel = selected
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
