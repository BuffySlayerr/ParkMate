package com.example.parkmate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.parkmate.R
import com.example.parkmate.data.local.entity.ParkingSessionEntity
import com.example.parkmate.data.local.entity.VehicleEntity
import com.example.parkmate.data.model.ParkingType
import com.example.parkmate.data.model.VehicleType
import com.example.parkmate.ui.theme.parkMateColors
import com.example.parkmate.ui.util.calculateSessionCostNow
import com.example.parkmate.ui.util.formatCountdown
import com.example.parkmate.ui.util.formatDurationCompact
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingSessionDetailsSheet(
    session: ParkingSessionEntity,
    vehicle: VehicleEntity?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()

    val vehicleName = vehicle?.name ?: stringResource(id = R.string.vehicle)
    val vehicleIcon = when (vehicle?.type) {
        VehicleType.CAR -> Icons.Default.DirectionsCar
        VehicleType.MOTORCYCLE -> Icons.Default.TwoWheeler
        VehicleType.BICYCLE -> Icons.Default.DirectionsBike
        null -> Icons.Default.LocalParking
    }

    val dateTimeFormatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val startText = dateTimeFormatter.format(Date(session.startTimeMillis))
    val endText = session.endTimeMillis?.let { dateTimeFormatter.format(Date(it)) }
    val expiryText = session.fixedTicketExpiryMillis?.let { dateTimeFormatter.format(Date(it)) }

    val statusText = if (session.isActive) stringResource(id = R.string.active_banner) else stringResource(id = R.string.completed)

    val parkingTypeText = when (session.parkingType) {
        ParkingType.FREE -> stringResource(id = R.string.free)
        ParkingType.HOURLY_PAID -> stringResource(id = R.string.hourly)
        ParkingType.FIXED_TICKET -> stringResource(id = R.string.ticket)
    }

    val totalDuration = if (session.isActive) {
        formatDurationCompact(now - session.startTimeMillis)
    } else {
        val end = session.endTimeMillis ?: session.startTimeMillis
        formatDurationCompact(max(0L, end - session.startTimeMillis))
    }

    val pricingText = when (session.parkingType) {
        ParkingType.FREE -> stringResource(id = R.string.no_cost)
        ParkingType.HOURLY_PAID -> session.hourlyRate?.let { stringResource(id = R.string.hourly_rate_format, it) } ?: "-"
        ParkingType.FIXED_TICKET -> session.fixedTicketCost?.let { stringResource(id = R.string.fixed_cost_format, it) } ?: "-"
    }

    val currentOrFinalCost = if (session.isActive) {
        stringResource(id = R.string.cost_format, calculateSessionCostNow(session, now))
    } else {
        session.finalCost?.let { stringResource(id = R.string.cost_format, it) } ?: stringResource(id = R.string.cost_format, 0.0)
    }

    val countdownText = if (session.parkingType == ParkingType.FIXED_TICKET) {
        formatCountdown(context, session.fixedTicketExpiryMillis, now)
    } else {
        null
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = vehicleIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = MaterialTheme.parkMateColors.secondaryContainerSoft,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp)
                )

                Column(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = vehicleName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = session.locationName ?: stringResource(id = R.string.unknown_location),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = statusText,
                    modifier = Modifier
                        .background(
                            color = if (session.isActive) MaterialTheme.parkMateColors.livePill else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (session.isActive) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface
                )
            }

            session.photoUri?.takeIf { it.isNotBlank() }?.let { photoUri ->
                Image(
                    painter = rememberAsyncImagePainter(photoUri),
                    contentDescription = stringResource(id = R.string.photo),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentScale = ContentScale.Crop
                )
            }

            DetailsSection(
                title = stringResource(id = R.string.session_details),
                rows = listOfNotNull(
                    stringResource(id = R.string.parking_type) to parkingTypeText,
                    stringResource(id = R.string.start) to startText,
                    stringResource(id = R.string.end) to (endText ?: if (session.isActive) stringResource(id = R.string.still_active) else "-"),
                    if (session.parkingType == ParkingType.FIXED_TICKET) {
                        expiryText?.let { stringResource(id = R.string.expiry) to it }
                    } else null,
                    if (session.parkingType == ParkingType.FIXED_TICKET) {
                        countdownText?.let { stringResource(id = R.string.countdown) to it }
                    } else null,
                    stringResource(id = R.string.duration) to totalDuration,
                    stringResource(id = R.string.pricing) to pricingText,
                    if (session.isActive) {
                        stringResource(id = R.string.current_cost) to currentOrFinalCost
                    } else {
                        stringResource(id = R.string.final_cost) to currentOrFinalCost
                    }
                )
            )

            session.note?.takeIf { it.isNotBlank() }?.let { note ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.note),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(id = R.string.close))
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DetailsSection(
    title: String,
    rows: List<Pair<String, String>>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rows.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = row.first,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = row.second,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (index != rows.lastIndex) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}
