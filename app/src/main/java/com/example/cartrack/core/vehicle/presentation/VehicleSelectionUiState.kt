package com.example.cartrack.core.vehicle.presentation

import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto

data class VehicleSelectionUiState(
    val isLoading: Boolean = false,
    val vehicles: List<VehicleResponseDto> = emptyList(),
    val error: String? = null
)
