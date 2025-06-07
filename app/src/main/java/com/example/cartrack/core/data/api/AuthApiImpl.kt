package com.example.cartrack.core.data.api

import com.example.cartrack.core.data.model.auth.RefreshTokenRequestDto
import com.example.cartrack.core.data.model.auth.TokenResponseDto
import com.example.cartrack.core.data.model.auth.UserLoginRequestDto
import com.example.cartrack.core.data.model.auth.UserRegisterRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

// Notă: Această clasă nu are nevoie de un qualifier specific, deoarece Dagger va injecta
// clientul HTTP corespunzător în funcție de qualifier-ul folosit în modulul de DI
// la momentul furnizării (`@AuthenticatedAuthApi` vs `@UnauthenticatedAuthApi`).
class AuthApiImpl @Inject constructor(
    private val client: HttpClient
) : AuthApi {

    private val BASE_URL = "http://10.0.2.2:5098/api/auth"

    override suspend fun login(request: UserLoginRequestDto): TokenResponseDto {
        return client.post("$BASE_URL/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun register(request: UserRegisterRequestDto): HttpResponse {
        return client.post("$BASE_URL/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequestDto): TokenResponseDto {
        return client.post("$BASE_URL/refresh") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}