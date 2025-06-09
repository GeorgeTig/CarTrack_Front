package com.example.cartrack.features.profile

import com.example.cartrack.core.data.model.user.UserResponseDto
import com.example.cartrack.core.data.model.vehicle.VehicleResponseDto

data class ProfileUiState(
    val isLoading: Boolean = true,
    val userInfo: UserResponseDto? = null,
    val vehicles: List<VehicleResponseDto> = emptyList(),
    val error: String? = null
)