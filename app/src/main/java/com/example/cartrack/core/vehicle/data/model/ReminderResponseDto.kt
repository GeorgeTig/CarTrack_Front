package com.example.cartrack.core.vehicle.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReminderResponseDto(
    val configId: Int,
    val reminderName: String,
    val statusId: Int,
    val statusName: String,
    val maintenanceTypeId: Int,
    val maintenanceTypeName: String,
    val maintenanceCategoryId: Int,
    val maintenanceCategoryName: String,
    val lastMileage: Double?,
    val lastDate: String?,
    val createdDate: String?
)