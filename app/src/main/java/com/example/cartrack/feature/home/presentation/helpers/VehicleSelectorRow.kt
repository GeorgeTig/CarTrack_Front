package com.example.cartrack.feature.home.presentation.helpers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto

@Composable
fun VehicleSelectorRow(
    vehicles: List<VehicleResponseDto>,
    selectedVehicleId: Int?,
    onVehicleSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val orderedVehicles = remember(vehicles, selectedVehicleId) {
        val selected = vehicles.find { it.id == selectedVehicleId }
        if (selected != null) {
            listOf(selected) + vehicles.filterNot { it.id == selectedVehicleId }
        } else {
            vehicles
        }
    }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Padding vertical pentru rândul de stories
        contentPadding = PaddingValues(horizontal = 16.dp), // Padding la capetele listei
        horizontalArrangement = Arrangement.spacedBy(10.dp) // Spațiu între item-urile story
    ) {
        items(
            items = orderedVehicles,
            key = { vehicle -> vehicle.id }
        ) { vehicle ->
            VehicleStoryItem(
                vehicle = vehicle,
                isSelected = vehicle.id == selectedVehicleId,
                onClick = { onVehicleSelect(vehicle.id) }
            )
        }
    }
}