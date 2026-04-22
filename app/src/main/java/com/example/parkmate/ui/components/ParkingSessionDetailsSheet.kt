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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
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
    val now = System.currentTimeMillis()

    val vehicleName = vehicle?.name ?: "Vehicle"
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

    val statusText = if (session.isActive) "ACTIVE" else "COMPLETED"

    val parkingTypeText = when (session.parkingType) {
        ParkingType.FREE -> "Free"
        ParkingType.HOURLY_PAID -> "Hourly"
        ParkingType.FIXED_TICKET -> "Fixed ticket"
    }

    val totalDuration = if (session.isActive) {
        formatDurationCompact(now - session.startTimeMillis)
    } else {
        val end = session.endTimeMillis ?: session.startTimeMillis
        formatDurationCompact(max(0L, end - session.startTimeMillis))
    }

    val pricingText = when (session.parkingType) {
        ParkingType.FREE -> "No cost"
        ParkingType.HOURLY_PAID -> session.hourlyRate?.let { "€ %.2f / h".format(it) } ?: "-"
        ParkingType.FIXED_TICKET -> session.fixedTicketCost?.let { "€ %.2f fixed".format(it) } ?: "-"
    }

    val currentOrFinalCost = if (session.isActive) {
        "€ %.2f".format(calculateSessionCostNow(session, now))
    } else {
        session.finalCost?.let { "€ %.2f".format(it) } ?: "€ 0.00"
    }

    val countdownText = if (session.parkingType == ParkingType.FIXED_TICKET) {
        formatCountdown(session.fixedTicketExpiryMillis, now)
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
                        text = session.locationName ?: "Unknown location",
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
                    style = MaterialTheme.typography.labelMedium
                )
            }

            session.photoUri?.takeIf { it.isNotBlank() }?.let { photoUri ->
                Image(
                    painter = rememberAsyncImagePainter(photoUri),
                    contentDescription = "Parking session photo",
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
                title = "Session details",
                rows = listOfNotNull(
                    "Parking type" to parkingTypeText,
                    "Start" to startText,
                    "End" to (endText ?: if (session.isActive) "Still active" else "-"),
                    if (session.parkingType == ParkingType.FIXED_TICKET) {
                        expiryText?.let { "Expiry" to it }
                    } else null,
                    if (session.parkingType == ParkingType.FIXED_TICKET) {
                        countdownText?.let { "Countdown" to it }
                    } else null,
                    "Duration" to totalDuration,
                    "Pricing" to pricingText,
                    if (session.isActive) {
                        "Current cost" to currentOrFinalCost
                    } else {
                        "Final cost" to currentOrFinalCost
                    }
                )
            )

            session.note?.takeIf { it.isNotBlank() }?.let { note ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Note",
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
                Text("Close")
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