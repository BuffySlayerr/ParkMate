package com.example.parkmate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.parkmate.ui.theme.parkMateColors

@Composable
fun FilterDropdownChip(
    label: String,
    value: String,
    options: List<String>,
    selected: Boolean,
    activeStyle: FilterDropdownChipStyle = FilterDropdownChipStyle.Standard,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = when {
        value.isBlank() -> label
        label == "Vehicle" -> abbreviateVehicleLabel(value)
        else -> value
    }

    val backgroundColor = when {
        !selected -> MaterialTheme.parkMateColors.surfaceContainerLowest
        activeStyle == FilterDropdownChipStyle.Highlighted -> MaterialTheme.parkMateColors.livePill
        else -> MaterialTheme.parkMateColors.secondaryContainerSoft
    }

    val contentColor = when {
        !selected -> MaterialTheme.colorScheme.onSurface
        activeStyle == FilterDropdownChipStyle.Highlighted -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.primary
    }

    Box {
        Row(
            modifier = Modifier
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = contentColor
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

enum class FilterDropdownChipStyle {
    Standard,
    Highlighted
}

private fun abbreviateVehicleLabel(value: String): String {
    val trimmed = value.trim()
    if (trimmed.length <= 12) return trimmed

    val words = trimmed.split(" ").filter { it.isNotBlank() }
    if (words.size >= 2) {
        val first = words.first()
        val second = words[1]
        val abbreviated = "$first $second"
        return if (abbreviated.length <= 12) abbreviated else "${abbreviated.take(11)}…"
    }

    return "${trimmed.take(11)}…"
}