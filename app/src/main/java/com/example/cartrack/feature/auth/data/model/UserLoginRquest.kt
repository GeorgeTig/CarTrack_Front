package com.example.cartrack.feature.auth.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserLoginRequest (
    val email: String,
    val password: String
)