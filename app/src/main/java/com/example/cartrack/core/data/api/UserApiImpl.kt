package com.example.cartrack.core.data.api

import com.example.cartrack.core.data.model.user.ChangePasswordRequestDto
import com.example.cartrack.core.data.model.user.UpdateProfileRequestDto
import com.example.cartrack.core.data.model.user.UserResponseDto
import com.example.cartrack.core.di.AuthenticatedHttpClient
import com.example.cartrack.core.storage.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class UserApiImpl @Inject constructor(
    @AuthenticatedHttpClient private val client: HttpClient,
    private val tokenManager: TokenManager
) : UserApi {

    private val BASE_URL = "http://10.0.2.2:5098/api/user"

    override suspend fun getUserInfo(): UserResponseDto {
        return client.get("$BASE_URL/profile") {
            header("Authorization", "Bearer ${tokenManager.getTokens()?.accessToken}")
        }.body()
    }

    override suspend fun updateProfile(request: UpdateProfileRequestDto): HttpResponse {
        return client.put("$BASE_URL/update-profile") {
            header("Authorization", "Bearer ${tokenManager.getTokens()?.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun changePassword(request: ChangePasswordRequestDto): HttpResponse {
        return client.post("$BASE_URL/change-password") {
            header("Authorization", "Bearer ${tokenManager.getTokens()?.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}