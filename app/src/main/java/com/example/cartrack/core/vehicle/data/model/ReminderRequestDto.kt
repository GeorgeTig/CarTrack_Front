package com.example.cartrack.core.vehicle.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ReminderRequestDto(
    val id: Int,
    val mileageInterval: Int,
    val timeInterval: Int
)