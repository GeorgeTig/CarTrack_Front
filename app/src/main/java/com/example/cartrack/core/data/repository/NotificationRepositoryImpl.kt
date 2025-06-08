package com.example.cartrack.core.data.repository

import com.example.cartrack.core.data.api.NotificationApi
import com.example.cartrack.core.data.api.safeApiCall
import com.example.cartrack.core.data.model.notification.MarkAsReadRequestDto
import com.example.cartrack.core.data.model.notification.NotificationResponseDto
import com.example.cartrack.core.domain.repository.AuthRepository
import com.example.cartrack.core.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Provider

class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi,
    private val authRepositoryProvider: Provider<AuthRepository>
) : NotificationRepository {

    override suspend fun getNotifications(): Result<List<NotificationResponseDto>> =
        safeApiCall(authRepositoryProvider, "Notifications") { notificationApi.getNotifications() }

    override suspend fun markNotificationsAsRead(ids: List<Int>): Result<Unit> {
        if (ids.isEmpty()) return Result.success(Unit)
        val request = MarkAsReadRequestDto(notificationIds = ids)
        return safeApiCall(authRepositoryProvider, "Mark Notifications Read") { notificationApi.markNotificationsAsRead(request); Unit }
    }
}