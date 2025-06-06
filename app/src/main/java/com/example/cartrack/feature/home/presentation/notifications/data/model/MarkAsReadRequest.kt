package com.example.cartrack.feature.home.presentation.notifications.data.model

import kotlinx.serialization.Serializable

@Serializable
data class MarkAsReadRequest(
    val notificationIds: List<Int>
)