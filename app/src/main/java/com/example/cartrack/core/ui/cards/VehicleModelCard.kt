package com.example.cartrack.core.ui.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.vehicle.data.model.VehicleModelResponseDto
import com.example.cartrack.feature.addvehicle.presentation.components.DetailRow

@Composable
fun VehicleModelCard( // Changed composable name to match file
    modifier: Modifier = Modifier,
    modelInfo: VehicleModelResponseDto?
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant) // Theme color
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Model Specifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // Theme color
                modifier = Modifier.padding(bottom = 12.dp)
            )
            if (modelInfo != null) {
                // Use non-nullable fields from DTO and DetailRow theming
                DetailRow("Model Name:", modelInfo.modelName)
                DetailRow("Series:", modelInfo.series)
                DetailRow("Year:", modelInfo.year.toString())
                DetailRow("Fuel Tank (gal):", modelInfo.fuelTankCapacity.toString())
                DetailRow("Consumption (mpg):", modelInfo.consumption.toString())
            } else {
                Text(
                    "Loading model specifications...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}