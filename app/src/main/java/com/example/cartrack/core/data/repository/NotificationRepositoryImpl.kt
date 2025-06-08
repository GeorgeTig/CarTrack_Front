package com.example.cartrack.core.data.repository

import android.util.Log
import com.example.cartrack.core.data.api.NotificationApi
// --- IMPORT CORECTAT ---
import com.example.cartrack.core.data.model.notification.MarkAsReadRequestDto
import com.example.cartrack.core.data.model.notification.NotificationResponseDto
import com.example.cartrack.core.domain.repository.NotificationRepository
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRepository {

    private val logTag = "NotificationRepo"

    override suspend fun getNotifications(): Result<List<NotificationResponseDto>> {
        return try {
            val notifications = notificationApi.getNotifications()
            Log.d(logTag, "Successfully fetched ${notifications.size} notifications.")
            Result.success(notifications)
        } catch (e: Exception) {
            Log.e(logTag, "Error fetching notifications: ${e.message}", e)
            Result.failure(Exception("Could not load notifications.", e))
        }
    }

    override suspend fun markNotificationsAsRead(ids: List<Int>): Result<Unit> {
        if (ids.isEmpty()) return Result.success(Unit)

        return try {
            // Folosim DTO-ul corect
            val request = MarkAsReadRequestDto(notificationIds = ids)
            val response = notificationApi.markNotificationsAsRead(request)

            if (response.status.isSuccess()) {
                Log.d(logTag, "Successfully marked ${ids.size} notifications as read.")
                Result.success(Unit)
            } else {
                val errorBody = response.bodyAsText()
                Log.e(logTag, "Failed to mark notifications as read: ${response.status}. Body: $errorBody")
                Result.failure(Exception("Failed to update notifications: ${response.status.description}"))
            }
        } catch (e: Exception) {
            Log.e(logTag, "Exception while marking notifications as read: ${e.message}", e)
            Result.failure(e)
        }
    }
}