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

    // Adăugăm ambele URL-uri de bază
    private val BASE_URL = "http://10.0.2.2:5098/api/notification"

    // --- AICI ESTE CORECȚIA CRUCIALĂ ---
    // Apelul GET se face către controller-ul de vehicul
    override suspend fun getNotifications(): List<NotificationResponseDto> {
        return client.get("$BASE_URL/all").body()
    }

    // Apelul POST se face către controller-ul de notificare
    override suspend fun markNotificationsAsRead(request: MarkAsReadRequestDto): HttpResponse {
        return client.post("$BASE_URL/mark-as-read") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}