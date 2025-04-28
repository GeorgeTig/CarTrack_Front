package com.example.cartrack.feature.addvehicle.domain.repository

import com.example.cartrack.feature.addvehicle.data.model.VinDecodedResponseDto

interface VinDecoderRepository {
    suspend fun decodeVin(vin: String, clientId: Int): Result<List<VinDecodedResponseDto>>
}