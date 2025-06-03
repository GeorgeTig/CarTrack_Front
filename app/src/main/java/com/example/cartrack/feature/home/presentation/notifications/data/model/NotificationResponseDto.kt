package com.example.cartrack.feature.home.presentation.notifications.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponseDto(
    val id: Int,
    val message: String,
    val date: String, // Vom primi String și îl vom parsa ulterior dacă e nevoie de DateTime
    val isRead: Boolean,
    val userId: Int,
    val vehicleId: Int , // Poate fi null dacă notificarea nu e legată de un vehicul specific
    val reminderId: Int   // Poate fi null dacă notificarea nu e legată de un reminder specific
)