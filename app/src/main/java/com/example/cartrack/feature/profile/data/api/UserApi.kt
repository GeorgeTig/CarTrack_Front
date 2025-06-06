package com.example.cartrack.feature.profile.data.api

import com.example.cartrack.feature.profile.data.model.UpdateProfileRequestDto
import com.example.cartrack.feature.profile.data.model.UserResponseDto
import io.ktor.client.statement.HttpResponse

interface UserApi {
    suspend fun getUserInfo(userId: Int): UserResponseDto
    suspend fun updateProfile(request: UpdateProfileRequestDto): HttpResponse
}