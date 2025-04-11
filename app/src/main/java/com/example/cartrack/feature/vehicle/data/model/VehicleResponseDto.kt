package com.example.cartrack.feature.vehicle.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleResponseDto(
    val id: Int,
    val vin: String,
    val mileage: Int,
    val modelName: String,
    val year: Int,
)