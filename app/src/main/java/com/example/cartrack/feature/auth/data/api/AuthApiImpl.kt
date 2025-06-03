package com.example.cartrack.feature.auth.data.api

import com.example.cartrack.feature.auth.data.model.RefreshTokenRequest
import com.example.cartrack.feature.auth.data.model.TokenResponse
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
    private val client: HttpClient // Acest client va fi configurat în AppModules
) : AuthApi {

    // Asigură-te că URL-ul este corect și emulatorul poate accesa acest IP/port
    private val BASE_AUTH_URL = "http://10.0.2.2:5098/api/auth"

    override suspend fun login(request: UserLoginRequest): TokenResponse {
        return client.post("$BASE_AUTH_URL/login") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<TokenResponse>()
    }

    override suspend fun register(request: UserRegisterRequest): HttpResponse {
        return client.post("$BASE_AUTH_URL/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun refreshToken(request: RefreshTokenRequest): TokenResponse {
        // Acest apel este făcut de clientul Ktor configurat în AppModules,
        // dar folosind o instanță specifică de AuthApi care utilizează un HttpClient simplu
        // pentru a evita problemele cu plugin-ul Auth.
        return client.post("$BASE_AUTH_URL/refresh") { // Potrivește cu endpoint-ul din AuthController
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<TokenResponse>()
    }
}