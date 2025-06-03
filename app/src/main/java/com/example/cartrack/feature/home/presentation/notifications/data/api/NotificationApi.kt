package com.example.cartrack.feature.home.presentation.notifications.data.api

import com.example.cartrack.feature.home.presentation.notifications.data.model.NotificationResponseDto


interface NotificationApi {
    suspend fun getNotificationsByClientId(clientId: Int): List<NotificationResponseDto>
}