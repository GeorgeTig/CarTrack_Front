package com.example.cartrack.core.data.repository

import com.example.cartrack.core.data.api.UserApi
import com.example.cartrack.core.data.api.safeApiCall
import com.example.cartrack.core.data.model.user.ChangePasswordRequestDto
import com.example.cartrack.core.data.model.user.UpdateProfileRequestDto
import com.example.cartrack.core.data.model.user.UserResponseDto
import com.example.cartrack.core.domain.repository.AuthRepository
import com.example.cartrack.core.domain.repository.UserRepository
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import javax.inject.Inject
import javax.inject.Provider

class UserRepositoryImpl @Inject constructor(
    private val userApi: UserApi,
    private val authRepositoryProvider: Provider<AuthRepository>
) : UserRepository {

    override suspend fun getUserInfo(): Result<UserResponseDto> =
        safeApiCall(authRepositoryProvider, "User Info") { userApi.getUserInfo() }

    override suspend fun updateProfile(username: String, phoneNumber: String): Result<Unit> {
        val request = UpdateProfileRequestDto(
            username = username,
            phoneNumber = phoneNumber.ifBlank { null }
        )
        return safeApiCall(authRepositoryProvider, "Update Profile") { userApi.updateProfile(request); Unit }
    }

    override suspend fun changePassword(request: ChangePasswordRequestDto): Result<Unit> {
        return try {
            safeApiCall(authRepositoryProvider, "Change Password") { userApi.changePassword(request); Unit }
        } catch (e: ResponseException) {
            if (e.response.status == HttpStatusCode.BadRequest) {
                Result.failure(Exception("Current password is incorrect."))
            } else {
                Result.failure(e)
            }
        }
    }
}