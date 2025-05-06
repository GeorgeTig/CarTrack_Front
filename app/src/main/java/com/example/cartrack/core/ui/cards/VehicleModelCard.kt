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
fun VehicleModelCard(
    modifier: Modifier = Modifier,
    modelInfo: VehicleModelResponseDto?
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Model Specifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (modelInfo != null) {
                DetailRow("Model Name:", modelInfo.modelName ?: "N/A")
                DetailRow("Series:", modelInfo.series ?: "N/A")
                DetailRow("Year:", modelInfo.year?.toString() ?: "N/A")
                DetailRow("Fuel Tank (gal):", modelInfo.fuelTankCapacity?.toString() ?: "N/A") // Assuming gallons
                DetailRow("Consumption (mpg):", modelInfo.consumption?.toString() ?: "N/A")
            } else {
                Text("Loading model specifications or not available...")
            }
        }
    }
}