package com.example.cartrack.feature.home.presentation.notifications.domain.repository

import android.util.Log
import com.example.cartrack.feature.home.presentation.notifications.data.api.NotificationApi
import com.example.cartrack.feature.home.presentation.notifications.data.model.NotificationResponseDto
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRepository {

    private val logTag = "NotificationRepo"

    override suspend fun getNotificationsByClientId(clientId: Int): Result<List<NotificationResponseDto>> {
        return try {
            val notifications = notificationApi.getNotificationsByClientId(clientId)
            Log.d(logTag, "Successfully fetched ${notifications.size} notifications for client $clientId")
            Result.success(notifications)
        } catch (e: ClientRequestException) {
            val errorBody = runCatching { e.response.body<String>() }.getOrNull()
            val errorMsg = "Error fetching notifications for client $clientId: ${e.response.status.description} (${e.response.status.value}). ${errorBody ?: ""}"
            Log.e(logTag, errorMsg, e)
            if (e.response.status.value == 401 || e.response.status.value == 403) {
                Result.failure(Exception("Authentication error fetching notifications."))
            } else {
                Result.failure(Exception("Client error fetching notifications: ${e.response.status.description}"))
            }
        } catch (e: ServerResponseException) {
            val errorMsg = "Error fetching notifications for client $clientId: Server error ${e.response.status.value}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Server error fetching notifications."))
        } catch (e: IOException) {
            val errorMsg = "Error fetching notifications for client $clientId: Network error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Network error fetching notifications. Please check connection."))
        } catch (e: SerializationException) {
            val errorMsg = "Error fetching notifications for client $clientId: Error parsing response: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Error parsing server response for notifications."))
        } catch (e: Exception) {
            val errorMsg = "Error fetching notifications for client $clientId: Unexpected error: ${e.localizedMessage}"
            Log.e(logTag, errorMsg, e)
            Result.failure(Exception("Unexpected error fetching notifications."))
        }
    }
}