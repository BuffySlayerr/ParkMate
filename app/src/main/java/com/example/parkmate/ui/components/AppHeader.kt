package com.example.parkmate.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.parkmate.R
import com.example.parkmate.ui.theme.GradientEnd
import com.example.parkmate.ui.theme.GradientStart
import com.example.parkmate.ui.theme.SurfaceContainerLowest
import com.example.parkmate.ui.theme.parkMateColors

@Composable
fun AppHeader(
    title: String = "PARKMATE",
    onNotificationsClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.parkMateColors.surfaceContainerLowest)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.park_icon_nobg),
                contentDescription = "App Logo",
                modifier = Modifier.size(32.dp)
            )

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.parkMateColors.gradientStart,
                            MaterialTheme.parkMateColors.gradientEnd
                        )
                    )
                ),
                modifier = Modifier.padding(start = 10.dp)
            )
        }

        IconButton(
            onClick = onNotificationsClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}