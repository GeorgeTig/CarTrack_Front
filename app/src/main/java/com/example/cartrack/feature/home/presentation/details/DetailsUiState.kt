package com.example.cartrack.feature.home.presentation.details

import com.example.cartrack.core.vehicle.data.model.*

data class DetailsUiState(
    val selectedVehicleId: Int? = null, // Store the ID fetched on init
    val visibleDetail: VisibleDetailSection = VisibleDetailSection.NONE,
    val engineDetails: VehicleEngineResponseDto? = null,
    val modelDetails: VehicleModelResponseDto? = null,
    val bodyDetails: VehicleBodyResponseDto? = null,
    val generalInfoDetails: VehicleInfoResponseDto? = null,
    val usageStatsDetails: VehicleUsageStatsResponseDto? = null,
    val isLoadingInitialId: Boolean = true, // Loading state for the initial ID fetch
    val isLoadingDetails: Boolean = false, // Loading state for specific detail fetch
    val error: String? = null
)