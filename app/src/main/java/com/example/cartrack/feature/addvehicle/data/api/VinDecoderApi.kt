package com.example.cartrack.feature.addvehicle.data.api

import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto // Import model from this feature

interface VinDecoderApi {
    // Backend endpoint returns a list of possible matches
    suspend fun decodeVin(vin: String, clientId: Int): List<VinDecodedResponseDto>
}