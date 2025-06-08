package com.example.cartrack.core.data.model.history

import kotlinx.serialization.Serializable

@Serializable
data class MaintenanceLogResponseDto(
    val id: Int,
    val date: String, // Ktor va primi data ca un string ISO 8601
    val mileage: Double,
    val cost: Double,
    val serviceProvider: String,
    val notes: String,
    val performedTasks: List<String>
)