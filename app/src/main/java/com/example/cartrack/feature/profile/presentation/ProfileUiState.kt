package com.example.cartrack.feature.profile.presentation

import com.example.cartrack.core.vehicle.data.model.VehicleResponseDto
import com.example.cartrack.feature.profile.data.model.UserResponseDto


data class ProfileUiState(
    val isLoading: Boolean = true,
    val userInfo: UserResponseDto? = null,
    val vehicles: List<VehicleResponseDto> = emptyList(),
    val error: String? = null
)