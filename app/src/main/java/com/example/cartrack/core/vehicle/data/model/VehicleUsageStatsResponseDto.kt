package com.example.cartrack.core.vehicle.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VehicleUsageStatsResponseDto(
    val id: Int,
    val startDate: String?,
    val endDate: String?,
    val distance: Double?
)