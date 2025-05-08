package com.example.cartrack.core.vehicle.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReminderResponseDto(
    // --- IDs ---
    val configId: Int,
    val statusId: Int,
    val typeId: Int,

    // --- Names ---
    val name: String,
    val typeName: String,

    // --- Intervals ---
    val mileageInterval: Int?,
    val timeInterval: Int,

    // --- Due Information ---
    val dueMileage: Double,
    val dueDate: String,

    // --- Other Flags/Info ---
    val isEditable: Boolean,

    // --- Last Check Info ---
    val lastMileageCheck: Double,
    val lastDateCheck: String
)