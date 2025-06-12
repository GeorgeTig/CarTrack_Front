package com.example.cartrack.core.data.model.maintenance

import kotlinx.serialization.Serializable

// --- DTOs for Reminders ---

@Serializable
data class ReminderResponseDto(
    val configId: Int,
    val statusId: Int,
    val typeId: Int,
    val name: String,
    val typeName: String,
    val mileageInterval: Int,
    val timeInterval: Int,
    val dueMileage: Double,
    val dueDate: Int,
    val isEditable: Boolean,
    val isActive: Boolean,
    val isCustom: Boolean,
    val lastMileageCheck: Double,
    val lastDateCheck: String
)

@Serializable
data class ReminderUpdateRequestDto(
    val id: Int,
    val mileageInterval: Int,
    val timeInterval: Int
)

@Serializable
data class CustomReminderRequestDto(
    val name: String,
    val maintenanceTypeId: Int,
    val mileageInterval: Int,
    val dateInterval: Int
)

@Serializable
data class ReminderTypeResponseDto(
    val id: Int,
    val name: String
)

@Serializable
data class MaintenanceItemDto(
    val configId: Int?,
    val customName: String?
)

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