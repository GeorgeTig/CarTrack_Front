package com.example.cartrack.core.data.api

import com.example.cartrack.core.data.model.vin.VinDecodedResponseDto
import com.example.cartrack.core.di.AuthenticatedHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

interface VinDecoderApi {
    // Adăugăm clientId înapoi în semnătura metodei
    suspend fun decodeVin(vin: String, clientId: Int): List<VinDecodedResponseDto>
}

class VinDecoderApiImpl @Inject constructor(
    @AuthenticatedHttpClient private val client: HttpClient
) : VinDecoderApi {
    private val BASE_URL = "http://10.0.2.2:5098/api/vindecoder"

    // Construim URL-ul corect, cu ambii parametri
    override suspend fun decodeVin(vin: String, clientId: Int): List<VinDecodedResponseDto> {
        return client.get("$BASE_URL/$vin/$clientId").body()
    }
}