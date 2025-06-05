package com.example.cartrack.feature.home.presentation

import com.example.cartrack.core.vehicle.data.model.VehicleInfoResponseDto
import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto
data class HomeUiState(
    val isLoadingVehicleList: Boolean = true,
    val vehicles: List<VehicleResponseDto> = emptyList(),
    val selectedVehicle: VehicleResponseDto? = null,
    val selectedVehicleInfo: VehicleInfoResponseDto? = null,
    val vehicleListError: String? = null
)

enum class HomeTab {
    STATISTICS,
    DETAILS
}
