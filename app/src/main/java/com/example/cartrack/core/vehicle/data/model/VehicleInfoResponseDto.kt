package com.example.cartrack.core.vehicle.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleInfoResponseDto(
    val mileage: Double,
    val travelDistanceAVG: Double,
    val lastUpdate: String
)