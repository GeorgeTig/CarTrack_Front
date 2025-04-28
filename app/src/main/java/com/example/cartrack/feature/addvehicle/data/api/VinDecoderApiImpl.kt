package com.example.cartrack.feature.addvehicle.data.api

import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto // Import model from this feature
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import javax.inject.Inject

class VinDecoderApiImpl @Inject constructor(
    private val client: HttpClient
) : VinDecoderApi {

    private val BASE_URL = "http://10.0.2.2:5098/api/vindecoder"

    override suspend fun decodeVin(vin: String, clientId: Int): List<VinDecodedResponseDto> {
        return client.get("$BASE_URL/$vin/$clientId")
            .body<List<VinDecodedResponseDto>>()
    }
}