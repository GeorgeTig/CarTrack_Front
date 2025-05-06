package com.example.cartrack.feature.home.presentation

import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto

data class HomeUiState(
    val isLoading: Boolean = true,
    val dropdownVehicles: List<VehicleResponseDto> = emptyList(),
    val vehicles : List<VehicleResponseDto> = emptyList(),
    val selectedVehicle: VehicleResponseDto? = null,
    val error: String? = null,
    val isDropdownExpanded: Boolean = false
)