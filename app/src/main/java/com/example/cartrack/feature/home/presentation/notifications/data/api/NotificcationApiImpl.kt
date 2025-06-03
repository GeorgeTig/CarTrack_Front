package com.example.cartrack.feature.home.presentation.notifications.data.api

import com.example.cartrack.core.di.AuthenticatedHttpClient
import com.example.cartrack.feature.home.presentation.notifications.data.model.NotificationResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class NotificationApiImpl @Inject constructor(
    @AuthenticatedHttpClient private val client: HttpClient // ADAUGĂ CALIFICATIVUL AICI
) : NotificationApi {

    private val BASE_URL_VEHICLE = "http://10.0.2.2:5098/api/vehicle" // Probabil ar trebui să fie un URL de bază pentru notificări

    override suspend fun getNotificationsByClientId(clientId: Int): List<NotificationResponseDto> {
        // Ajustează URL-ul dacă este necesar pentru notificări
        return client.get("$BASE_URL_VEHICLE/$clientId/notifications") {
            contentType(ContentType.Application.Json)
        }.body<List<NotificationResponseDto>>()
    }
}