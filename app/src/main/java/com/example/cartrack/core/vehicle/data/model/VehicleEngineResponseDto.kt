package com.example.cartrack.core.vehicle.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleEngineResponseDto(
    val id: Int,
    val engineType: String?,
    val fuelType: String?,
    val cylinders: String?,
    val size: Double?,
    val horsePower: Int?,
    val torqueFtLbs: Int?,
    val driveType: String?,
    val transmission: String?
)
