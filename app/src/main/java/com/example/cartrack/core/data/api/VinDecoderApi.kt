package com.example.cartrack.core.data.api

import com.example.cartrack.core.data.model.vin.VinDecodedResponseDto

interface VinDecoderApi {
    suspend fun decodeVin(vin: String): List<VinDecodedResponseDto>
}
