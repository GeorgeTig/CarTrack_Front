package com.example.cartrack.feature.addvehicle.domain.repository

import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto // Import model from this feature

interface VinDecoderRepository {
    // Returns a Result containing the list of decoded possibilities
    suspend fun decodeVin(vin: String, clientId: Int): Result<List<VinDecodedResponseDto>>
}