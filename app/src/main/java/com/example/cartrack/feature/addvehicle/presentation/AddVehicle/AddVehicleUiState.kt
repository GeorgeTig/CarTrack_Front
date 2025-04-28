package com.example.cartrack.feature.addvehicle.presentation.AddVehicle

import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto

data class AddVehicleUiState(
    val vinInput: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val decodeResult: List<VinDecodedResponseDto>? = null,
    val vinValidationError: String? = null
)