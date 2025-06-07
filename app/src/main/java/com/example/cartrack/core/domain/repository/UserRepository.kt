package com.example.cartrack.core.domain.repository

import com.example.cartrack.core.data.model.user.ChangePasswordRequestDto
import com.example.cartrack.core.data.model.user.UserResponseDto

interface UserRepository {
    suspend fun getUserInfo(): Result<UserResponseDto>
    suspend fun updateProfile(username: String, phoneNumber: String): Result<Unit>
    suspend fun changePassword(request: ChangePasswordRequestDto): Result<Unit>
}