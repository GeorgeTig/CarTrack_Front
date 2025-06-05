package com.example.cartrack.feature.profile.data.api

import com.example.cartrack.feature.profile.data.model.UserResponseDto

interface UserApi {
    suspend fun getUserInfo(userId: Int): UserResponseDto
}