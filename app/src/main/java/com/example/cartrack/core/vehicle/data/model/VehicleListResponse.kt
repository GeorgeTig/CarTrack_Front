package com.example.cartrack.core.vehicle.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleListResponse(
    val result: List<VehicleResponseDto>

)