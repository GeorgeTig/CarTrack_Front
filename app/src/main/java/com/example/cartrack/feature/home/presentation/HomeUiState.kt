package com.example.cartrack.feature.home.presentation

import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto
data class HomeUiState(
    val isLoadingVehicleList: Boolean = true,
    val vehicles: List<VehicleResponseDto> = emptyList(),
    val selectedVehicle: VehicleResponseDto? = null,
    val dropdownVehicles: List<VehicleResponseDto> = emptyList(),
    val vehicleListError: String? = null,
    val isDropdownExpanded: Boolean = false,
    val selectedTab: HomeTab = HomeTab.STATISTICS
)

enum class HomeTab {
    STATISTICS,
    DETAILS
}
