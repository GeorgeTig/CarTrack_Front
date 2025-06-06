package com.example.cartrack.feature.profile.data.api

import com.example.cartrack.core.di.AuthenticatedHttpClient // Va folosi clientul autentificat
import com.example.cartrack.feature.profile.data.model.UpdateProfileRequestDto
import com.example.cartrack.feature.profile.data.model.UserResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class UserApiImpl @Inject constructor(
    @AuthenticatedHttpClient private val client: HttpClient
) : UserApi {

    private val BASE_URL = "http://10.0.2.2:5098/api/user"

    override suspend fun getUserInfo(userId: Int): UserResponseDto {
        return client.get("$BASE_URL/profile") {
            contentType(ContentType.Application.Json)
        }.body<UserResponseDto>()
    }

    override suspend fun updateProfile(request: UpdateProfileRequestDto): HttpResponse {
        return client.put("$BASE_URL/update-profile") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}