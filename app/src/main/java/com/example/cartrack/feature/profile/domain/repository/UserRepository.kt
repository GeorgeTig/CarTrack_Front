package com.example.cartrack.feature.profile.domain.repository

import com.example.cartrack.feature.profile.data.model.UserResponseDto

interface UserRepository {
    suspend fun getUserInfo(userId: Int): Result<UserResponseDto>
    suspend fun updateProfile(username: String, phoneNumber: String): Result<Unit>
}