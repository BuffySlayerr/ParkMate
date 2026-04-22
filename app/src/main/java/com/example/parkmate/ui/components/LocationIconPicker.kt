package com.example.parkmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.parkmate.data.model.LocationIconType
import com.example.parkmate.ui.theme.SecondaryContainerSoft
import com.example.parkmate.ui.theme.SurfaceContainerLowest
import com.example.parkmate.ui.theme.parkMateColors

fun locationIconFor(type: LocationIconType): ImageVector {
    return when (type) {
        LocationIconType.HOME -> Icons.Default.Home
        LocationIconType.OFFICE -> Icons.Default.Business
        LocationIconType.GYM -> Icons.Default.FitnessCenter
        LocationIconType.SHOP -> Icons.Default.ShoppingBasket
        LocationIconType.FAVORITE -> Icons.Default.Star
        LocationIconType.PIN -> Icons.Default.Place
    }
}

@Composable
fun LocationIconPicker(
    selected: LocationIconType,
    onSelected: (LocationIconType) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(LocationIconType.entries) { type ->
            Icon(
                imageVector = locationIconFor(type),
                contentDescription = type.name,
                tint = if (selected == type) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .background(
                        color = if (selected == type) {
                            MaterialTheme.parkMateColors.secondaryContainerSoft
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        },
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelected(type) }
                    .padding(12.dp)
            )
        }
    }
}