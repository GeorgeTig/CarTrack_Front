package com.example.cartrack.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cartrack.feature.maintenance.presentation.CategoryFilterItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilterChip(
    category: CategoryFilterItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true // Add enabled parameter, defaulting to true
) {
    FilterChip(
        // Pass the mandatory parameters
        selected = isSelected,
        enabled = enabled, // Pass the enabled state
        onClick = onClick,
        label = { Text(category.name) },
        leadingIcon = {
            Icon(
                imageVector = category.icon.icon,
                contentDescription = category.name,
                modifier = Modifier.size(FilterChipDefaults.IconSize)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
            // You might want different colors for disabled state if 'enabled' can be false
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline,
            selectedBorderColor = Color.Transparent,
            borderWidth = 1.dp,
            selectedBorderWidth = 0.dp,
            enabled = true,
            selected = true
        )
    )
}
