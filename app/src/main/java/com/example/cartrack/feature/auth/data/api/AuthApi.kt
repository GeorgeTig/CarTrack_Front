package com.example.cartrack.feature.auth.data.api

import com.example.cartrack.feature.auth.data.model.RefreshTokenRequest
import com.example.cartrack.feature.auth.data.model.TokenResponse
import com.example.cartrack.feature.auth.data.model.UserLoginRequest
import com.example.cartrack.feature.auth.data.model.UserRegisterRequest
import io.ktor.client.statement.HttpResponse

interface AuthApi {
    suspend fun login(request: UserLoginRequest): TokenResponse
    suspend fun register(request: UserRegisterRequest): HttpResponse
    suspend fun refreshToken(request: RefreshTokenRequest): TokenResponse
}