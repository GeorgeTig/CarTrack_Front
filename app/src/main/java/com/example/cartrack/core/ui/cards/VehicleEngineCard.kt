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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Engine Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (engineInfo != null) {
                DetailRow("Type:", engineInfo.engineType ?: "N/A")
                DetailRow("Fuel:", engineInfo.fuelType ?: "N/A")
                DetailRow("Cylinders:", engineInfo.cylinders ?: "N/A")
                DetailRow("Size (L):", engineInfo.size?.toString() ?: "N/A")
                DetailRow("Horsepower:", engineInfo.horsePower?.toString() ?: "N/A")
                DetailRow("Torque (ft-lbs):", engineInfo.torqueFtLbs?.toString() ?: "N/A")
                DetailRow("Drive Type:", engineInfo.driveType ?: "N/A")
                DetailRow("Transmission:", engineInfo.transmission ?: "N/A")
            } else {
                Text("Loading engine details or not available...")
            }
        }
    }
}