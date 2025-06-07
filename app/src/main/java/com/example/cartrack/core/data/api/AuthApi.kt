package com.example.cartrack.core.data.api

import com.example.cartrack.core.data.model.auth.RefreshTokenRequestDto
import com.example.cartrack.core.data.model.auth.TokenResponseDto
import com.example.cartrack.core.data.model.auth.UserLoginRequestDto
import com.example.cartrack.core.data.model.auth.UserRegisterRequestDto
import io.ktor.client.statement.HttpResponse

interface AuthApi {
    suspend fun login(request: UserLoginRequestDto): TokenResponseDto
    suspend fun register(request: UserRegisterRequestDto): HttpResponse
    suspend fun refreshToken(request: RefreshTokenRequestDto): TokenResponseDto
}