package com.example.cartrack.features.home.helpers

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.data.model.vehicle.VehicleInfoResponseDto
import com.example.cartrack.core.data.model.vehicle.VehicleResponseDto

@Composable
fun VehicleInfoCard(vehicle: VehicleResponseDto, vehicleInfo: VehicleInfoResponseDto?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Vehicle Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Divider()
            InfoRow("Model Year", vehicle.year.toString())
            InfoRow("VIN", vehicle.vin)
            InfoRow("Mileage", vehicleInfo?.mileage?.toInt()?.toString()?.plus(" km") ?: "Loading...")
        }
    }
}

@Composable
fun VehicleHealthCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Car Health", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Divider()
            InfoRow("Overall Status", "Good")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}