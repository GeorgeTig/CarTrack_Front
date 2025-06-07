package com.example.cartrack.core.data.api

import com.example.cartrack.core.data.model.user.ChangePasswordRequestDto
import com.example.cartrack.core.data.model.user.UpdateProfileRequestDto
import com.example.cartrack.core.data.model.user.UserResponseDto
import io.ktor.client.statement.HttpResponse

interface UserApi {
    suspend fun getUserInfo(): UserResponseDto
    suspend fun updateProfile(request: UpdateProfileRequestDto): HttpResponse
    suspend fun changePassword(request: ChangePasswordRequestDto): HttpResponse
}