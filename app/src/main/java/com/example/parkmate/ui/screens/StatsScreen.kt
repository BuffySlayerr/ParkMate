package com.example.parkmate.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.Tile
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.TileOverlayState
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.example.parkmate.ui.theme.parkMateColors
import com.example.parkmate.ui.viewmodel.MonthlySpendUi
import com.example.parkmate.ui.components.MetricCard
import com.example.parkmate.ui.components.SectionTitle
import com.example.parkmate.ui.viewmodel.StatsViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.max

@Composable
fun StatsScreen(
    viewModel: StatsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var isMapInteracting by remember { mutableStateOf(false) }

    val defaultCenter = uiState.heatmapPoints.firstOrNull()?.let {
        LatLng(it.latitude, it.longitude)
    } ?: LatLng(44.4949, 11.3426)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 12f)
    }

    val heatmapProvider = remember(uiState.heatmapPoints) {
        if (uiState.heatmapPoints.isEmpty()) {
            null
        } else {
            val weightedData = uiState.heatmapPoints.map { point ->
                com.google.maps.android.heatmaps.WeightedLatLng(
                    LatLng(point.latitude, point.longitude),
                    point.intensity.toDouble().coerceAtLeast(0.1)
                )
            }

            HeatmapTileProvider.Builder()
                .weightedData(weightedData)
                .radius(50)
                .opacity(0.7)
                .build()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        // to move through the map swiftly
        userScrollEnabled = !isMapInteracting
    ) {
        item {
            SectionTitle(
                title = "Stats",
                subtitle = "Track sessions, spending and parking habits."
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Total Sessions",
                    value = uiState.totalSessions.toString(),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Active",
                    value = uiState.activeSessions.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            MonthlySpendingChartCard(
                data = uiState.monthlySpending,
                totalSpent = uiState.totalSpent
            )
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Most Used Parking Type",
                    value = uiState.mostUsedParkingType,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Most Used Vehicle",
                    value = uiState.mostUsedVehicleName,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            MetricCard(
                title = "Saved Locations",
                value = uiState.savedLocationsCount.toString()
            )
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Parking Heatmap",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Where you park most across the city.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .background(
                        color = MaterialTheme.parkMateColors.surfaceContainerLowest,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(8.dp)
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                if (event.type == PointerEventType.Press) {
                                    isMapInteracting = true
                                } else if (event.type == PointerEventType.Release) {
                                    isMapInteracting = false
                                }
                            }
                        }
                    }
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        mapToolbarEnabled = false,
                        myLocationButtonEnabled = false
                    )
                ) {
                    heatmapProvider?.let { provider ->
                        TileOverlay(
                            tileProvider = provider,
                            state = remember { TileOverlayState() }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Current version uses a soft visual hotspot layer. It can be upgraded to a true Google Maps heatmap tile layer.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MonthlySpendingChartCard(
    data: List<MonthlySpendUi>,
    totalSpent: Double
) {
    val maxValue = max(
        1.0,
        data.maxOfOrNull { it.amount } ?: 1.0
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.parkMateColors.surfaceContainerLowest,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "Monthly Spending",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "€ %.2f total".format(totalSpent),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                val ratio = (item.amount / maxValue).toFloat().coerceIn(0f, 1f)

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = if (item.amount > 0) "€${item.amount.toInt()}" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )

                    val barGradientColors = listOf(
                        MaterialTheme.parkMateColors.gradientEnd,
                        MaterialTheme.colorScheme.primary
                    )

                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val barHeight = size.height * ratio
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = barGradientColors
                                ),
                                topLeft = androidx.compose.ui.geometry.Offset(
                                    x = size.width * 0.18f,
                                    y = size.height - barHeight
                                ),
                                size = androidx.compose.ui.geometry.Size(
                                    width = size.width * 0.64f,
                                    height = barHeight
                                ),
                                cornerRadius = CornerRadius(18f, 18f)
                            )
                        }
                    }

                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
