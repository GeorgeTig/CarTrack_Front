package com.example.cartrack.feature.profile.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequestDto(
    val username: String,
    val phoneNumber: String?
)