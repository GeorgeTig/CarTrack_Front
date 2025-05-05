package com.example.cartrack.feature.home.presentation

data class HomeUiState(

    val isLoading: Boolean = false,
    val error: String? = null, // General error messages for Snackbar
    val selectedVehicleId: Int = -1,
)