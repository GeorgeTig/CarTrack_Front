package com.example.cartrack.core.data.model.user

import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDto(
    val username: String,
    val email: String,
    val phoneNumber: String // Chiar dacă backend-ul are `long?`, e mai sigur ca String pe client
)

@Serializable
data class UpdateProfileRequestDto(
    val username: String,
    val phoneNumber: String? // Folosim `String?` pentru a putea trimite null dacă e gol
)

@Serializable
data class ChangePasswordRequestDto(
    val currentPassword: String,
    val newPassword: String
)