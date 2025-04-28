package com.example.cartrack.feature.auth.data.api

import com.example.cartrack.feature.auth.data.model.UserLoginRequest
import com.example.cartrack.feature.auth.data.model.UserRegisterRequest
import io.ktor.client.statement.HttpResponse

interface AuthApi {
    suspend fun login(request: UserLoginRequest): String
    suspend fun register(request: UserRegisterRequest): HttpResponse
}