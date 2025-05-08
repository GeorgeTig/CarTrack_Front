package com.example.cartrack.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.maintenance.presentation.TypeFilterItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeFilterChip(
    type: TypeFilterItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    FilterChip(
        selected = isSelected,
        enabled = enabled,
        onClick = onClick,
        label = { Text(type.name) },
        leadingIcon = {
            Icon(
                imageVector = type.icon.icon,
                contentDescription = type.name,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
        },
        // --- Colors Configuration ---
        colors = FilterChipDefaults.filterChipColors(
            // Colors when the chip IS selected
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,

            // Colors when the chip is NOT selected
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant,

            // Colors when disabled (applied if enabled=false)
            disabledSelectedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
            disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        ),
        // --- Border Configuration ---
        border = FilterChipDefaults.filterChipBorder(
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = Color.Transparent,
            disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
            disabledSelectedBorderColor = Color.Transparent,
            borderWidth = 1.dp,
            selectedBorderWidth = 0.dp,
            enabled = enabled,
            selected = isSelected
        ),
    )
}