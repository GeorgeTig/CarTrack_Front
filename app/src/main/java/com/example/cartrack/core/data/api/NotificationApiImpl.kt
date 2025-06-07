package com.example.cartrack.core.data.api

import com.example.cartrack.core.data.model.notification.MarkAsReadRequestDto
import com.example.cartrack.core.data.model.notification.NotificationResponseDto
import com.example.cartrack.core.di.AuthenticatedHttpClient
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

    private val BASE_URL = "http://10.0.2.2:5098/api/notification"

    override suspend fun getNotifications(): List<NotificationResponseDto> {
        // Backend-ul extrage clientId din token, deci nu mai trebuie trimis
        return client.get("$BASE_URL/all").body()
    }

    override suspend fun markNotificationsAsRead(request: MarkAsReadRequestDto): HttpResponse {
        return client.post("$BASE_URL/mark-as-read") { // Corectat la "mark-as-read"
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}