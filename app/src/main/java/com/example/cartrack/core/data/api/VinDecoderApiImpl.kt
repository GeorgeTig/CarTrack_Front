package com.example.cartrack.core.data.api

import com.example.cartrack.core.data.model.vin.VinDecodedResponseDto
import com.example.cartrack.core.di.AuthenticatedHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

class VinDecoderApiImpl @Inject constructor(
    @AuthenticatedHttpClient private val client: HttpClient
) : VinDecoderApi {

    private val BASE_URL = "http://10.0.2.2:5098/api/vindecoder"

    override suspend fun decodeVin(vin: String): List<VinDecodedResponseDto> {
        return client.get("$BASE_URL/$vin").body()
    }
}