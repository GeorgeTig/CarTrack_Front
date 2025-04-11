package com.example.cartrack.feature.addvehicle.data.api

import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto // Import model from this feature
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import javax.inject.Inject

class VinDecoderApiImpl @Inject constructor(
    private val client: HttpClient // Ktor client provided by core module
) : VinDecoderApi {

    // Base URL for the VIN decoding endpoint
    private val BASE_URL = "http://10.0.2.2:5098/api/vindecoder" // Ensure port is correct

    override suspend fun decodeVin(vin: String, clientId: Int): List<VinDecodedResponseDto> {
        // Auth token is added automatically by Ktor Auth plugin (configured in core)
        return client.get("$BASE_URL/$vin/$clientId")
            .body<List<VinDecodedResponseDto>>() // Expecting a JSON array from the backend
    }
}