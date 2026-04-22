package com.example.parkmate.ui.components

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.parkmate.data.model.ParkingType
import com.example.parkmate.ui.theme.OutlineVariantSoft
import com.example.parkmate.ui.theme.SurfaceContainerLowest
import com.example.parkmate.ui.theme.parkMateColors

@Composable
fun ParkingTypeChoiceCard(
    parkingType: ParkingType,
    selected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (parkingType) {
        ParkingType.FREE -> Icons.Default.MoneyOff
        ParkingType.HOURLY_PAID -> Icons.Default.AccessTime
        ParkingType.FIXED_TICKET -> Icons.Default.ConfirmationNumber
    }

    val title = when (parkingType) {
        ParkingType.FREE -> "Free"
        ParkingType.HOURLY_PAID -> "Hourly"
        ParkingType.FIXED_TICKET -> "Ticket"
    }

    val subtitle = when (parkingType) {
        ParkingType.FREE -> "Free zone"
        ParkingType.HOURLY_PAID -> "Pay by hour"
        ParkingType.FIXED_TICKET -> "Fixed pass"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.parkMateColors.surfaceContainerLowest,
                shape = RoundedCornerShape(22.dp)
            )
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    OutlineVariantSoft
                },
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp),
            maxLines = 1
        )

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 2
        )
    }
}