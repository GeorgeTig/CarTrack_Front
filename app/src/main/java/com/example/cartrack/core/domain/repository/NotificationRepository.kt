package com.example.cartrack.core.domain.repository

import com.example.cartrack.core.data.model.notification.NotificationResponseDto

interface NotificationRepository {
    suspend fun getNotifications(): Result<List<NotificationResponseDto>>
    suspend fun markNotificationsAsRead(ids: List<Int>): Result<Unit>
}