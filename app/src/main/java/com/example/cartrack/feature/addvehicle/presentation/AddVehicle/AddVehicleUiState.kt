package com.example.cartrack.feature.addvehicle.presentation.AddVehicle

import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto

data class AddVehicleUiState(
    val vinInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null, // General error messages for Snackbar
    val decodeResult: List<VinDecodedResponseDto>? = null, // Holds the list of results from API
    val vinValidationError: String? = null // Specific error for VIN input field
)