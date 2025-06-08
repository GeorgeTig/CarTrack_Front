package com.example.cartrack.core.data.api

import com.example.cartrack.core.data.model.vin.VinDecodedResponseDto
import com.example.cartrack.core.di.AuthenticatedHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

interface VinDecoderApi {
    // Adăugăm clientId înapoi în semnătura metodei
    suspend fun decodeVin(vin: String): List<VinDecodedResponseDto>
}
