package com.example.cartrack.core.vehicle.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleResponseDto(
    val id: Int,
    val vin: String,
    val mileage: Double,
    val modelName: String,
    val year: Int,
)