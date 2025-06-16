package com.example.cartrack.core.data.model.user

import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDto(
    val username: String,
    val email: String,
    val phoneNumber: String
)

@Serializable
data class UpdateProfileRequestDto(
    val username: String,
    val phoneNumber: String?
)

@Serializable
data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String
)