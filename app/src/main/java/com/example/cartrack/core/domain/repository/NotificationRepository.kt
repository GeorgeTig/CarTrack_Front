package com.example.cartrack.core.domain.repository

import com.example.cartrack.core.data.model.notification.NotificationResponseDto

interface NotificationRepository {
    // Adăugăm clientId
    suspend fun getNotifications(clientId: Int): Result<List<NotificationResponseDto>>
    suspend fun markNotificationsAsRead(ids: List<Int>): Result<Unit>
}