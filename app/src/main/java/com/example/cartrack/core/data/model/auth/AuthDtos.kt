package com.example.cartrack.core.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginRequestDto(
    val email: String,
    val password: String
)

@Serializable
data class UserRegisterRequestDto(
    val username: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val roleId: Int
)

@Serializable
data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String
)