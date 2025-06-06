package com.example.cartrack.feature.profile.domain.repository

import android.util.Log
import com.example.cartrack.feature.profile.data.api.UserApi
import com.example.cartrack.feature.profile.data.model.UpdateProfileRequestDto
import com.example.cartrack.feature.profile.data.model.UserResponseDto
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.SerializationException
import java.io.IOException
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : UserRepository {
    private val logTag = "UserRepo"

    override suspend fun getUserInfo(userId: Int): Result<UserResponseDto> {
        return try {
            val userInfo = userApi.getUserInfo(userId)
            Log.d(logTag, "Successfully fetched user info for ID $userId")
            Result.success(userInfo)
        } catch (e: ClientRequestException) {
            val errorBody = kotlin.runCatching { e.response.bodyAsText() }.getOrNull()
            Log.e(logTag, "Error fetching user info (Client Error ${e.response.status.value}). Body: $errorBody", e)
            Result.failure(Exception("Could not load user details (client error)."))
        } catch (e: ServerResponseException) {
            val errorBody = kotlin.runCatching { e.response.bodyAsText() }.getOrNull()
            Log.e(logTag, "Error fetching user info (Server Error ${e.response.status.value}). Body: $errorBody", e)
            Result.failure(Exception("Could not load user details (server error)."))
        } catch (e: IOException) {
            Log.e(logTag, "Error fetching user info (Network Error): ${e.localizedMessage}", e)
            Result.failure(Exception("Network error. Please check connection."))
        } catch (e: SerializationException) {
            Log.e(logTag, "Error fetching user info (Serialization Error): ${e.localizedMessage}", e)
            Result.failure(Exception("Error parsing user data."))
        } catch (e: Exception) {
            Log.e(logTag, "Error fetching user info (Unexpected Error): ${e.localizedMessage}", e)
            Result.failure(Exception("An unexpected error occurred while fetching user details."))
        }
    }

    override suspend fun updateProfile(username: String, phoneNumber: String): Result<Unit> {
        return try {
            val request = UpdateProfileRequestDto(
                username = username,
                // Trimitem null dacă string-ul e gol, pentru a se potrivi cu modelul C# (string?)
                phoneNumber = phoneNumber.ifBlank { null }
            )
            val response = userApi.updateProfile(request)

            if (response.status.isSuccess()) {
                Log.d(logTag, "Profile updated successfully.")
                Result.success(Unit)
            } else {
                val errorBody = response.bodyAsText()
                Log.e(logTag, "Failed to update profile: ${response.status}. Body: $errorBody")
                Result.failure(Exception("Update failed: ${response.status.description}"))
            }
        } catch (e: Exception) {
            Log.e(logTag, "Exception during profile update: ${e.message}", e)
            Result.failure(e)
        }
    }
}