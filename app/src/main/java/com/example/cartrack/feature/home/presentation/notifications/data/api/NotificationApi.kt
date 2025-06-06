package com.example.cartrack.feature.home.presentation.notifications.data.api

import com.example.cartrack.feature.home.presentation.notifications.data.model.MarkAsReadRequest
import com.example.cartrack.feature.home.presentation.notifications.data.model.NotificationResponseDto
import io.ktor.client.statement.HttpResponse


interface NotificationApi {
    suspend fun markNotificationsAsRead(request: MarkAsReadRequest): HttpResponse
    suspend fun getNotificationsByClientId(clientId: Int): List<NotificationResponseDto>
}