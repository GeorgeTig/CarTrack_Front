package com.example.cartrack.core.domain.repository

import com.example.cartrack.core.data.model.vin.VinDecodedResponseDto

interface VinDecoderRepository {
    suspend fun decodeVin(vin: String, clientId: Int): Result<List<VinDecodedResponseDto>>
}
