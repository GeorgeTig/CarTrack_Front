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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Body & Exterior",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            if (bodyInfo != null) {
                DetailRow("Body Type:", bodyInfo.bodyType ?: "N/A")
                DetailRow("Color:", bodyInfo.color ?: "N/A")
                DetailRow("Doors:", bodyInfo.numberOfDoors?.toString() ?: "N/A")
                // Add more fields as per your VehicleBodyResponseDto
            } else {
                Text("Loading body information or not available...")
            }
        }
    }
}