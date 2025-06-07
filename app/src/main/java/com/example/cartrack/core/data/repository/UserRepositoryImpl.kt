package com.example.cartrack.core.data.repository

import android.util.Log
import com.example.cartrack.core.data.api.UserApi
import com.example.cartrack.core.data.model.user.ChangePasswordRequestDto
import com.example.cartrack.core.data.model.user.UpdateProfileRequestDto
import com.example.cartrack.core.data.model.user.UserResponseDto
import com.example.cartrack.core.domain.repository.UserRepository
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi
) : UserRepository {

    private val logTag = "UserRepo"

    override suspend fun getUserInfo(): Result<UserResponseDto> {
        return try {
            val userInfo = userApi.getUserInfo()
            Log.d(logTag, "Successfully fetched user info.")
            Result.success(userInfo)
        } catch (e: Exception) {
            Log.e(logTag, "Error fetching user info: ${e.message}", e)
            Result.failure(Exception("Could not load user profile.", e))
        }
    }

    override suspend fun updateProfile(username: String, phoneNumber: String): Result<Unit> {
        return try {
            val request = UpdateProfileRequestDto(
                username = username,
                phoneNumber = phoneNumber.ifBlank { null } // Trimite null dacă e gol
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

    override suspend fun changePassword(request: ChangePasswordRequestDto): Result<Unit> {
        return try {
            val response = userApi.changePassword(request)
            if (response.status.isSuccess()) {
                Log.d(logTag, "Password changed successfully.")
                Result.success(Unit)
            } else {
                val errorBody = response.bodyAsText()
                Log.e(logTag, "Failed to change password: ${response.status}. Body: $errorBody")
                if (response.status.value == 400) { // Specific pentru parolă curentă greșită
                    Result.failure(Exception("Current password is incorrect."))
                } else {
                    Result.failure(Exception("Failed to change password: ${response.status.description}"))
                }
            }
        } catch (e: Exception) {
            Log.e(logTag, "Exception during password change: ${e.message}", e)
            Result.failure(e)
        }
    }
}