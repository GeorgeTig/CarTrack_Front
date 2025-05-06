package com.example.cartrack.feature.home.presentation.statistics

data class StatisticsUiState(
    val selectedVehicleId: Int? = null, // Store the ID fetched on init
    val isLoading: Boolean = true, // Start loading on init now
    val statsData: Any? = null,
    val error: String? = null
)