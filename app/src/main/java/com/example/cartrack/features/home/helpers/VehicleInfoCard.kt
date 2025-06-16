package com.example.cartrack.features.home.helpers

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.data.model.vehicle.VehicleInfoResponseDto
import com.example.cartrack.core.data.model.vehicle.VehicleResponseDto

@Composable
fun VehicleInfoCard(
    vehicle: VehicleResponseDto,
    vehicleInfo: VehicleInfoResponseDto?,
    onViewDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Vehicle Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Divider()
            DetailRow(label = "VIN", value = vehicle.vin)
            DetailRow(label = "Mileage", value = vehicleInfo?.mileage?.toInt()?.let { "$it km" } ?: "N/A")
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = onViewDetailsClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("View Detailed Specs")
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}