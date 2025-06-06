package com.example.cartrack.feature.home.presentation.notifications.data.api

import com.example.cartrack.core.di.AuthenticatedHttpClient
import com.example.cartrack.feature.home.presentation.notifications.data.model.MarkAsReadRequest
import com.example.cartrack.feature.home.presentation.notifications.data.model.NotificationResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class NotificationApiImpl @Inject constructor(
    @AuthenticatedHttpClient private val client: HttpClient
) : NotificationApi {

    private val BASE_URL_VEHICLE = "http://10.0.2.2:5098/api/vehicle"
    private val BASE_URL_NOTIFICATION = "http://10.0.2.2:5098/api/notification"

    override suspend fun getNotificationsByClientId(clientId: Int): List<NotificationResponseDto> {
        return client.get("$BASE_URL_VEHICLE/$clientId/notifications") {
            contentType(ContentType.Application.Json)
        }.body<List<NotificationResponseDto>>()
    }

    override suspend fun markNotificationsAsRead(request: MarkAsReadRequest): HttpResponse {
        return client.post("$BASE_URL_NOTIFICATION/mark_as_read") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}