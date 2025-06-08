package com.example.cartrack.features.home.helpers

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.data.model.vehicle.VehicleResponseDto

@Composable
fun TitleHeader(vehicle: VehicleResponseDto, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${vehicle.producer} ${vehicle.series}",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = vehicle.year.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Divider(
            modifier = Modifier.padding(top = 16.dp).width(80.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}