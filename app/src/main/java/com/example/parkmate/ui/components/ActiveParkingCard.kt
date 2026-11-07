package com.example.parkmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.parkmate.R
import com.example.parkmate.data.local.entity.ParkingSessionEntity
import com.example.parkmate.data.model.ParkingType
import com.example.parkmate.ui.theme.parkMateColors
import com.example.parkmate.ui.util.calculateSessionCostNow
import com.example.parkmate.ui.util.formatCountdown
import com.example.parkmate.ui.util.formatDurationCompact

/*
Card displayed on the map screen showing the current active parking session.
It provides real-time updates for duration and cost.
*/
@Composable
fun ActiveParkingCard(
    session: ParkingSessionEntity,
    nowMillis: Long, // Updated periodically from the ViewModel to refresh the UI
    onEndSession: () -> Unit,
    onViewDetails: () -> Unit
) {
    val context = LocalContext.current
    
    // Calculate how long the user has been parked
    val elapsedMillis = nowMillis - session.startTimeMillis
    val elapsedText = formatDurationCompact(elapsedMillis)

    // Dynamic info based on parking type (Free, Hourly, or Fixed Ticket)
    val subtitle = when (session.parkingType) {
        ParkingType.FREE -> stringResource(id = R.string.started_ago_format, elapsedText)

        ParkingType.HOURLY_PAID -> {
            val costNow = calculateSessionCostNow(session, nowMillis)
            stringResource(id = R.string.running_for_cost_format, elapsedText, costNow)
        }

        ParkingType.FIXED_TICKET -> {
            // Show countdown to ticket expiration
            val countdown = formatCountdown(context, session.fixedTicketExpiryMillis, nowMillis)
            stringResource(id = R.string.ticket_active_countdown_format, countdown)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.parkMateColors.surfaceContainerLowest.copy(alpha = 0.96f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Parking Icon with background pill
            Icon(
                imageVector = Icons.Default.LocalParking,
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

            Column {
                Text(
                    text = session.locationName ?: stringResource(id = R.string.current_session),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Primary action to stop the current session
            Button(
                onClick = onEndSession,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = RoundedCornerShape(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = stringResource(id = R.string.stop),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Secondary action to see more details (photo, notes, pricing rules)
            Button(
                onClick = onViewDetails,
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp),
                shape = RoundedCornerShape(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = stringResource(id = R.string.details),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
