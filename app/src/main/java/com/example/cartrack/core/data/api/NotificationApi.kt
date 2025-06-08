package com.example.cartrack.core.data.api

import com.example.cartrack.core.data.model.notification.MarkAsReadRequestDto
import com.example.cartrack.core.data.model.notification.NotificationResponseDto
import io.ktor.client.statement.HttpResponse

interface NotificationApi {
    suspend fun getNotifications(): List<NotificationResponseDto> // Am eliminat clientId
    suspend fun markNotificationsAsRead(request: MarkAsReadRequestDto): HttpResponse
}