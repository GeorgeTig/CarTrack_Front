package com.example.cartrack.feature.auth.data.model


import kotlinx.serialization.Serializable

@Serializable
data class UserRegisterRequest (
    val username: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val roleId: Int
)