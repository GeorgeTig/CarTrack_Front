package com.example.cartrack.core.ui.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.vehicle.data.model.VehicleUsageStatsResponseDto
import com.example.cartrack.feature.addvehicle.presentation.components.DetailRow

@Composable
fun VehicleUsageStatsCard(
    modifier: Modifier = Modifier,
    usageStats: VehicleUsageStatsResponseDto?
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Theme color
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Usage Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            if (usageStats != null) {
                DetailRow("Period Start:", usageStats.startDate?.take(10) ?: "N/A")
                DetailRow("Period End:", usageStats.endDate?.take(10) ?: "N/A")
                DetailRow("Distance Travelled:", usageStats.distance.toString())
            } else {
                Text(
                    "Loading usage statistics...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}