package com.example.cartrack.core.vehicle.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleModelResponseDto(
    val id: Int,
    val modelName: String,
    val series: String,
    val year: Int,
    val fuelTankCapacity: Long,
    val consumption: Long
)