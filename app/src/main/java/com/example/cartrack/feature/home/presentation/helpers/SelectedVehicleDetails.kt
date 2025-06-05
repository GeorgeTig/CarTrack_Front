package com.example.cartrack.feature.home.presentation.helpers

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.vehicle.data.model.VehicleInfoResponseDto
import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto

@Composable
fun SelectedVehicleDetails(
    vehicle: VehicleResponseDto,
    vehicleInfo: VehicleInfoResponseDto?, // Pentru kilometraj
    onNavigateToFullDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start // Aliniere la stânga pentru text
    ) {
        // Titlu: Producător + Nume Serie (presupunând că 'series' conține asta)
        // Dacă ai câmpuri separate pentru producător și model, le poți combina.
        Text(
            text = vehicle.series, // Adaptează dacă ai Producător + Model separat
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Listă de informații
        VehicleInfoRow(label = "Car Health", value = vehicleInfo?.let { "Good" } ?: "Loading..." ) // Placeholder
        VehicleInfoRow(label = "Year", value = vehicle.year.toString())
        VehicleInfoRow(label = "VIN", value = vehicle.vin)
        VehicleInfoRow(label = "Mileage", value = vehicleInfo?.mileage?.toInt()?.toString()?.plus(" km") ?: "Loading...")

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateToFullDetails,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("View Full Details")
        }
    }
}

@Composable
fun VehicleInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f) // Eticheta ocupă spațiul disponibil
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End // Valoarea aliniată la dreapta
        )
    }
}
