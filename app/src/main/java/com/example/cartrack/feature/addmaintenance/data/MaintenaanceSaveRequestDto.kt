package com.example.cartrack.feature.addmaintenance.data

import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceSaveRequestDto(
    val vehicleId: Int,
    val date: String,
    val mileage: Double,
    val maintenanceItems: List<MaintenanceItemDto>,
    val serviceProvider: String,
    val notes: String,
    val cost: Double
)