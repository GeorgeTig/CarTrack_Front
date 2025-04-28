package com.example.cartrack.feature.auth.data.api

import com.example.cartrack.feature.auth.data.model.UserLoginRequest
import com.example.cartrack.feature.auth.data.model.UserRegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class AuthApiImpl @Inject constructor(
    private val client: HttpClient
) : AuthApi {


    override suspend fun login(request: UserLoginRequest): String {
        return client.post("http://10.0.2.2:5098/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<String>()
    }

    override suspend fun register(request: UserRegisterRequest): HttpResponse {
        return client.post("http://10.0.2.2:5098/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}