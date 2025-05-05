package com.example.cartrack.core.vehicle.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleResponseDto(
    val id: Int,
    val vin: String,
    val series: String,
    val year: Int,
)