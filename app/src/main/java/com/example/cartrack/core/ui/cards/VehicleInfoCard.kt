package com.example.cartrack.core.ui.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto

// Define a light blue color - adjust as needed
val LightBlueBackground = Color(0xFFE0F7FA) // A light cyan-blue

@Composable
fun VehicleInfoCard(
    modifier: Modifier = Modifier,
    vehicle: VehicleResponseDto?,
    isSelectedDisplay: Boolean = false // Flag to indicate if this is the main selected display
) {
    Card(
        modifier = modifier.width(IntrinsicSize.Min), // Make card width wrap content, min ensures it doesn't get too small
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelectedDisplay) 4.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightBlueBackground // Apply light blue background
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .fillMaxWidth(), // Allow text to center properly
            horizontalAlignment = Alignment.CenterHorizontally // Center children horizontally
        ) {
            if (vehicle != null) {
                Text(
                    // Combine Series and Year
                    text = "${vehicle.series} ${vehicle.year}",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = if (isSelectedDisplay) 18.sp else 16.sp), // Slightly larger for selected
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2 // Allow wrapping for longer series names
                )

                Spacer(modifier = Modifier.height(4.dp))

                val vin = vehicle.vin
                Text(
                    text = vin,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = if (isSelectedDisplay) 12.sp else 11.sp),
                    textAlign = TextAlign.Center
                )
            } else {
                // Placeholder for loading state
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = if (isSelectedDisplay) 18.sp else 16.sp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "VIN: ...",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = if (isSelectedDisplay) 12.sp else 11.sp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}