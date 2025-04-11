package com.example.cartrack.feature.vehicle.presentation

import com.example.cartrack.feature.vehicle.data.model.VehicleResponseDto

data class VehicleSelectionUiState(
    val isLoading: Boolean = false,
    val vehicles: List<VehicleResponseDto> = emptyList(),
    val error: String? = null
)
