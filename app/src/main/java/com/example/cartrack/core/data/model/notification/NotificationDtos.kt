package com.example.cartrack.core.data.model.notification

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponseDto(
    val id: Int,
    val message: String,
    val date: String,
    val isRead: Boolean,
    val userId: Int,
    val vehicleId: Int,
    val reminderId: Int,
    val vehicleName: String? = null,
    val vehicleYear: Int? = null,
    val vehicleImageUrl: String? = null
)

@Serializable
data class MarkAsReadRequestDto(
    val notificationIds: List<Int>
)