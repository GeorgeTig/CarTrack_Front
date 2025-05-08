package com.example.cartrack.core.ui.cards

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto

@Composable
fun VehicleInfoCard(
    modifier: Modifier = Modifier,
    vehicle: VehicleResponseDto?,
    isSelectedDisplay: Boolean = false
) {
    Card(
        modifier = modifier.width(IntrinsicSize.Min),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelectedDisplay) 4.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (vehicle != null) {
                Text(
                    text = "${vehicle.series} ${vehicle.year}",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = if (isSelectedDisplay) 18.sp else 16.sp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = vehicle.vin,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f), // THEME COLOR
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = if (isSelectedDisplay) 12.sp else 11.sp),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Loading...",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = if (isSelectedDisplay) 18.sp else 16.sp),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "VIN: ...",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f), // THEME COLOR
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = if (isSelectedDisplay) 12.sp else 11.sp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}