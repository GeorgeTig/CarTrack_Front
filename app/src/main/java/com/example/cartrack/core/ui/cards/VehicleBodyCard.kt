package com.example.cartrack.core.ui.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.vehicle.data.model.VehicleBodyResponseDto
import com.example.cartrack.feature.addvehicle.presentation.components.DetailRow


@Composable
fun VehicleBodyCard(
    modifier: Modifier = Modifier,
    bodyInfo: VehicleBodyResponseDto?
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Body & Exterior",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            // Show loading/placeholder or details
            if (bodyInfo != null) {
                DetailRow("Body Type:", bodyInfo.bodyType )
                DetailRow("Doors:", bodyInfo.doorNumber.toString())
                DetailRow("Seats:", bodyInfo.seatNumber.toString())
            } else {
                Text(
                    "Loading body information...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}