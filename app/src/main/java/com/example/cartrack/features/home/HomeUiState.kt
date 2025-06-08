package com.example.cartrack.features.home

import com.example.cartrack.core.data.model.maintenance.ReminderResponseDto
import com.example.cartrack.core.data.model.vehicle.DailyUsageDto
import com.example.cartrack.core.data.model.vehicle.VehicleInfoResponseDto
import com.example.cartrack.core.data.model.vehicle.VehicleResponseDto

// --- DEFINIȚIA NOUĂ ---
data class LocationData(
    val city: String = "Loading...", // Valoare default
    val temperature: String = "--°",  // Valoare default
    val iconUrl: String = ""         // URL-ul pentru iconița vremii
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val vehicles: List<VehicleResponseDto> = emptyList(),
    val selectedVehicle: VehicleResponseDto? = null,
    val selectedVehicleInfo: VehicleInfoResponseDto? = null,
    val error: String? = null,
    val warnings: List<ReminderResponseDto> = emptyList(),
    val isWarningsExpanded: Boolean = false,
    val isLoadingDetails: Boolean = false,
    val dailyUsage: List<DailyUsageDto> = emptyList(),
    val lastSyncTime: String = "never",

    // --- PROPRIETATE NOUĂ ---
    val locationData: LocationData = LocationData(),
    val isSyncMileageDialogVisible: Boolean = false
)
