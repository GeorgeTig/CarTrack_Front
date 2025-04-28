package com.example.cartrack.feature.addvehicle.data.api

import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto

interface VinDecoderApi {
    suspend fun decodeVin(vin: String, clientId: Int): List<VinDecodedResponseDto>
}