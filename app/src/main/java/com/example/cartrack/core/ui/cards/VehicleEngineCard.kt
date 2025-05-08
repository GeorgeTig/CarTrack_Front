package com.example.cartrack.core.ui.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.vehicle.data.model.VehicleEngineResponseDto
import com.example.cartrack.feature.addvehicle.presentation.components.DetailRow

@Composable
fun VehicleEngineCard(
    modifier: Modifier = Modifier,
    engineInfo: VehicleEngineResponseDto?
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Theme color
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Engine Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // Theme color
                modifier = Modifier.padding(bottom = 12.dp)
            )
            if (engineInfo != null) {
                DetailRow("Type:", engineInfo.engineType)
                DetailRow("Fuel:", engineInfo.fuelType)
                DetailRow("Cylinders:", engineInfo.cylinders)
                DetailRow("Size (L):", engineInfo.size.toString())
                DetailRow("Horsepower:", engineInfo.horsePower.toString())
                DetailRow("Torque (ft-lbs):", engineInfo.torqueFtLbs.toString())
                DetailRow("Drive Type:", engineInfo.driveType)
                DetailRow("Transmission:", engineInfo.transmission)
            } else {
                Text(
                    "Loading engine details...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}