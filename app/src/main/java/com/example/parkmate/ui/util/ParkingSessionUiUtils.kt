package com.example.parkmate.ui.util

import com.example.parkmate.data.local.entity.ParkingSessionEntity
import com.example.parkmate.data.model.ParkingType
import kotlin.math.ceil
import kotlin.math.max

fun formatDurationCompact(durationMillis: Long): String {
    val totalMinutes = max(0L, durationMillis / 60_000L).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return if (hours > 0) {
        if (minutes > 0) "${hours}h ${minutes}m" else "${hours}h"
    } else {
        "${totalMinutes}m"
    }
}

fun calculateHourlyRoundedCost(
    startTimeMillis: Long,
    nowMillis: Long,
    hourlyRate: Double?
): Double {
    val rate = hourlyRate ?: 0.0
    if (rate <= 0.0) return 0.0

    val durationMillis = max(0L, nowMillis - startTimeMillis)
    val totalHoursRoundedUp = ceil(durationMillis / 3_600_000.0).toInt().coerceAtLeast(1)
    return totalHoursRoundedUp * rate
}

fun calculateSessionCostNow(
    session: ParkingSessionEntity,
    nowMillis: Long = System.currentTimeMillis()
): Double {
    return when (session.parkingType) {
        ParkingType.FREE -> 0.0
        ParkingType.HOURLY_PAID -> calculateHourlyRoundedCost(
            startTimeMillis = session.startTimeMillis,
            nowMillis = nowMillis,
            hourlyRate = session.hourlyRate
        )
        ParkingType.FIXED_TICKET -> session.fixedTicketCost ?: 0.0
    }
}

fun formatCountdown(expiryMillis: Long?, nowMillis: Long = System.currentTimeMillis()): String {
    if (expiryMillis == null) return "No expiry set"

    val remaining = max(0L, expiryMillis - nowMillis)
    val totalMinutes = (remaining / 60_000L).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        remaining <= 0L -> "Expired"
        hours > 0 -> if (minutes > 0) "${hours}h ${minutes}m left" else "${hours}h left"
        else -> "${minutes}m left"
    }
}