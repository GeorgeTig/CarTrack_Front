package com.example.cartrack.core.data.api

import com.example.cartrack.core.data.model.vin.VinDecodedResponseDto
import com.example.cartrack.core.di.AuthenticatedHttpClient
import com.example.cartrack.core.storage.TokenManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import javax.inject.Inject

class VinDecoderApiImpl @Inject constructor(
    @AuthenticatedHttpClient private val client: HttpClient,
    private val tokenManager: TokenManager
) : VinDecoderApi {

    private val BASE_URL = "http://10.0.2.2:5098/api/vindecoder"

    override suspend fun decodeVin(vin: String): List<VinDecodedResponseDto> {
        return client.get("$BASE_URL/$vin") {
            header("Authorization", "Bearer ${tokenManager.getTokens()?.accessToken}")
        }.body()
    }
}