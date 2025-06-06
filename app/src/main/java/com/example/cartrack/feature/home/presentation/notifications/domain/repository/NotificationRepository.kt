package com.example.cartrack.feature.home.presentation.notifications.domain.repository

import com.example.cartrack.feature.home.presentation.notifications.data.model.NotificationResponseDto

interface NotificationRepository {
    suspend fun markNotificationsAsRead(ids: List<Int>): Result<Unit>
    suspend fun getNotificationsByClientId(clientId: Int): Result<List<NotificationResponseDto>>
}