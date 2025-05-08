package com.example.cartrack.core.vehicle.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleBodyResponseDto(
    val id: Int,
    val bodyType: String,
    val doorNumber: Int,
    val seatNumber: Int,
)