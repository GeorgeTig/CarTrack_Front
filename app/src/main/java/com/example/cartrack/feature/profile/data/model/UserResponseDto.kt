package com.example.cartrack.feature.profile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDto(
    val username: String,
    val email: String,
    val phoneNumber: String // Backend-ul trimite int, dar e mai sigur ca String pe client pentru afișare "N/A"
    // Sau păstrează Int și formatează în ViewModel/UI
)