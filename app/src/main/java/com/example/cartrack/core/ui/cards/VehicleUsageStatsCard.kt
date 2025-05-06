package com.example.cartrack.core.ui.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.vehicle.data.model.VehicleUsageStatsResponseDto
import com.example.cartrack.feature.addvehicle.presentation.components.DetailRow

// Assuming formatDisplayDate is available from VehicleGeneralInfoCard or a common util

@Composable
fun VehicleUsageStatsCard(
    modifier: Modifier = Modifier,
    usageStats: VehicleUsageStatsResponseDto?
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Usage Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (usageStats != null) {
                // If startDate is String?, .toString() on null gives "null", so handle null explicitly
                DetailRow("Period Start:", usageStats.startDate ?: "N/A")
                DetailRow("Period End:", usageStats.endDate ?: "N/A")
                DetailRow("Distance Travelled:", usageStats.distance?.toString() ?: "N/A")
            } else {
                Text("Loading usage statistics or not available...")
            }
        }
    }
}